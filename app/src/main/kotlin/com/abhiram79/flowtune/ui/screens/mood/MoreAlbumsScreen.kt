package com.abhiram79.flowtune.compose.ui.screens.mood

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.abhiram79.flowtune.compose.R
import com.abhiram79.flowtune.compose.ui.components.themed.Scaffold
import com.abhiram79.flowtune.compose.ui.screens.GlobalRoutes
import com.abhiram79.flowtune.compose.ui.screens.Route
import com.abhiram79.flowtune.compose.ui.screens.albumRoute
import com.abhiram79.flowtune.compose.persist.PersistMapCleanup
import com.abhiram79.flowtune.compose.routing.RouteHandler

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
