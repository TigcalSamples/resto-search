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
    val rating: Float = 0f,
    val vicinity: String = "",
    val geometry: Geometry
)

data class Geometry(
    val location: GeometryLocation
)

data class GeometryLocation(
    val lat: Float = 0f,
    val lng: Float = 0f
)

val Restaurant.latLng: Pair<Double, Double>
    get() = geometry.location.lat.toDouble() to geometry.location.lng.toDouble()