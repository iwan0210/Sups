package com.kotlin.sups.ui.addStory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kotlin.sups.R
import com.kotlin.sups.data.Result
import com.kotlin.sups.databinding.ActivityAddStoryBinding
import com.kotlin.sups.helper.ViewModelFactory
import com.kotlin.sups.helper.reduceFileImage
import com.kotlin.sups.helper.uriToFile
import com.kotlin.sups.ui.camera.CameraActivity
import com.kotlin.sups.ui.camera.CameraActivity.Companion.CAMERAX_RESULT
import com.kotlin.sups.ui.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private var location: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
    private val addStoryViewModel: AddStoryViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addStoryViewModel.getLoginState().observe(this) { isLogin ->
            if (!isLogin) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
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
                            addStoryViewModel.deleteToken()
                            addStoryViewModel.saveLoginState(false)
                        }.show()
                    true
                }

                else -> true
            }
        }

        binding.apply {
            appBar.setNavigationOnClickListener {
                finishAfterTransition()
            }

            btnGallery.setOnClickListener {
                launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            btnCamera.setOnClickListener {
                val intent = Intent(this@AddStoryActivity, CameraActivity::class.java)
                launcherIntentCameraX.launch(intent)
            }

            buttonAdd.setOnClickListener {
                val desc = edAddDescription.editText?.text.toString().trim()
                when {
                    desc.isEmpty() -> edAddDescription.error = getString(R.string.cannot_be_empty)
                    edAddDescription.error != null -> Unit
                    else -> uploadImage()
                }
            }

            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    getMyLastLocation { location ->
                        if (location == null) {
                            showToast("Please enable your location")
                            buttonView.isChecked = false
                        }
                        this@AddStoryActivity.location = location
                    }
                } else {
                    location = null
                }
            }
        }

        addStoryViewModel.result.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                    }

                    is Result.Error -> {
                        lifecycleScope.launch {
                            delay(2000)
                            showLoading(false)
                            Snackbar.make(
                                window.decorView.rootView,
                                result.error,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is Result.Success -> {
                        lifecycleScope.launch {
                            delay(2000)
                            showLoading(false)
                            Snackbar.make(
                                window.decorView.rootView,
                                result.data.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            finishAfterTransition()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                loading.visibility = View.VISIBLE
                buttonAdd.isEnabled = false
                btnCamera.isEnabled = false
                btnGallery.isEnabled = false
            } else {
                loading.visibility = View.INVISIBLE
                buttonAdd.isEnabled = true
                btnCamera.isEnabled = true
                btnGallery.isEnabled = true
            }
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivPreview.setImageURI(it)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edAddDescription.editText?.text.toString().trim()

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            addStoryViewModel.postStory(
                multipartBody,
                requestBody,
                location?.latitude?.toFloat(),
                location?.longitude?.toFloat()
            )
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation {}
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation {}
                }

                else -> {
                    binding.checkbox.isChecked = false
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation(listener: (Location?) -> Unit) {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    listener.invoke(location)
                } else {
                    showToast("Failed to get Location, Please Try Again")
                    binding.checkbox.isChecked = false
                }

            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}