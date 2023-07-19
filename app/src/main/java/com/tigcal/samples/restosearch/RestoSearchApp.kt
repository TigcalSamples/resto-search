package com.tigcal.samples.restosearch

import android.app.Application
import com.tigcal.samples.restosearch.network.MapRepository
import com.tigcal.samples.restosearch.network.MapService
import com.tigcal.samples.restosearch.network.NutritionixRepository
import com.tigcal.samples.restosearch.network.NutritionixService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class RestoSearchApp: Application() {

    lateinit var mapRepository: MapRepository
    lateinit var nutritionixRepository: NutritionixRepository

    override fun onCreate() {
        super.onCreate()

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val mapRetrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val mapService = mapRetrofit.create(MapService::class.java)
        mapRepository = MapRepository(mapService)

        val nutritionixRetrofit = Retrofit.Builder()
            .baseUrl("https://trackapi.nutritionix.com/v2/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val nutritionixService = nutritionixRetrofit.create(NutritionixService::class.java)
        nutritionixRepository = NutritionixRepository(nutritionixService)
    }
}