package com.tigcal.samples.restosearch.network

import com.tigcal.samples.restosearch.BuildConfig
import com.tigcal.samples.restosearch.model.NutritionixResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NutritionixService {

    @Headers(
        "x-app-id: ${BuildConfig.NUTRITIONIX_APP_ID}",
        "x-app-key: ${BuildConfig.NUTRITIONIX_API_KEY}"
    )
    @GET("search/instant")
    suspend fun getMenuItems(
        @Query("query") query: String,
        @Query("branded") branded: Boolean,
        @Query("brand_ids") brand_ids: String
    ): NutritionixResponse
}
