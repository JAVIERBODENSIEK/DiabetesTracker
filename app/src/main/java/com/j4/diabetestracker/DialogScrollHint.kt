package com.j4.diabetestracker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun ScrollableDialogHint(
    selectedLanguage: String,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val showScrollHint by remember(scrollState) {
        derivedStateOf { scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue }
    }
    val hintAlpha by animateFloatAsState(
        targetValue = if (showScrollHint) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "dialogScrollHintAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .alpha(hintAlpha),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = when (selectedLanguage) {
                "German" -> "Weitere Optionen nach unten scrollen"
                "Spanish" -> "Desliza hacia abajo para más opciones"
                else -> "Scroll down for more options"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
