package app.flowtune.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import app.flowtune.Database
import app.flowtune.LocalPlayerAwareWindowInsets
import app.flowtune.LocalPlayerServiceBinder
import app.flowtune.R
import app.flowtune.models.Song
import app.flowtune.ui.components.LocalMenuState
import app.flowtune.ui.components.themed.FloatingActionsContainerWithScrollToTop
import app.flowtune.ui.components.themed.Header
import app.flowtune.ui.components.themed.InHistoryMediaItemMenu
import app.flowtune.ui.components.themed.SecondaryTextButton
import app.flowtune.ui.items.SongItem
import app.flowtune.utils.align
import app.flowtune.utils.asMediaItem
import app.flowtune.utils.forcePlay
import app.flowtune.utils.medium
import app.flowtune.utils.playingSong
import app.flowtune.persist.persistList
import app.flowtune.core.ui.Dimensions
import app.flowtune.core.ui.LocalAppearance
import app.flowtune.providers.innertube.models.NavigationEndpoint
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalSongSearch(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var items by persistList<Song>("search/local/songs")

    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.length > 1)
            Database
                .search("%${textFieldValue.text}%")
                .collect { items = it.toImmutableList() }
    }

    val lazyListState = rememberLazyListState()

    val (currentMediaId, playing) = playingSong(binder)

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    titleContent = {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = typography.xxl.medium.align(TextAlign.End),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(colorPalette.text),
                            decorationBox = decorationBox
                        )
                    },
                    actionsContent = {
                        if (textFieldValue.text.isNotEmpty()) SecondaryTextButton(
                            text = stringResource(R.string.clear),
                            onClick = { onTextFieldValueChange(TextFieldValue()) }
                        )
                    }
                )
            }

            items(
                items = items,
                key = Song::id
            ) { song ->
                SongItem(
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                val mediaItem = song.asMediaItem
                                binder?.stopRadio()
                                binder?.player?.forcePlay(mediaItem)
                                binder?.setupRadio(
                                    NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                )
                            }
                        )
                        .animateItem(),
                    song = song,
                    thumbnailSize = Dimensions.thumbnails.song,
                    isPlaying = playing && currentMediaId == song.id // TODO: refactor out a simple 'song list' in order to fix this kind of repetition
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
}
