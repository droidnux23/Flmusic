package com.abhiram79.flowtune.providers.sponsorblock.requests

import com.abhiram79.flowtune.providers.sponsorblock.SponsorBlock
import com.abhiram79.flowtune.providers.sponsorblock.models.Action
import com.abhiram79.flowtune.providers.sponsorblock.models.Category
import com.abhiram79.flowtune.providers.sponsorblock.models.Segment
import com.abhiram79.flowtune.providers.utils.SerializableUUID
import com.abhiram79.flowtune.providers.utils.runCatchingCancellable
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

suspend fun SponsorBlock.segments(
    videoId: String,
    categories: List<Category>? = listOf(Category.Sponsor, Category.OfftopicMusic, Category.PoiHighlight),
    actions: List<Action>? = listOf(Action.Skip, Action.POI),
    segments: List<SerializableUUID>? = null
) = runCatchingCancellable {
    httpClient.get("/api/skipSegments") {
        parameter("videoID", videoId)
        if (!categories.isNullOrEmpty()) categories.forEach { parameter("category", it.serialName) }
        if (!actions.isNullOrEmpty()) actions.forEach { parameter("action", it.serialName) }
        if (!segments.isNullOrEmpty()) segments.forEach { parameter("requiredSegment", it) }
        parameter("service", "YouTube")
    }.body<List<Segment>>()
}
