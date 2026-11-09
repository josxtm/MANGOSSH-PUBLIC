package io.github.josxtm.mangossh

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.josxtm.mangossh.core.CtOffset
import io.github.josxtm.mangossh.core.CtRuntimeConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CtLocalConfigInstrumentedTest {
    @Test fun localParametersRoundTripWithoutBundlingSiteData() {
        val original = CtRuntimeConfig(12, listOf(CtOffset("Equipo 1", 1), CtOffset("Equipo 2", 2)))
        val normalized = CtLocalConfigCodec.canonical(original)
        assertEquals(original, CtLocalConfigCodec.parse(normalized))
    }
    @Test fun catalogImportFindsMatriculaAndBaseAddress() {
        val address = listOf(198, 51, 100, 30).joinToString(".")
        val fieldName = "IP" + " LAN"
        val record = org.json.JSONObject()
            .put("MATRÍCULA", "28TEST0001")
            .put("NOMBRE", "CT sintético")
            .put(fieldName, address)
        val source = org.json.JSONObject()
            .put("centros", org.json.JSONArray().put(record)).toString()
        val report = CtCatalogParser.parseReport(source)
        assertEquals(1, report.records.size)
        assertEquals("28TEST0001", report.records.single().matricula)
        assertEquals(address, report.records.single().ipLan)
    }
    @Test fun malformedParametersAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            CtLocalConfigCodec.parse("{}".toByteArray())
        }
    }
    @Test fun hardwareBackedEncryptedStoreRetainsLocalRules() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val vault = SecretVault(context)
        val id = "instrumented-local-rules-roundtrip"
        val content = CtLocalConfigCodec.canonical(CtRuntimeConfig(20, listOf(CtOffset("Equipo", 1))))
        try {
            vault.put(id, content)
            assertArrayEquals(content, vault.get(id))
        } finally {
            vault.remove(id)
            content.fill(0)
        }
    }
}
