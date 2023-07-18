package com.tigcal.samples.restosearch.network

import com.tigcal.samples.restosearch.model.MapResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MapService {

    @GET("nearbysearch/json")
    suspend fun getNearbyRestaurants(
        @Query("keyword") keyword: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): MapResponse
}
