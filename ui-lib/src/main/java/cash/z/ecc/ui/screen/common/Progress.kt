package cash.z.ecc.ui.screen.common

import android.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.widget.ProgressBar
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import cash.z.ecc.ui.screen.onboarding.model.Progress
import cash.z.ecc.ui.theme.ZcashTheme

// Eventually rename to GradientLinearProgressIndicator
@Composable
fun PinkProgress(progress: Progress, modifier: Modifier = Modifier) {
    // Needs custom implementation to apply gradient
    LinearProgressIndicator(
        progress = progress.percent().decimal, modifier,
        ZcashTheme.colors.progressStart, ZcashTheme.colors.progressBackground
    )
}

@Composable
fun AndroidProgressView(progress: Progress, modifier: Modifier = Modifier) {
    val backgroundColor = ZcashTheme.colors.progressBackground.toArgb()
    val startColor = ZcashTheme.colors.progressStart.toArgb()
    val endColor = ZcashTheme.colors.progressEnd.toArgb()
    AndroidView(
        factory = {
            ProgressBar(it, null, R.attr.progressBarStyleHorizontal).apply {
                isIndeterminate = false
                background = ColorDrawable(backgroundColor)
                progressDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(startColor, endColor))

                setProgress(progress.current.value)
                max = progress.last.value
            }
        },
        modifier = modifier,
        update = {
            it.progress = progress.current.value
            it.max = progress.last.value
        })
}
