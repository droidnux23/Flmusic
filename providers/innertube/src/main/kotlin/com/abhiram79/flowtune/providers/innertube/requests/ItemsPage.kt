package app.flowtune.providers.innertube.requests

import app.flowtune.providers.innertube.Innertube
import app.flowtune.providers.innertube.models.BrowseResponse
import app.flowtune.providers.innertube.models.ContinuationResponse
import app.flowtune.providers.innertube.models.GridRenderer
import app.flowtune.providers.innertube.models.MusicResponsiveListItemRenderer
import app.flowtune.providers.innertube.models.MusicShelfRenderer
import app.flowtune.providers.innertube.models.MusicTwoRowItemRenderer
import app.flowtune.providers.innertube.models.bodies.BrowseBody
import app.flowtune.providers.innertube.models.bodies.ContinuationBody
import app.flowtune.providers.utils.runCatchingCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun <T : Innertube.Item> Innertube.itemsPage(
    body: BrowseBody,
    fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
    fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T? = { null }
) = runCatchingCancellable {
    val response = client.post(BROWSE) {
        setBody(body)
    }.body<BrowseResponse>()

    val sectionListRendererContent = response
        .contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents
        ?.firstOrNull()

    itemsPageFromMusicShelRendererOrGridRenderer(
        musicShelfRenderer = sectionListRendererContent
            ?.musicShelfRenderer,
        gridRenderer = sectionListRendererContent
            ?.gridRenderer,
        fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
        fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer
    )
}

suspend fun <T : Innertube.Item> Innertube.itemsPage(
    body: ContinuationBody,
    fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
    fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T? = { null }
) = runCatchingCancellable {
    val response = client.post(BROWSE) {
        setBody(body)
    }.body<ContinuationResponse>()

    itemsPageFromMusicShelRendererOrGridRenderer(
        musicShelfRenderer = response
            .continuationContents
            ?.musicShelfContinuation,
        gridRenderer = null,
        fromMusicResponsiveListItemRenderer = fromMusicResponsiveListItemRenderer,
        fromMusicTwoRowItemRenderer = fromMusicTwoRowItemRenderer
    )
}

private fun <T : Innertube.Item> itemsPageFromMusicShelRendererOrGridRenderer(
    musicShelfRenderer: MusicShelfRenderer?,
    gridRenderer: GridRenderer?,
    fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T?,
    fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T?
) = when {
    musicShelfRenderer != null -> Innertube.ItemsPage(
        continuation = musicShelfRenderer
            .continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation,
        items = musicShelfRenderer
            .contents
            ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
            ?.mapNotNull(fromMusicResponsiveListItemRenderer)
    )

    gridRenderer != null -> Innertube.ItemsPage(
        continuation = null,
        items = gridRenderer
            .items
            ?.mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
            ?.mapNotNull(fromMusicTwoRowItemRenderer)
    )

    else -> null
}
