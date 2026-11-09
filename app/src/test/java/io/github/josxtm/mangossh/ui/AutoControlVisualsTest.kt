package io.github.josxtm.mangossh.ui

import io.github.josxtm.mangossh.Preferences
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.Color

/** Public UI regression tests; no CT/IP credentials or private assets. */
class AutoControlVisualsTest {
    @Test fun matchesTenApprovedAutoControlThemeNames() {
        assertEquals(10, mangoThemes.size)
        assertEquals(
            listOf("T1 Claro Profesional", "T2 Azul Corporativo", "T3 Verde Técnico",
                "T4 Gris Industrial", "T5 Arena / Trabajo de Campo", "T6 Noche Suave",
                "T7 Rojo Operativo", "T8 Púrpura Técnico", "T9 Turquesa Moderno",
                "T10 Inspiración macOS"),
            mangoThemes.map { it.name }
        )
    }

    @Test fun selectsIndustrialGreyT4ByDefaultWithoutOverwritingExistingPreferences() {
        assertEquals(3, DEFAULT_MANGO_THEME)
        val original = Preferences(fontSize = 19, timeout = 30, saveOutput = true)
        assertEquals(3, original.tabletTheme)
        assertEquals(19, original.copy(tabletTheme = 0).fontSize)
        assertEquals(30, original.copy(tabletTheme = 0).timeout)
        assertTrue(original.copy(tabletTheme = 0).saveOutput)
        assertEquals(Color(0xFFA92E35), mangoAutoColors(3, "Claro", false).primary)
        assertEquals(Color(0xFFE5E7E9), mangoAutoColors(3, "Claro", false).background)
    }

    @Test fun usesDarkAppearanceWithoutChangingSelectedPalette() {
        assertEquals(Color(0xFF202B38), mangoAutoColors(5, "Claro", false).background)
        assertNotEquals(
            mangoAutoColors(3, "Claro", false).background,
            mangoAutoColors(3, "Oscuro", false).background
        )
        assertEquals(
            mangoAutoColors(3, "Oscuro", false).background,
            mangoAutoColors(3, "Sistema", true).background
        )
    }

    @Test fun clampsOutOfRangeThemeToSafeDefault() {
        assertEquals(mangoAutoColors(3, "Claro", false).primary,
            mangoAutoColors(999, "Claro", false).primary)
    }
}
