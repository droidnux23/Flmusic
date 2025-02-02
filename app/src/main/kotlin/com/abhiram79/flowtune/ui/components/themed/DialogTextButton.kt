package com.abhiram79.flowtune.compose.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abhiram79.flowtune.compose.utils.disabled
import com.abhiram79.flowtune.compose.utils.medium
import com.abhiram79.flowtune.compose.utils.primary
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.utils.roundedShape

@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xs.medium.let {
            when {
                !enabled -> it.disabled
                primary -> it.primary
                else -> it
            }
        },
        modifier = modifier
            .clip(36.dp.roundedShape)
            .background(if (primary) colorPalette.accent else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}
