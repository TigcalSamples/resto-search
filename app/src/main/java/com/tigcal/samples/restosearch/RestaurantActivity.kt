package com.tigcal.samples.restosearch

import android.os.Bundle
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class RestaurantActivity : AppCompatActivity() {
    private val emptyText: TextView by lazy { findViewById(R.id.empty_text) }
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        title =  name

        menuItemAdapter = MenuItemAdapter(this)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = menuItemAdapter
        }

        val brandId = intent.getStringExtra(EXTRA_BRAND_ID) ?: ""
        if (brandId.isEmpty()) {
            emptyText.isVisible = true
        }

        val nutritionixRepository = (application as RestoSearchApp).nutritionixRepository
        val nutritionixViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NutritionixViewModel(nutritionixRepository) as T
            }
        })[NutritionixViewModel::class.java]
        progressBar.isVisible = true
        nutritionixViewModel.getMenuItems(name, brandId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    nutritionixViewModel.menuItems.collect {
                        progressBar.isVisible = false
                        menuItemAdapter.setMenuItems(it)
                        if (it.isEmpty()) emptyText.isVisible = true
                    }
                }
                launch {
                    nutritionixViewModel.error.collect { message ->
                        progressBar.isVisible = false
                        if (message.isNotEmpty()) {
                            displayErrorMessage(message)
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayErrorMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_BRAND_ID = "com.tigcal.samples.restosearch.resto.brandid"
        const val EXTRA_NAME = "com.tigcal.samples.restosearch.resto"
    }
}