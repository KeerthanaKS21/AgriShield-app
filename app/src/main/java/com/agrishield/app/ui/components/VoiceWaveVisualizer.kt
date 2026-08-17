package com.agrishield.app.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrishield.app.ui.theme.AgriGreenLight
import com.agrishield.app.ui.theme.AgriGreenPrimary

@Composable
fun VoiceWaveVisualizer(
    isListening: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = AgriGreenPrimary,
    maxHeight: Dp = 32.dp
) {
    if (!isListening) return

    val transition = rememberInfiniteTransition(label = "VoiceWave")

    val h1 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(280, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 10f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 24f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(310, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(waveColor)
            )
        }
    }
}
