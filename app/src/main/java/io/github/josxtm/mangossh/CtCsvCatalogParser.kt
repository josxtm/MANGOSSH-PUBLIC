package io.github.josxtm.mangossh

import io.github.josxtm.mangossh.core.Validation
import java.io.BufferedReader
import java.io.PushbackReader
import java.io.Reader
import java.text.Normalizer
import java.util.Locale

/**
 * Streaming CSV reader: no row is retained after it has been indexed.
 * Only explicitly allowed technical columns are indexed; confidential columns
 * are discarded before a CtRecord is created.
 */
object CtCsvCatalogParser {
    private const val MAX_RECORDS = 300_000
    private const val MAX_COLUMNS = 512
    private const val MAX_CELL = 8_192

    private fun key(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace(Regex("[\u0300-\u036f]"), "")
            .uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")

    private val ids = setOf("MATRICULA", "MATRICULACT", "CTMATRICULA", "MATRICULABDI", "IDCT")
    private val ips = setOf("IPLAN", "LANIP", "IPLANEDIT", "DIRECCIONIPLAN", "DIRECCIONLAN", "IPREDLAN", "IPLANCT")
    private val names = setOf("NOMBRE", "NOMBRECT", "NOMBREBDI", "NOMBRECENTRO", "CENTRO", "DENOMINACION")
    private val masks = setOf("MASCARA", "MASCARARED", "NETMASK", "SUBNETMASK", "LANMASK", "MASCARALAN")
    private val privateWords = listOf("PASSWORD", "PASSWD", "CONTRASEN", "SECRET", "TOKEN",
        "CREDENCIAL", "PRIVATEKEY", "APIKEY", "CLAVE", "AUTENTIC", "USUARIO", "LOGIN", "PSK")
    // Whitelist of technical metadata; unknown fields are never persisted.
    private val technicalWords = listOf("CODIGOBDI", "MUNICIPIO", "CALLE", "DIRECCION", "PROVINCIA",
        "ALIMENTACION", "GST", "CPU", "RFID", "CAA", "IPTC", "NTRAFOS",
        "TRAFO", "TRANSFORMACION", "MODELO", "FABRICANTE", "NUMEROSERIE",
        "POTENCIA", "EDIFICACION", "TIPOCENTRO", "TIPO", "TENSION", "SUBESTACION",
        "POSICION", "FECHA", "COMUNICACION", "CIRCUITO", "OPERADOR", "BAHIA", "PINGS")

    private fun allowedTechnical(k: String): Boolean =
        k.isNotBlank() && privateWords.none { k.contains(it) } &&
            !(k.startsWith("PIN") && !k.startsWith("PINGS")) &&
            technicalWords.any { k == it || k.startsWith(it) }

    private fun isId(raw: String): Boolean {
        val s = CtCatalogParser.normalizeMatricula(raw)
        return s.matches(Regex("[A-Z0-9-]{1,40}")) && s.any(Char::isDigit)
    }

    fun parse(input: Reader): CtCatalogReport {
        val buffered = if (input is BufferedReader) input else BufferedReader(input, 64 * 1024)
        buffered.mark(131_072)
        val headerLine = buffered.readLine() ?: throw IllegalArgumentException("CSV vacío")
        require(headerLine.length < 100_000) { "Cabecera CSV demasiado larga" }
        buffered.reset()
        val options = listOf(';', ',', '\t', '|')
        fun occurrences(delimiter: Char): Int {
            var inside = false
            var count = 0
            var i = 0
            while (i < headerLine.length) {
                if (headerLine[i] == '"') {
                    if (inside && i + 1 < headerLine.length && headerLine[i + 1] == '"') i++
                    else inside = !inside
                } else if (!inside && headerLine[i] == delimiter) count++
                i++
            }
            return count
        }
        val delimiter = options.maxByOrNull(::occurrences) ?: ';'
        require(occurrences(delimiter) > 0) { "No se reconoce el separador CSV" }
        val csv = CsvRows(PushbackReader(buffered, 1), delimiter)
        val columns = csv.next() ?: throw IllegalArgumentException("CSV sin cabecera")
        require(columns.size in 2..MAX_COLUMNS) { "Cantidad de columnas CSV no admitida" }
        val normalized = columns.map(::key)
        // Repeated decorative columns are permitted, but two candidate ID or
        // address columns are ambiguous and must not trigger network changes.
        require(normalized.count { it in ids } == 1 && normalized.count { it in ips } == 1) {
            "Cabeceras de matrícula o dirección LAN ambiguas"
        }
        val idIndex = normalized.indexOfFirst { it in ids }
        val ipIndex = normalized.indexOfFirst { it in ips }
        require(idIndex >= 0 && ipIndex >= 0) { "Faltan columnas de matrícula o dirección LAN" }
        val nameIndex = normalized.indexOfFirst { it in names }
        val maskIndex = normalized.indexOfFirst { it in masks }
        val metaIndices = normalized.indices.filter { i ->
            i != idIndex && i != ipIndex && i != nameIndex && i != maskIndex &&
                allowedTechnical(normalized[i])
        }.take(40)
        val found = linkedMapOf<String, CtRecord>()
        val ambiguous = hashSetOf<String>()
        var candidates = 0
        var missingId = 0
        var missingIp = 0
        var invalidIp = 0
        var duplicates = 0
        while (true) {
            val cells = csv.next() ?: break
            if (cells.all(String::isBlank)) continue
            require(cells.size == columns.size) { "CSV con filas de diferente número de columnas" }
            candidates++
            require(candidates <= MAX_RECORDS) { "Demasiados registros CT" }
            val id = CtCatalogParser.normalizeMatricula(cells[idIndex])
            if (!isId(id)) { missingId++; continue }
            val ip = cells[ipIndex].trim()
            if (ip.isEmpty()) missingIp++
            else if (!Validation.ipv4(ip)) { invalidIp++; continue }
            val sourceMask = if (maskIndex >= 0) cells[maskIndex].trim() else ""
            val mask = parseMask(sourceMask)
            if (sourceMask.isNotEmpty() && mask == null) { invalidIp++; continue }
            if (id in ambiguous) { duplicates++; continue }
            if (id in found) {
                // Ambiguous identifiers must never cause a network write.
                found.remove(id)
                ambiguous.add(id)
                duplicates++
                continue
            }
            val attrs = linkedMapOf<String, String>()
            for (i in metaIndices) {
                val value = cells[i].trim()
                if (value.isNotEmpty()) attrs[normalized[i]] = value.take(160)
            }
            found[id] = CtRecord(id,
                if (nameIndex >= 0) cells[nameIndex].trim().take(120).ifBlank { id } else id,
                ip, mask.orEmpty(), attrs)
        }
        return CtCatalogReport(found.values.toList(), candidates, missingIp, invalidIp,
            missingId, duplicates)
    }

    /** A provided prefix is converted; an absent mask is never guessed. */
    private fun parseMask(raw: String): String? {
        if (raw.isEmpty()) return ""
        val prefix = raw.removePrefix("/").toIntOrNull()
        if (prefix != null) {
            if (prefix !in 1..30) return null
            val n = (0xffffffffL shl (32 - prefix)) and 0xffffffffL
            return (3 downTo 0).joinToString(".") { ((n ushr (it * 8)) and 255).toString() }
        }
        if (!Validation.ipv4(raw)) return null
        val n = raw.split('.').fold(0L) { acc, v -> (acc shl 8) or v.toLong() }
        val inverse = n.inv() and 0xffffffffL
        return if (inverse >= 3 && inverse and (inverse + 1) == 0L) raw else null
    }

    /** RFC 4180-style quoted cells, including escaped quotes and embedded newlines. */
    private class CsvRows(private val reader: PushbackReader, private val separator: Char) {
        fun next(): List<String>? {
            val row = ArrayList<String>(32)
            val field = StringBuilder()
            var quoted = false
            var closed = false
            var touched = false
            fun append(c: Char) {
                require(field.length < MAX_CELL) { "Campo CSV demasiado grande" }
                field.append(c)
            }
            fun endField() {
                require(row.size < MAX_COLUMNS) { "Demasiadas columnas CSV" }
                row.add(field.toString())
                field.setLength(0)
                closed = false
            }
            while (true) {
                val code = reader.read()
                if (code == -1) {
                    require(!quoted) { "CSV con comillas sin cerrar" }
                    if (!touched && row.isEmpty() && field.isEmpty()) return null
                    endField()
                    return row
                }
                touched = true
                val c = code.toChar()
                if (quoted) {
                    if (c == '"') {
                        val next = reader.read()
                        if (next == '"'.code) append('"')
                        else {
                            quoted = false
                            closed = true
                            if (next >= 0) reader.unread(next)
                        }
                    } else append(c)
                } else when (c) {
                    separator -> endField()
                    '\n' -> { endField(); return row }
                    '\r' -> {
                        val next = reader.read()
                        if (next != '\n'.code && next >= 0) reader.unread(next)
                        endField()
                        return row
                    }
                    '"' -> {
                        require(!closed && field.isEmpty()) { "Comillas CSV mal formadas" }
                        quoted = true
                    }
                    else -> {
                        require(!closed || c == ' ' || c == '\t') { "Texto después de comillas CSV" }
                        if (!closed) append(c)
                    }
                }
            }
        }
    }
}
