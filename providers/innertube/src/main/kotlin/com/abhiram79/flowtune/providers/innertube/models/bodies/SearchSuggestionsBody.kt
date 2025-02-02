package com.abhiram79.flowtune.providers.innertube.models.bodies

import com.abhiram79.flowtune.providers.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsBody(
    val context: Context = Context.DefaultWeb,
    val input: String
)
