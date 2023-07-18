package com.tigcal.samples.restosearch

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.tigcal.samples.restosearch.network.MapRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 0

    private var searchQuery = ""

    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: MapRepository
    private lateinit var viewModel: MapViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        repository = (application as RestoSearchApp).mapRepository
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(repository) as T
            }
        })[MapViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.restaurants.collect {
                        //TODO add to list
                        progressBar.isVisible = false
                        Log.d("Restaurants", "Restaurants: $it")
                    }
                }
                launch {
                    viewModel.error.collect { message ->
                        progressBar.isVisible = false
                        if (message.isNotEmpty()) {
                            displayErrorMessage(message)
                        }
                    }
                }
            }
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
//            adapter =
        }

        val bottomNavigationView: BottomNavigationView? = findViewById(R.id.bottom_navigation)
        bottomNavigationView?.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_list -> {
                    showList()
                    return@OnItemSelectedListener true
                }
                R.id.action_map -> {
                    showMap()
                    return@OnItemSelectedListener true
                }
            }
            false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.action_search)?.actionView as? SearchView
        searchView?.let { searchVue ->
            searchVue.setSearchableInfo(manager.getSearchableInfo(componentName))
            searchVue.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchRestaurant(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchRestaurant(searchQuery)
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.permission_needed))
                    .setPositiveButton(getString(R.string.permission_okay)) { _, _ ->
                        openSettingsPage()
                    }.setNegativeButton(getString(R.string.permission_cancel), null)
                    .create().show()
            }
        }
    }

    private fun showList() {
        recyclerView.isVisible = true
        //TODO hide map
    }

    private fun showMap() {
        //TODO show map
        recyclerView.isVisible = false
    }


    @SuppressLint("MissingPermission")
    private fun searchRestaurant(query: String) {
        searchQuery = query
        if (hasLocationPermission()) {
            progressBar.isVisible = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) viewModel.searchNearbyRestaurants(query, location)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE)
    }

    private fun openSettingsPage() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun displayErrorMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    }
}
