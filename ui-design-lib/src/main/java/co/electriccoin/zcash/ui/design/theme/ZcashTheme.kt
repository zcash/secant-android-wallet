package co.electriccoin.zcash.ui.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import co.electriccoin.zcash.ui.design.theme.internal.DarkColorPalette
import co.electriccoin.zcash.ui.design.theme.internal.DarkExtendedColorPalette
import co.electriccoin.zcash.ui.design.theme.internal.ExtendedTypography
import co.electriccoin.zcash.ui.design.theme.internal.LightColorPalette
import co.electriccoin.zcash.ui.design.theme.internal.LightExtendedColorPalette
import co.electriccoin.zcash.ui.design.theme.internal.LocalExtendedColors
import co.electriccoin.zcash.ui.design.theme.internal.LocalExtendedTypography
import co.electriccoin.zcash.ui.design.theme.internal.Typography

@Composable
fun ZcashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val baseColors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val extendedColors = if (darkTheme) {
        DarkExtendedColorPalette
    } else {
        LightExtendedColorPalette
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = baseColors,
            typography = Typography,
            content = content
        )
    }
}

// Use with eg. ZcashTheme.colors.tertiary
object ZcashTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val typography: ExtendedTypography
        @Composable
        get() = LocalExtendedTypography.current
}
