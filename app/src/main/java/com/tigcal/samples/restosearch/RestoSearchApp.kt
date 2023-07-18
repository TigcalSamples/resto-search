package com.tigcal.samples.restosearch

import android.app.Application
import com.tigcal.samples.restosearch.network.MapRepository
import com.tigcal.samples.restosearch.network.MapService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RestoSearchApp: Application() {

    lateinit var mapRepository: MapRepository

    override fun onCreate() {
        super.onCreate()

        val mapRetrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val mapService = mapRetrofit.create(MapService::class.java)
        mapRepository = MapRepository(mapService)
    }
}