package com.tigcal.samples.restosearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tigcal.samples.restosearch.model.MenuItem
import com.tigcal.samples.restosearch.model.NutritionixBrand
import com.tigcal.samples.restosearch.model.Restaurant
import com.tigcal.samples.restosearch.network.NutritionixRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NutritionixViewModel(
    private val repository: NutritionixRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {
    var nutritionixBrands = emptyList<NutritionixBrand>()

    fun getBrandId(restaurant: String): String {
        return nutritionixBrands.find {
             it.name.uppercase() == restaurant.uppercase()
        }?.id ?: ""
    }

    private val _menuItems = MutableStateFlow(emptyList<MenuItem>())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun getMenuItems(query: String, brandIds: String, ) {
        viewModelScope.launch(dispatcher) {
            repository.getMenuItems(query, brandIds, true)
                .catch {
                    _error.value = it.message.toString()
                }.collect {
                    _menuItems.value = it
                }
        }
    }
}