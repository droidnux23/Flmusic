package com.abhiram79.flowtune.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.abhiram79.flowtune.R
import com.abhiram79.flowtune.models.Mood
import com.abhiram79.flowtune.ui.components.themed.Scaffold
import com.abhiram79.flowtune.ui.screens.GlobalRoutes
import com.abhiram79.flowtune.ui.screens.Route
import app.vitune.compose.persist.PersistMapCleanup
import app.vitune.compose.routing.RouteHandler

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
