package app.flowtune.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import app.flowtune.R
import app.flowtune.models.toUiMood
import app.flowtune.ui.components.themed.Scaffold
import app.flowtune.ui.screens.GlobalRoutes
import app.flowtune.ui.screens.Route
import app.flowtune.ui.screens.moodRoute
import app.flowtune.persist.PersistMapCleanup
import app.flowtune.routing.RouteHandler

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
