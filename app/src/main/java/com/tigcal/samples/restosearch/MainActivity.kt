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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ktx.addMarker
import com.tigcal.samples.restosearch.model.Restaurant
import com.tigcal.samples.restosearch.model.latLng
import com.tigcal.samples.restosearch.network.MapRepository
import com.tigcal.samples.restosearch.util.NutritionixUtil
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val REQUEST_CODE = 0
    private var searchQuery = ""
    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var restaurants = emptyList<Restaurant>()

    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: MapRepository
    private lateinit var mapViewModel: MapViewModel
    private lateinit var nutritionixViewModel: NutritionixViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var resturantAdapter: ResturantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        repository = (application as RestoSearchApp).mapRepository
        mapViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(repository) as T
            }
        })[MapViewModel::class.java]
        nutritionixViewModel = NutritionixViewModel()

        resturantAdapter = ResturantAdapter(this)
        resturantAdapter.onClickListener = { resto -> openRestaurantDetails(resto) }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = resturantAdapter
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
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

        val nutritionixBrands = NutritionixUtil.getBrands(this, "nutritionix_brands.json")
        nutritionixViewModel.nutritionixBrands = nutritionixBrands

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mapViewModel.restaurants.collect { restos ->
                        displayRestaurants(restos)
                        bottomNavigationView.selectedItemId = R.id.action_list
                    }
                }
                launch {
                    mapViewModel.error.collect { message ->
                        progressBar.isVisible = false
                        if (message.isNotEmpty()) {
                            displayErrorMessage(message)
                        }
                    }
                }
            }
        }

        showList()
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
        mapFragment?.let {
            supportFragmentManager.beginTransaction()
                .hide(it)
                .commit()
        }
    }

    private fun showMap() {
        mapFragment?.let {
            supportFragmentManager.beginTransaction()
                .show(it)
                .commit()
        }
        recyclerView.isVisible = false
    }


    @SuppressLint("MissingPermission")
    private fun searchRestaurant(query: String) {
        searchQuery = query
        if (hasLocationPermission()) {
            progressBar.isVisible = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) mapViewModel.searchNearbyRestaurants(query, location)
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

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        this.googleMap?.let { gMap ->
            gMap.uiSettings.isZoomControlsEnabled = true
            gMap.setOnInfoWindowClickListener { marker ->
                val resto = restaurants[marker.tag.toString().toInt()]
                openRestaurantDetails(resto)
            }
        }

        centerMapToCurrentLoc()
    }

    private fun centerMapToCurrentLoc() {
        mapViewModel.lastKnownLocation?.let { location ->
            this.googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f)
            )
        }
    }

    private fun displayRestaurants(restos: List<Restaurant>) {
        restaurants = restos

        progressBar.isVisible = false
        resturantAdapter.setRestaurants(restos)

        centerMapToCurrentLoc()

        restos.forEachIndexed { index, resto ->
            googleMap?.addMarker {
                position(LatLng(resto.latLng.first, resto.latLng.second))
                title(resto.name)
            }?.tag = index
        }
    }

    private fun openRestaurantDetails(resto: Restaurant) {
        Log.d("resto", "open: $resto")
        val intent = Intent(this, RestaurantActivity::class.java).apply {
            putExtra(RestaurantActivity.EXTRA_NAME, resto.name)

        }
        startActivity(intent)
    }
}
