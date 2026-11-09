package io.github.josxtm.mangossh.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Same 10 approved names, accents and surfaces as AUTOCONTROL-PUBLIC
 * feat/tablet-industrial-gui-mangossh @ a871f549da (09 Nov 2026).
 *
 * Visual values only: this module never reads SSH credentials or CT data.
 * Index 3 = T4 Gris Industrial, the AutoControL tablet default.
 */
data class MangoThemeToken(
    val name: String,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val dark: Boolean = false
)

val mangoThemes = listOf(
    MangoThemeToken("T1 Claro Profesional", Color(0xFF487B96), Color(0xFFF4F7FA), Color.White),
    MangoThemeToken("T2 Azul Corporativo", Color(0xFF366C9C), Color(0xFFE7F0F8), Color.White),
    MangoThemeToken("T3 Verde Técnico", Color(0xFF47795A), Color(0xFFF1F6EC), Color.White),
    MangoThemeToken("T4 Gris Industrial", Color(0xFFA92E35), Color(0xFFE5E7E9), Color(0xFFF8F8F7)),
    MangoThemeToken("T5 Arena / Trabajo de Campo", Color(0xFF9A7343), Color(0xFFF8F2E8), Color(0xFFFFFCF7)),
    MangoThemeToken("T6 Noche Suave", Color(0xFF90B9C9), Color(0xFF202B38), Color(0xFF2A3949), true),
    MangoThemeToken("T7 Rojo Operativo", Color(0xFFB44B4D), Color(0xFFFFF0EF), Color.White),
    MangoThemeToken("T8 Púrpura Técnico", Color(0xFF8069AB), Color(0xFFF3F0FA), Color.White),
    MangoThemeToken("T9 Turquesa Moderno", Color(0xFF348C94), Color(0xFFECF8F8), Color.White),
    MangoThemeToken("T10 Inspiración macOS", Color(0xFF688CB8), Color(0xFFECF1FA), Color(0xFFF8FAFE))
)

const val DEFAULT_MANGO_THEME = 3

fun mangoAutoColors(index: Int, mode: String, systemDark: Boolean): ColorScheme {
    val selected = mangoThemes.getOrElse(index) { mangoThemes[DEFAULT_MANGO_THEME] }
    val dark = when (mode) {
        "Oscuro" -> true
        "Sistema" -> if (selected.dark) true else systemDark
        else -> selected.dark
    }
    if (dark) {
        val accent = if (selected.dark) selected.accent else
            when (index) {
                3 -> Color(0xFFE49B9F) // Industrial red accessible on graphite.
                6 -> Color(0xFFE6A7AB)
                else -> selected.accent.copy(alpha = 0.95f)
            }
        return darkColorScheme(
            primary = accent,
            onPrimary = Color(0xFF20242A),
            secondary = accent,
            background = if (selected.dark) selected.background else Color(0xFF212830),
            onBackground = Color(0xFFF0F3F4),
            surface = if (selected.dark) selected.surface else Color(0xFF2D363F),
            onSurface = Color(0xFFF0F3F4),
            surfaceVariant = if (selected.dark) Color(0xFF354757) else Color(0xFF3C464F),
            onSurfaceVariant = Color(0xFFDFE3E5),
            outline = Color(0xFF87949C),
            error = Color(0xFFF1AAA9)
        )
    }
    val onLight = Color(0xFF242C32)
    return lightColorScheme(
        primary = selected.accent,
        onPrimary = Color.White,
        secondary = selected.accent,
        background = selected.background,
        onBackground = onLight,
        surface = selected.surface,
        onSurface = onLight,
        surfaceVariant = when (index) {
            3 -> Color(0xFFF0F1F1)
            else -> selected.background
        },
        onSurfaceVariant = Color(0xFF4D5861),
        outline = Color(0xFF89949A),
        error = Color(0xFFB12626)
    )
}
