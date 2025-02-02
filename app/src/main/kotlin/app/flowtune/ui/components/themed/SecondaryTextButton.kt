package com.abhiram79.flowtune.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abhiram79.flowtune.utils.center
import com.abhiram79.flowtune.utils.disabled
import com.abhiram79.flowtune.utils.medium
import com.abhiram79.flowtune.core.ui.LocalAppearance
import com.abhiram79.flowtune.core.ui.surface
import com.abhiram79.flowtune.core.ui.utils.roundedShape

@Composable
fun SecondaryTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alternative: Boolean = false
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.medium.center.let { if (enabled) it else it.disabled },
        modifier = modifier
            .clip(16.dp.roundedShape)
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (alternative) colorPalette.background0 else colorPalette.surface)
            .padding(all = 8.dp)
            .padding(horizontal = 8.dp)
    )
}
