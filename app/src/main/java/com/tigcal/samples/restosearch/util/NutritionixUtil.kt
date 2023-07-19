package com.tigcal.samples.restosearch.util

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tigcal.samples.restosearch.model.NutritionixBrand
import java.io.IOException

object NutritionixUtil {
    var brandType = Types.newParameterizedType(
        MutableList::class.java,
        NutritionixBrand::class.java
    )

    fun getBrands(context: Context, file: String): List<NutritionixBrand> {
        val string = getJson(context, file)
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<List<NutritionixBrand>> = moshi.adapter(brandType)

        val brands = adapter.fromJson(string)

        return brands ?: emptyList()
    }

    private fun getJson(context: Context, jsonFile: String): String {
        return try {
            val inputStream = context.assets.open(jsonFile)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e("NutritionixUtil", "IOException in getJson: " + e.localizedMessage)
            ""
        }
    }
}