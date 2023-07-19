package com.tigcal.samples.restosearch

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RestaurantActivity : AppCompatActivity() {
    private val nameText: TextView by lazy { findViewById(R.id.name_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        nameText.text =  name
        val brandId = intent.getStringExtra(EXTRA_BRAND_ID) ?: ""

        val nutritionixRepository = (application as RestoSearchApp).nutritionixRepository
        val nutritionixViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NutritionixViewModel(nutritionixRepository) as T
            }
        })[NutritionixViewModel::class.java]
        nutritionixViewModel.getMenuItems(name, brandId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    nutritionixViewModel.menuItems.collect {
                        Log.d("NutritionixViewModel",  "menu items: $it")
                    }
                }
                launch {
                    nutritionixViewModel.error.collect { message ->
                        if (message.isNotEmpty()) {
                            displayErrorMessage(message)
                        }
                    }
                }
            }
        }
    }

    private fun displayErrorMessage(message: String) {
        Snackbar.make(nameText, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_BRAND_ID = "com.tigcal.samples.restosearch.resto.brandid"
        const val EXTRA_NAME = "com.tigcal.samples.restosearch.resto"
    }
}