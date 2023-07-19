package com.tigcal.samples.restosearch.network

import com.tigcal.samples.restosearch.model.MenuItem
import com.tigcal.samples.restosearch.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NutritionixRepository(private val service: NutritionixService)  {

    fun getMenuItems(
        query: String,
        brandIds: String,
        branded: Boolean
    ): Flow<List<MenuItem>> {
        return flow {
            val response = service.getMenuItems(query, branded, brandIds)
            emit(response.branded)
        }
    }

}