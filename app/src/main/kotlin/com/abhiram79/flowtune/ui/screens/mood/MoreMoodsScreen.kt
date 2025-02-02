package com.abhiram79.flowtune.compose.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.abhiram79.flowtune.compose.R
import com.abhiram79.flowtune.compose.models.toUiMood
import com.abhiram79.flowtune.compose.ui.components.themed.Scaffold
import com.abhiram79.flowtune.compose.ui.screens.GlobalRoutes
import com.abhiram79.flowtune.compose.ui.screens.Route
import com.abhiram79.flowtune.compose.ui.screens.moodRoute
import com.abhiram79.flowtune.compose.persist.PersistMapCleanup
import com.abhiram79.flowtune.compose.routing.RouteHandler

@Route
@Composable
fun MoreMoodsScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "more_moods/")

    RouteHandler {
        GlobalRoutes()

        moodRoute { mood ->
            MoodScreen(mood = mood)
        }

        Content {
            Scaffold(
                key = "moremoods",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.moods_and_genres, R.drawable.playlist)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoreMoodsList(
                            onMoodClick = { mood -> moodRoute(mood.toUiMood()) }
                        )
                    }
                }
            }
        }
    }
}
