package com.abhiram79.flowtune.providers.innertube.models.bodies

import com.abhiram79.flowtune.providers.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String
)
