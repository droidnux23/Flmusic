package com.abhiram79.flowtune.compose.ui.screens.localplaylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abhiram79.flowtune.compose.Database
import com.abhiram79.flowtune.compose.LocalPlayerAwareWindowInsets
import com.abhiram79.flowtune.compose.LocalPlayerServiceBinder
import com.abhiram79.flowtune.compose.R
import com.abhiram79.flowtune.compose.models.Playlist
import com.abhiram79.flowtune.compose.models.Song
import com.abhiram79.flowtune.compose.models.SongPlaylistMap
import com.abhiram79.flowtune.compose.preferences.DataPreferences
import com.abhiram79.flowtune.compose.query
import com.abhiram79.flowtune.compose.transaction
import com.abhiram79.flowtune.compose.ui.components.LocalMenuState
import com.abhiram79.flowtune.compose.ui.components.themed.CircularProgressIndicator
import com.abhiram79.flowtune.compose.ui.components.themed.ConfirmationDialog
import com.abhiram79.flowtune.compose.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.abhiram79.flowtune.compose.ui.components.themed.Header
import com.abhiram79.flowtune.compose.ui.components.themed.HeaderIconButton
import com.abhiram79.flowtune.compose.ui.components.themed.InPlaylistMediaItemMenu
import com.abhiram79.flowtune.compose.ui.components.themed.LayoutWithAdaptiveThumbnail
import com.abhiram79.flowtune.compose.ui.components.themed.Menu
import com.abhiram79.flowtune.compose.ui.components.themed.MenuEntry
import com.abhiram79.flowtune.compose.ui.components.themed.ReorderHandle
import com.abhiram79.flowtune.compose.ui.components.themed.SecondaryTextButton
import com.abhiram79.flowtune.compose.ui.components.themed.TextFieldDialog
import com.abhiram79.flowtune.compose.ui.items.SongItem
import com.abhiram79.flowtune.compose.utils.PlaylistDownloadIcon
import com.abhiram79.flowtune.compose.utils.asMediaItem
import com.abhiram79.flowtune.compose.utils.completed
import com.abhiram79.flowtune.compose.utils.enqueue
import com.abhiram79.flowtune.compose.utils.forcePlayAtIndex
import com.abhiram79.flowtune.compose.utils.forcePlayFromBeginning
import com.abhiram79.flowtune.compose.utils.launchYouTubeMusic
import com.abhiram79.flowtune.compose.utils.playingSong
import com.abhiram79.flowtune.compose.utils.toast
import com.abhiram79.flowtune.compose.reordering.animateItemPlacement
import com.abhiram79.flowtune.compose.reordering.draggedItem
import com.abhiram79.flowtune.compose.reordering.rememberReorderingState
import app.vitune.core.ui.Dimensions
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.utils.isLandscape
import app.vitune.providers.innertube.Innertube
import app.vitune.providers.innertube.models.bodies.BrowseBody
import app.vitune.providers.innertube.requests.playlistPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlaylistSongs(
    playlist: Playlist,
    songs: ImmutableList<Song>,
    onDelete: () -> Unit,
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) = LayoutWithAdaptiveThumbnail(
    thumbnailContent = thumbnailContent,
    modifier = modifier
) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (DataPreferences.autoSyncPlaylists) playlist.browseId?.let { browseId ->
            loading = true
            sync(playlist, browseId)
            loading = false
        }
    }

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = songs,
        onDragEnd = { fromIndex, toIndex ->
            transaction {
                Database.move(playlist.id, fromIndex, toIndex)
            }
        },
        extraItemCount = 1
    )

    var isRenaming by rememberSaveable { mutableStateOf(false) }

    if (isRenaming) TextFieldDialog(
        hintText = stringResource(R.string.enter_playlist_name_prompt),
        initialTextInput = playlist.name,
        onDismiss = { isRenaming = false },
        onAccept = { text ->
            query {
                Database.update(playlist.copy(name = text))
            }
        }
    )

    var isDeleting by rememberSaveable { mutableStateOf(false) }

    if (isDeleting) ConfirmationDialog(
        text = stringResource(R.string.confirm_delete_playlist),
        onDismiss = { isDeleting = false },
        onConfirm = {
            query {
                Database.delete(playlist)
            }
            onDelete()
        }
    )

    val (currentMediaId, playing) = playingSong(binder)

    Box {
        LookaheadScope {
            LazyColumn(
                state = reorderingState.lazyListState,
                contentPadding = LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues(),
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                item(
                    key = "header",
                    contentType = 0
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Header(
                            title = playlist.name,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            SecondaryTextButton(
                                text = stringResource(R.string.enqueue),
                                enabled = songs.isNotEmpty(),
                                onClick = {
                                    binder?.player?.enqueue(songs.map { it.asMediaItem })
                                }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            AnimatedVisibility(loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            }

                            PlaylistDownloadIcon(
                                songs = songs.map { it.asMediaItem }.toImmutableList()
                            )

                            HeaderIconButton(
                                icon = R.drawable.ellipsis_horizontal,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        Menu {
                                            playlist.browseId?.let { browseId ->
                                                MenuEntry(
                                                    icon = R.drawable.sync,
                                                    text = stringResource(R.string.sync),
                                                    enabled = !loading,
                                                    onClick = {
                                                        menuState.hide()
                                                        coroutineScope.launch {
                                                            loading = true
                                                            sync(playlist, browseId)
                                                            loading = false
                                                        }
                                                    }
                                                )

                                                songs.firstOrNull()?.id?.let { firstSongId ->
                                                    MenuEntry(
                                                        icon = R.drawable.play,
                                                        text = stringResource(R.string.watch_playlist_on_youtube),
                                                        onClick = {
                                                            menuState.hide()
                                                            binder?.player?.pause()
                                                            uriHandler.openUri(
                                                                "https://youtube.com/watch?v=$firstSongId&list=${
                                                                    playlist.browseId.drop(2)
                                                                }"
                                                            )
                                                        }
                                                    )

                                                    MenuEntry(
                                                        icon = R.drawable.musical_notes,
                                                        text = stringResource(R.string.open_in_youtube_music),
                                                        onClick = {
                                                            menuState.hide()
                                                            binder?.player?.pause()
                                                            if (
                                                                !launchYouTubeMusic(
                                                                    context = context,
                                                                    endpoint = "watch?v=$firstSongId&list=${
                                                                        playlist.browseId.drop(2)
                                                                    }"
                                                                )
                                                            ) context.toast(
                                                                context.getString(R.string.youtube_music_not_installed)
                                                            )
                                                        }
                                                    )
                                                }
                                            }

                                            MenuEntry(
                                                icon = R.drawable.pencil,
                                                text = stringResource(R.string.rename),
                                                onClick = {
                                                    menuState.hide()
                                                    isRenaming = true
                                                }
                                            )

                                            MenuEntry(
                                                icon = R.drawable.trash,
                                                text = stringResource(R.string.delete),
                                                onClick = {
                                                    menuState.hide()
                                                    isDeleting = true
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        if (!isLandscape) thumbnailContent()
                    }
                }

                itemsIndexed(
                    items = songs,
                    key = { _, song -> song.id },
                    contentType = { _, song -> song }
                ) { index, song ->
                    SongItem(
                        modifier = Modifier
                            .combinedClickable(
                                onLongClick = {
                                    menuState.display {
                                        InPlaylistMediaItemMenu(
                                            playlistId = playlist.id,
                                            positionInPlaylist = index,
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                },
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.player?.forcePlayAtIndex(
                                        items = songs.map { it.asMediaItem },
                                        index = index
                                    )
                                }
                            )
                            .animateItemPlacement(reorderingState)
                            .draggedItem(
                                reorderingState = reorderingState,
                                index = index
                            )
                            .background(colorPalette.background0),
                        song = song,
                        thumbnailSize = Dimensions.thumbnails.song,
                        trailingContent = {
                            ReorderHandle(
                                reorderingState = reorderingState,
                                index = index
                            )
                        },
                        clip = !reorderingState.isDragging,
                        isPlaying = playing && currentMediaId == song.id
                    )
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            icon = R.drawable.shuffle,
            visible = !reorderingState.isDragging,
            onClick = {
                if (songs.isEmpty()) return@FloatingActionsContainerWithScrollToTop

                binder?.stopRadio()
                binder?.player?.forcePlayFromBeginning(
                    songs.shuffled().map { it.asMediaItem }
                )
            }
        )
    }
}

private suspend fun sync(
    playlist: Playlist,
    browseId: String
) = runCatching {
    Innertube.playlistPage(
        BrowseBody(browseId = browseId)
    )?.completed()?.getOrNull()?.let { remotePlaylist ->
        transaction {
            Database.clearPlaylist(playlist.id)

            remotePlaylist.songsPage
                ?.items
                ?.map { it.asMediaItem }
                ?.onEach { Database.insert(it) }
                ?.mapIndexed { position, mediaItem ->
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = playlist.id,
                        position = position
                    )
                }
                ?.let(Database::insertSongPlaylistMaps)
        }
    }
}.onFailure {
    if (it is CancellationException) throw it
    it.printStackTrace()
}
