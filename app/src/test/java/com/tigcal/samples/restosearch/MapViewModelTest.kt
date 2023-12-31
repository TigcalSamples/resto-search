package com.tigcal.samples.restosearch

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.tigcal.samples.restosearch.model.Geometry
import com.tigcal.samples.restosearch.model.GeometryLocation
import com.tigcal.samples.restosearch.model.Restaurant
import com.tigcal.samples.restosearch.network.MapRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.lang.RuntimeException

@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun getNearbyRestaurants() {
        val dispatcher = StandardTestDispatcher()
        val geometry = Geometry(GeometryLocation(8f, 9f))
        val restaurants = listOf(Restaurant(id = "1", geometry=geometry),
            Restaurant(id = "2", geometry=geometry))

        val repository: MapRepository = mock {
            onBlocking {
                getNearbyRestaurants(
                    anyString(), anyString(), anyInt(), anyString(), anyString()
                )
            } doReturn
                    flowOf(restaurants)
        }
        val location: Location = mock()

        val viewModel = MapViewModel(repository, dispatcher)
        viewModel.searchNearbyRestaurants("", location)

        runTest {
            dispatcher.scheduler.advanceUntilIdle()
            viewModel.restaurants.test {
                assertEquals(restaurants, awaitItem())
            }
        }
    }

    @Test
    fun getNearbyRestaurantsError() {
        val exception = "Test Exception"
        val dispatcher = StandardTestDispatcher()

        val repository: MapRepository = mock {
            onBlocking {
                getNearbyRestaurants(
                    anyString(), anyString(), anyInt(), anyString(), anyString()
                )
            } doReturn
                    flow { throw RuntimeException(exception) }
        }

        val viewModel = MapViewModel(repository, dispatcher)
        val location: Location = mock()
        viewModel.searchNearbyRestaurants("", location)

        runTest {
            dispatcher.scheduler.advanceUntilIdle()
            viewModel.error.test {
                assertEquals(exception, awaitItem())
            }
        }
    }
}