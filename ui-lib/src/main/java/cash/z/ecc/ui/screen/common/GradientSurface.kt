package cash.z.ecc.ui.screen.common

import androidx.compose.foundation.background
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import cash.z.ecc.ui.theme.ZcashTheme

@Composable
fun GradientSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
            .background(ZcashTheme.colors.surfaceGradient()),
        shape = RectangleShape,
        content = content
    )
}