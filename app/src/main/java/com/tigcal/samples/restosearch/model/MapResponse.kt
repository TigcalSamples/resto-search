package com.tigcal.samples.restosearch.model

import com.squareup.moshi.Json

data class MapResponse(
    @field:Json(name = "results")
    val restaurants: List<Restaurant>,
    val status: String = ""
)

data class Restaurant(
    @field:Json(name = "place_id")
    val id: String = "",
    val icon: String = "",
    val name: String = "",
    val rating: String = "",
    val vicinity: String = ""
)