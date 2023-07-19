package com.tigcal.samples.restosearch.model

import com.squareup.moshi.Json

data class NutritionixResponse (
    val branded: List<MenuItem>
)

data class MenuItem(
    @field:Json(name = "food_name")
    val name: String,
    @field:Json(name = "nf_calories")
    val calories: String,
    val photo: Photo
)

data class Photo(
    val thumb: String
)