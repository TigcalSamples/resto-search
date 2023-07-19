package com.tigcal.samples.restosearch

import androidx.lifecycle.ViewModel
import com.tigcal.samples.restosearch.model.NutritionixBrand

class NutritionixViewModel: ViewModel() {
    var nutritionixBrands = emptyList<NutritionixBrand>()

    fun getBrandId(restaurant: String): String {
        return nutritionixBrands.find { it.name == restaurant }?.id ?: ""
    }
}