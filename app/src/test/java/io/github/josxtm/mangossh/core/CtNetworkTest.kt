package io.github.josxtm.mangossh.core

import org.junit.Assert.*
import org.junit.Test

class CtNetworkTest {
    private val base = listOf(198, 51, 100, 0).joinToString(".")
    private val mask = listOf(255, 255, 255, 0).joinToString(".")
    private fun local() = CtRuntimeConfig(
        wanOffset = 12,
        targets = (1..7).map { CtOffset("Equipo $it", it) }
    )
    @Test fun plansSevenTargetsFromUserOwnedOffsets() {
        val plan = CtPlanning.build("TEST1", base, mask, local())
        assertEquals(24, plan.prefix)
        assertEquals(listOf(198, 51, 100, 12).joinToString("."), plan.mangoIp)
        assertEquals(7, plan.targets.size)
        assertEquals(listOf(198, 51, 100, 1).joinToString("."), plan.targets.first().ip)
    }
    @Test fun missingPolicyNeverInfersOperationalTopology() {
        assertThrows(IllegalArgumentException::class.java) {
            CtPlanning.build("TEST1", base, mask)
        }
    }
    @Test fun rejectsOverlappingAndOutOfRangeOffsets() {
        assertThrows(IllegalArgumentException::class.java) {
            CtPlanning.build("TEST1", base, mask,
                CtRuntimeConfig(1, listOf(CtOffset("Equipo", 1))))
        }
        assertThrows(IllegalArgumentException::class.java) {
            CtPlanning.build("TEST1", base, mask,
                CtRuntimeConfig(12, listOf(CtOffset("Equipo", 260))))
        }
    }
    @Test fun rejectsNonContiguousMaskAndBroadcastAddress() {
        val bad = listOf(255, 0, 255, 0).joinToString(".")
        assertThrows(IllegalArgumentException::class.java) {
            CtPlanning.build("TEST1", base, bad, local())
        }
        assertThrows(IllegalArgumentException::class.java) {
            CtPlanning.build("TEST1", base, mask, CtRuntimeConfig(255, listOf(CtOffset("Equipo", 1))))
        }
    }
    @Test fun mangoChangesAreOnlyWANAndReversible() {
        val plan = CtPlanning.build("TEST1", base, mask, local())
        val apply = CtCommands.apply(plan).shell
        val restore = CtCommands.restore().shell
        assertTrue(CtCommands.preflight().shell.contains("GL-MT300N"))
        assertTrue(apply.contains("network.wan.ipaddr"))
        assertTrue(apply.contains("mangossh-ct-wan-rollback"))
        assertTrue(restore.contains("cp -p"))
        assertTrue(CtCommands.verify(plan).shell.contains("CT_WAN_OK"))
        assertFalse(apply.contains("network.lan."))
        assertFalse(restore.contains("network.lan."))
    }
    @Test fun noUnlistedPingTargets() {
        val plan = CtPlanning.build("TEST1", base, mask, local())
        val unauthorized = CtTarget("Otro", 2, plan.targets[1].ip)
        assertThrows(IllegalArgumentException::class.java) {
            CtCommands.ping(plan, unauthorized)
        }
        assertTrue(CtCommands.ping(plan, plan.targets.first()).shell.contains("ping -c 4"))
    }
}
