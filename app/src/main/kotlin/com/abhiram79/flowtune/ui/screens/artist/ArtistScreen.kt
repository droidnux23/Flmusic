package com.abhiram79.flowtune.ui.screens.artist

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.abhiram79.flowtune.Database
import com.abhiram79.flowtune.LocalPlayerServiceBinder
import com.abhiram79.flowtune.R
import com.abhiram79.flowtune.models.Artist
import com.abhiram79.flowtune.preferences.UIStatePreferences
import com.abhiram79.flowtune.preferences.UIStatePreferences.artistScreenTabIndexProperty
import com.abhiram79.flowtune.query
import com.abhiram79.flowtune.ui.components.LocalMenuState
import com.abhiram79.flowtune.ui.components.themed.Header
import com.abhiram79.flowtune.ui.components.themed.HeaderIconButton
import com.abhiram79.flowtune.ui.components.themed.HeaderPlaceholder
import com.abhiram79.flowtune.ui.components.themed.NonQueuedMediaItemMenu
import com.abhiram79.flowtune.ui.components.themed.Scaffold
import com.abhiram79.flowtune.ui.components.themed.adaptiveThumbnailContent
import com.abhiram79.flowtune.ui.items.AlbumItem
import com.abhiram79.flowtune.ui.items.AlbumItemPlaceholder
import com.abhiram79.flowtune.ui.items.SongItem
import com.abhiram79.flowtune.ui.items.SongItemPlaceholder
import com.abhiram79.flowtune.ui.screens.GlobalRoutes
import com.abhiram79.flowtune.ui.screens.Route
import com.abhiram79.flowtune.ui.screens.albumRoute
import com.abhiram79.flowtune.ui.screens.searchresult.ItemsPage
import com.abhiram79.flowtune.utils.asMediaItem
import com.abhiram79.flowtune.utils.forcePlay
import com.abhiram79.flowtune.utils.playingSong
import app.vitune.compose.persist.PersistMapCleanup
import app.vitune.compose.persist.persist
import app.vitune.compose.routing.RouteHandler
import app.vitune.core.ui.Dimensions
import app.vitune.core.ui.LocalAppearance
import app.vitune.providers.innertube.Innertube
import app.vitune.providers.innertube.models.bodies.BrowseBody
import app.vitune.providers.innertube.models.bodies.ContinuationBody
import app.vitune.providers.innertube.requests.artistPage
import app.vitune.providers.innertube.requests.itemsPage
import app.vitune.providers.innertube.utils.from
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun ArtistScreen(browseId: String) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "artist/$browseId/")

    var artist by persist<Artist?>("artist/$browseId/artist")

    var artistPage by persist<Innertube.ArtistPage?>("artist/$browseId/artistPage")

    LaunchedEffect(Unit) {
        Database
            .artist(browseId)
            .combine(
                flow = artistScreenTabIndexProperty.stateFlow.map { it != 4 },
                transform = ::Pair
            )
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch))
                    withContext(Dispatchers.IO) {
                        Innertube.artistPage(BrowseBody(browseId = browseId))
                            ?.onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.name,
                                        thumbnailUrl = currentArtistPage.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt
                                    )
                                )
                            }
                    }
            }
    }

    RouteHandler {
        GlobalRoutes()

        Content {
            val (currentMediaId, playing) = playingSong(binder)

            val thumbnailContent = adaptiveThumbnailContent(
                isLoading = artist?.timestamp == null,
                url = artist?.thumbnailUrl,
                shape = CircleShape
            )

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (artist?.timestamp == null) HeaderPlaceholder(modifier = Modifier.shimmer()) else {
                        val (colorPalette) = LocalAppearance.current
                        val context = LocalContext.current

                        Header(title = artist?.name ?: stringResource(R.string.unknown)) {
                            textButton?.invoke()

                            Spacer(modifier = Modifier.weight(1f))

                            HeaderIconButton(
                                icon = if (artist?.bookmarkedAt == null) R.drawable.bookmark_outline
                                else R.drawable.bookmark,
                                color = colorPalette.accent,
                                onClick = {
                                    val bookmarkedAt =
                                        if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                    query {
                                        artist
                                            ?.copy(bookmarkedAt = bookmarkedAt)
                                            ?.let(Database::update)
                                    }
                                }
                            )

                            HeaderIconButton(
                                icon = R.drawable.share_social,
                                color = colorPalette.text,
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://music.youtube.com/channel/$browseId"
                                        )
                                    }

                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                            )
                        }
                    }
                }

            Scaffold(
                key = "artist",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = UIStatePreferences.artistScreenTabIndex,
                onTabChange = { UIStatePreferences.artistScreenTabIndex = it },
                tabColumnContent = {
                    tab(0, R.string.overview, R.drawable.sparkles)
                    tab(1, R.string.songs, R.drawable.musical_notes)
                    tab(2, R.string.albums, R.drawable.disc)
                    tab(3, R.string.singles, R.drawable.disc)
                    tab(4, R.string.library, R.drawable.library)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> ArtistOverview(
                            youtubeArtistPage = artistPage,
                            thumbnailContent = thumbnailContent,
                            headerContent = headerContent,
                            onAlbumClick = { albumRoute(it) },
                            onViewAllSongsClick = { UIStatePreferences.artistScreenTabIndex = 1 },
                            onViewAllAlbumsClick = { UIStatePreferences.artistScreenTabIndex = 2 },
                            onViewAllSinglesClick = { UIStatePreferences.artistScreenTabIndex = 3 }
                        )

                        1 -> ItemsPage(
                            tag = "artist/$browseId/songs",
                            header = headerContent,
                            provider = artistPage?.let {
                                @Suppress("SpacingAroundCurly")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicResponsiveListItemRenderer = Innertube.SongItem::from
                                        )
                                    } ?: artistPage
                                        ?.songsEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicResponsiveListItemRenderer = Innertube.SongItem::from
                                            )
                                        }
                                    ?: Result.success(
                                        Innertube.ItemsPage(
                                            items = artistPage?.songs,
                                            continuation = null
                                        )
                                    )
                                }
                            },
                            itemContent = { song ->
                                SongItem(
                                    song = song,
                                    thumbnailSize = Dimensions.thumbnails.song,
                                    modifier = Modifier.combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem
                                                )
                                            }
                                        },
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info?.endpoint)
                                        }
                                    ),
                                    isPlaying = playing && currentMediaId == song.key
                                )
                            },
                            itemPlaceholderContent = {
                                SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
                            }
                        )

                        2 -> ItemsPage(
                            tag = "artist/$browseId/albums",
                            header = headerContent,
                            emptyItemsText = stringResource(R.string.artist_has_no_albums),
                            provider = artistPage?.let {
                                @Suppress("SpacingAroundCurly")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                        )
                                    } ?: artistPage
                                        ?.albumsEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                            )
                                        }
                                    ?: Result.success(
                                        Innertube.ItemsPage(
                                            items = artistPage?.albums,
                                            continuation = null
                                        )
                                    )
                                }
                            },
                            itemContent = { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    modifier = Modifier.clickable(onClick = { albumRoute(album.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                            }
                        )

                        3 -> ItemsPage(
                            tag = "artist/$browseId/singles",
                            header = headerContent,
                            emptyItemsText = stringResource(R.string.artist_has_no_singles),
                            provider = artistPage?.let {
                                @Suppress("SpacingAroundCurly")
                                { continuation ->
                                    continuation?.let {
                                        Innertube.itemsPage(
                                            body = ContinuationBody(continuation = continuation),
                                            fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                        )
                                    } ?: artistPage
                                        ?.singlesEndpoint
                                        ?.takeIf { it.browseId != null }
                                        ?.let { endpoint ->
                                            Innertube.itemsPage(
                                                body = BrowseBody(
                                                    browseId = endpoint.browseId!!,
                                                    params = endpoint.params
                                                ),
                                                fromMusicTwoRowItemRenderer = Innertube.AlbumItem::from
                                            )
                                        }
                                    ?: Result.success(
                                        Innertube.ItemsPage(
                                            items = artistPage?.singles,
                                            continuation = null
                                        )
                                    )
                                }
                            },
                            itemContent = { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    modifier = Modifier.clickable(onClick = { albumRoute(album.key) })
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                            }
                        )

                        4 -> ArtistLocalSongs(
                            browseId = browseId,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent
                        )
                    }
                }
            }
        }
    }
}
