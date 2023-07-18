package com.tigcal.samples.restosearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.tigcal.samples.restosearch.model.MapResponse
import com.tigcal.samples.restosearch.model.Restaurant
import com.tigcal.samples.restosearch.network.MapRepository
import com.tigcal.samples.restosearch.network.MapService
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import java.lang.RuntimeException

@RunWith(MockitoJUnitRunner::class)
class MapRepositoryTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun getNearbyRestaurants() {
        val restaurants = listOf(Restaurant(id = "A"), Restaurant(id = "B"))
        val response = MapResponse(restaurants = restaurants)

        val service: MapService = mock {
            onBlocking {
                getNearbyRestaurants(
                    anyString(), anyString(), anyInt(), anyString(), anyString()
                )
            } doReturn
                    response
        }

        val repository = MapRepository(service)

        runTest {
            repository.getNearbyRestaurants("query", "loc", 100, "type", "key").test {
                assertEquals(restaurants, awaitItem())
                awaitComplete()
            }
        }
    }

    @Test
    fun getNearbyRestaurantsError() {
        val exception = "Test Exception"

        val service: MapService = mock {
            onBlocking {
                getNearbyRestaurants(
                    anyString(), anyString(), anyInt(), anyString(), anyString()
                )
            } doThrow
                    RuntimeException(exception)
        }

        val repository = MapRepository(service)

        runTest {
            repository.getNearbyRestaurants("query", "loc", 100, "type", "key").test {
                assertEquals(exception, awaitError().message)
            }
        }
    }
}