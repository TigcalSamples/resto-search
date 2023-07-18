package com.tigcal.samples.restosearch.network

import com.tigcal.samples.restosearch.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MapRepository(private val mapService: MapService)  {

    fun getNearbyRestaurants(
        keyword: String,
        location: String,
        radius: Int,
        type: String,
        key: String
    ): Flow<List<Restaurant>> {
        return flow {
            val response = mapService.getNearbyRestaurants(keyword, location, radius, type, key)
            emit(response.restaurants)
        }
    }

}