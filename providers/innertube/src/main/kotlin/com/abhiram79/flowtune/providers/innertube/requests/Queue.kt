package com.abhiram79.flowtune.providers.innertube.requests

import com.abhiram79.flowtune.providers.innertube.Innertube
import com.abhiram79.flowtune.providers.innertube.models.GetQueueResponse
import com.abhiram79.flowtune.providers.innertube.models.bodies.QueueBody
import com.abhiram79.flowtune.providers.innertube.utils.from
import com.abhiram79.flowtune.providers.utils.runCatchingCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun Innertube.queue(body: QueueBody) = runCatchingCancellable {
    val response = client.post(QUEUE) {
        setBody(body)
        mask("queueDatas.content.$PLAYLIST_PANEL_VIDEO_RENDERER_MASK")
    }.body<GetQueueResponse>()

    response
        .queueData
        ?.mapNotNull { queueData ->
            queueData
                .content
                ?.playlistPanelVideoRenderer
                ?.let(Innertube.SongItem::from)
        }
}

suspend fun Innertube.song(videoId: String): Result<Innertube.SongItem?>? =
    queue(QueueBody(videoIds = listOf(videoId)))?.map { it?.firstOrNull() }
