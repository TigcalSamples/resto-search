package com.tigcal.samples.restosearch

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tigcal.samples.restosearch.model.Restaurant
import com.tigcal.samples.restosearch.network.MapRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: MapRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {

    private val _restaurants = MutableStateFlow(emptyList<Restaurant>())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    var lastKnownLocation: Location? = null

    fun searchNearbyRestaurants(keyword: String, location: Location) {
        lastKnownLocation = location

        val latLong = "${location.latitude},${location.longitude}"
        viewModelScope.launch(dispatcher) {
            repository.getNearbyRestaurants(keyword, latLong, 50_000, "restaurant", BuildConfig.MAPS_API_KEY)
                .catch {
                    _error.value = it.message.toString()
                }.collect {
                    _restaurants.value = it
                }
        }
    }
}