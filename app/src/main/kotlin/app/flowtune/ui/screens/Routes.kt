package app.flowtune.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import app.flowtune.Database
import app.flowtune.LocalPlayerServiceBinder
import app.flowtune.R
import app.flowtune.handleUrl
import app.flowtune.models.Mood
import app.flowtune.models.SearchQuery
import app.flowtune.preferences.DataPreferences
import app.flowtune.query
import app.flowtune.ui.screens.album.AlbumScreen
import app.flowtune.ui.screens.artist.ArtistScreen
import app.flowtune.ui.screens.pipedplaylist.PipedPlaylistScreen
import app.flowtune.ui.screens.playlist.PlaylistScreen
import app.flowtune.ui.screens.search.SearchScreen
import app.flowtune.ui.screens.searchresult.SearchResultScreen
import app.flowtune.ui.screens.settings.LogsScreen
import app.flowtune.ui.screens.settings.SettingsScreen
import app.flowtune.utils.toast
import app.flowtune.routing.Route0
import app.flowtune.routing.Route1
import app.flowtune.routing.Route3
import app.flowtune.routing.Route4
import app.flowtune.routing.RouteHandlerScope
import app.flowtune.core.data.enums.BuiltInPlaylist
import io.ktor.http.Url
import java.util.UUID

/**
 * Marker class for linters that a composable is a route and should not be handled like a regular
 * composable, but rather as an entrypoint.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Route

val albumRoute = Route1<String>("albumRoute")
val artistRoute = Route1<String>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val localPlaylistRoute = Route1<Long>("localPlaylistRoute")
val logsRoute = Route0("logsRoute")
val pipedPlaylistRoute = Route3<String, String, String>("pipedPlaylistRoute")
val playlistRoute = Route4<String, String?, Int?, Boolean>("playlistRoute")
val moodRoute = Route1<Mood>("moodRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@Composable
fun RouteHandlerScope.GlobalRoutes() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current

    albumRoute { browseId ->
        AlbumScreen(browseId = browseId)
    }

    artistRoute { browseId ->
        ArtistScreen(browseId = browseId)
    }

    logsRoute {
        LogsScreen()
    }

    pipedPlaylistRoute { apiBaseUrl, sessionToken, playlistId ->
        PipedPlaylistScreen(
            apiBaseUrl = runCatching { Url(apiBaseUrl) }.getOrNull()
                ?: error("Invalid apiBaseUrl: $apiBaseUrl is not a valid Url"),
            sessionToken = sessionToken,
            playlistId = runCatching {
                UUID.fromString(playlistId)
            }.getOrNull() ?: error("Invalid playlistId: $playlistId is not a valid UUID")
        )
    }

    playlistRoute { browseId, params, maxDepth, shouldDedup ->
        PlaylistScreen(
            browseId = browseId,
            params = params,
            maxDepth = maxDepth,
            shouldDedup = shouldDedup
        )
    }

    settingsRoute {
        SettingsScreen()
    }

    searchRoute { initialTextInput ->
        SearchScreen(
            initialTextInput = initialTextInput,
            onSearch = { query ->
                searchResultRoute(query)

                if (!DataPreferences.pauseSearchHistory) query {
                    Database.insert(SearchQuery(query = query))
                }
            },
            onViewPlaylist = { url ->
                with(context) {
                    runCatching {
                        handleUrl(url.toUri(), binder)
                    }.onFailure {
                        toast(getString(R.string.error_url, url))
                    }
                }
            }
        )
    }

    searchResultRoute { query ->
        SearchResultScreen(
            query = query,
            onSearchAgain = { searchRoute(query) }
        )
    }
}
