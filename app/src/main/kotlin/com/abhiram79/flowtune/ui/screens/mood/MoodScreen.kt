package com.abhiram79.flowtune.compose.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.abhiram79.flowtune.compose.R
import com.abhiram79.flowtune.compose.models.Mood
import com.abhiram79.flowtune.compose.ui.components.themed.Scaffold
import com.abhiram79.flowtune.compose.ui.screens.GlobalRoutes
import com.abhiram79.flowtune.compose.ui.screens.Route
import com.abhiram79.flowtune.compose.persist.PersistMapCleanup
import com.abhiram79.flowtune.compose.routing.RouteHandler

@Route
@Composable
fun MoodScreen(mood: Mood) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "playlist/mood/")

    RouteHandler {
        GlobalRoutes()

        Content {
            Scaffold(
                key = "mood",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.mood, R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoodList(mood = mood)
                    }
                }
            }
        }
    }
}
