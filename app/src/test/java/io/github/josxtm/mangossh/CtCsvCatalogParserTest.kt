package io.github.josxtm.mangossh

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.util.zip.GZIPOutputStream

class CtCsvCatalogParserTest {
    private fun address(last: Int) = listOf(198, 51, 100, last).joinToString(".")
    private fun csv(vararg lines: String): String = lines.joinToString("\r\n") + "\r\n"

    @Test fun importsQuotedSemicolonAndSafeTechnicalData() {
        val source = csv(
            "Matrícula;Nombre CT;IP" + " LAN;Máscara;Municipio;CPU;Contraseña SSH;API key",
            "28TEST1;\"Centro; con nombre\";" + address(8) +
                ";" + listOf(255,255,255,0).joinToString(".") + ";Alcala;CPU-X;NO_EXPORTAR;NO_EXPORTAR",
            "28TEST2;Segundo;" + address(9) + ";;Madrid;CPU-Y;NO_EXPORTAR;NO_EXPORTAR"
        )
        val report = CtCsvCatalogParser.parse(StringReader(source))
        assertEquals(2, report.records.size)
        assertEquals("Centro; con nombre", report.records[0].name)
        assertEquals("CPU-X", report.records[0].technicalFields["CPU"])
        assertEquals("Alcala", report.records[0].technicalFields["MUNICIPIO"])
        assertFalse(report.toString().contains("NO_EXPORTAR"))
        assertTrue(report.records.all { r ->
            r.technicalFields.keys.none { it.contains("PASSWORD") || it.contains("CONTRASEN") || it.contains("APIKEY") }
        })
        assertTrue(report.records[1].netmask.isEmpty())
    }

    @Test fun csvAndGzipUseSameDispatcher() {
        val original = csv("MATRICULA,NOMBRE,IPLAN", "28TEST1,Primero," + address(8))
        val bytes = original.toByteArray(Charsets.UTF_8)
        val compressed = ByteArrayOutputStream().also { out ->
            GZIPOutputStream(out).use { it.write(bytes) }
        }.toByteArray()
        val plain = CtImportStream.parse(ByteArrayInputStream(bytes))
        val gzip = CtImportStream.parse(ByteArrayInputStream(compressed))
        assertEquals(plain.records, gzip.records)
        assertEquals(1, gzip.records.size)
    }

    @Test fun rejectsMalformedCsvWithoutReplacingPreviousIndex() {
        val malformed = "MATRICULA;IPLAN\n28TEST1;\"" + address(8)
        assertThrows(IllegalArgumentException::class.java) {
            CtCsvCatalogParser.parse(StringReader(malformed))
        }
        assertThrows(IllegalArgumentException::class.java) {
            CtCsvCatalogParser.parse(StringReader("IDENTIFICADOR;OTRO\n28TEST1;hola\n"))
        }
    }

    @Test fun ambiguousMatriculaNeverBecomesExecutableRecord() {
        val data = csv("MATRICULA;IPLAN",
            "28TEST1;" + address(8),
            "28TEST1;" + address(9),
            "28TEST2;",
            "28TEST3;not-an-ip")
        val report = CtCsvCatalogParser.parse(StringReader(data))
        assertTrue(report.records.none { it.matricula == "28TEST1" })
        assertEquals(1, report.duplicates)
        assertEquals(1, report.missingIp)
        assertEquals(1, report.invalidIp)
        assertEquals(1, report.records.size)
        assertEquals("28TEST2", report.records[0].matricula)
    }

    @Test fun quotedNewlinesAndEscapedQuotes() {
        val data = "MATRICULA,IPLAN,NOMBRE\n28TEST1," + address(8) +
            ",\"Uno \ncon \"\"comillas\"\"\"\n"
        val row = CtCsvCatalogParser.parse(StringReader(data)).records.single()
        assertEquals("Uno \ncon \"comillas\"", row.name)
    }

    @Test fun rejectsOverwideRowsAndInvalidMask() {
        val data = csv("MATRICULA;IPLAN;MASCARA",
            "28TEST1;" + address(8) + ";" + listOf(255,0,255,0).joinToString("."))
        assertEquals(0, CtCsvCatalogParser.parse(StringReader(data)).records.size)
        assertThrows(IllegalArgumentException::class.java) {
            CtCsvCatalogParser.parse(StringReader(csv("MATRICULA;IPLAN",
                "28TEST1;" + address(8) + ";extra")))
        }
    }
    @Test fun parsesPrefixMaskAndIgnoresDuplicateUnusedHeaders() {
        val data = csv("MATRICULA;IPLAN;MASCARA;VACIO;VACIO",
            "28TEST1;" + address(9) + ";/27;uno;dos")
        val report = CtCsvCatalogParser.parse(StringReader(data))
        assertEquals(1, report.records.size)
        assertEquals(listOf(255,255,255,224).joinToString("."), report.records.single().netmask)
    }
    @Test fun recognizesGridStyleHeaderWithManyColumns() {
        val headers = listOf("MATRICULA [BDI]", "NOMBRE [BDI]",
            "IP" + " LAN [EDIT]") + (3 until 364).map { "AUXILIAR_${it}" }
        val row = listOf("28TEST1", "Centro de ensayo", address(8)) +
            (3 until 364).map { "" }
        val data = headers.joinToString(";") + "\n" + row.joinToString(";") + "\n"
        val record = CtCsvCatalogParser.parse(StringReader(data)).records.single()
        assertEquals("28TEST1", record.matricula)
        assertEquals("Centro de ensayo", record.name)
        assertEquals(address(8), record.ipLan)
        assertTrue(record.netmask.isBlank())
    }
}
