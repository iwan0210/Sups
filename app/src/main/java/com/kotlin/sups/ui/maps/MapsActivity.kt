package com.kotlin.sups.ui.maps

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kotlin.sups.R
import com.kotlin.sups.data.Result
import com.kotlin.sups.data.model.Story
import com.kotlin.sups.databinding.ActivityMapsBinding
import com.kotlin.sups.helper.ViewModelFactory
import com.kotlin.sups.ui.detail.DetailActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
    private val mapsViewModel: MapsViewModel by viewModels {
        factory
    }

    private val windowData = mutableMapOf<Marker?, Story>()
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        binding.appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_language -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }

                R.id.action_logout -> {
                    MaterialAlertDialogBuilder(this, R.style.Theme_Sups_Dialog)
                        .setTitle(getString(R.string.logout))
                        .setCancelable(true)
                        .setMessage(getString(R.string.logout_confirm))
                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                            mapsViewModel.deleteToken()
                            mapsViewModel.saveLoginState(false)
                        }.show()
                    true
                }

                else -> true
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val indo = LatLng(-2.1983022, 117.7231143)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indo, 3f))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e("SetMayStyle", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("SetMayStyle", "Can't find style. Error: ", exception)
        }

        mapsViewModel.getStoriesWithLocation()

        mMap.setOnInfoWindowClickListener {
            val data = windowData[it]
            if (data != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_STORY, data)
                startActivity(intent)
            }
        }

        mapsViewModel.result.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }

                is Result.Error -> {
                    lifecycleScope.launch {
                        delay(2000)
                        showLoading(false)
                        Toast.makeText(this@MapsActivity, result.error, Toast.LENGTH_SHORT).show()
                    }
                }

                is Result.Success -> {
                    lifecycleScope.launch {
                        delay(2000)
                        showLoading(false)
                        if (result.data.isEmpty()) {
                            Toast.makeText(
                                this@MapsActivity,
                                getString(R.string.no_data_available),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            mMap.clear()
                            windowData.clear()
                            result.data.forEach { story ->
                                val lat = story.lat ?: 0.0
                                val long = story.lon ?: 0.0
                                val location = LatLng(lat, long)
                                val marker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title(story.name)
                                        .snippet(story.description)
                                )
                                windowData[marker] = story
                                boundsBuilder.include(location)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}