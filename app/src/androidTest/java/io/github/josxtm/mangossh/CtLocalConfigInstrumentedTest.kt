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
