package app.flowtune.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import app.flowtune.R
import app.flowtune.ui.components.themed.Scaffold
import app.flowtune.ui.screens.GlobalRoutes
import app.flowtune.ui.screens.Route
import app.flowtune.ui.screens.albumRoute
import app.flowtune.persist.PersistMapCleanup
import app.flowtune.routing.RouteHandler

@Route
@Composable
fun MoreAlbumsScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "more_albums/")

    RouteHandler {
        GlobalRoutes()

        Content {
            Scaffold(
                key = "morealbums",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.albums, R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> MoreAlbumsList(
                            onAlbumClick = { albumRoute(it) }
                        )
                    }
                }
            }
        }
    }
}
