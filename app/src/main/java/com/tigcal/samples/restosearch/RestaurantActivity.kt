package com.tigcal.samples.restosearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class RestaurantActivity : AppCompatActivity() {
    private val nameText: TextView by lazy { findViewById(R.id.name_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        nameText.text =  intent.getStringExtra(EXTRA_NAME) ?: ""
    }

    companion object {
        const val EXTRA_NAME = "com.tigcal.samples.restosearch.resto"
    }
}