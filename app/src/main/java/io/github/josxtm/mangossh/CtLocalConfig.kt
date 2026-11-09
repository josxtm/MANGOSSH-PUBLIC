package io.github.josxtm.mangossh

import io.github.josxtm.mangossh.core.CtOffset
import io.github.josxtm.mangossh.core.CtRuntimeConfig
import org.json.JSONArray
import org.json.JSONObject

/** Local-only configuration. Only validated offsets and labels reach encrypted storage. */
object CtLocalConfigCodec {
    const val MAX_BYTES = 65536
    fun parse(bytes: ByteArray): CtRuntimeConfig {
        require(bytes.size in 1..MAX_BYTES) { "Archivo de parámetros vacío o demasiado grande" }
        return try {
            val json = JSONObject(bytes.toString(Charsets.UTF_8).trimStart('\uFEFF'))
            require(json.getInt("version") == 1) { "Versión de parámetros no compatible" }
            val entries = json.getJSONArray("targets")
            require(entries.length() in 1..16) { "Indica de 1 a 16 destinos" }
            val destinations = (0 until entries.length()).map { index ->
                val item = entries.getJSONObject(index)
                CtOffset(item.getString("name").trim(), item.getInt("offset"))
            }
            CtRuntimeConfig(json.getInt("wanOffset"), destinations).also { it.validate() }
        } catch (error: org.json.JSONException) {
            throw IllegalArgumentException("Formato de parámetros no válido", error)
        }
    }
    fun canonical(config: CtRuntimeConfig): ByteArray {
        config.validate()
        val json = JSONObject().put("version", 1).put("wanOffset", config.wanOffset)
        val entries = JSONArray()
        config.targets.forEach { entries.put(JSONObject().put("name", it.name).put("offset", it.offset)) }
        json.put("targets", entries)
        return json.toString().toByteArray(Charsets.UTF_8)
    }
}
