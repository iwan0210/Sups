package com.kotlin.sups.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kotlin.sups.R
import com.kotlin.sups.data.model.Story
import com.kotlin.sups.databinding.ActivityDetailBinding
import com.kotlin.sups.helper.ViewModelFactory
import com.kotlin.sups.helper.formatDate
import com.kotlin.sups.helper.loadImage
import com.kotlin.sups.ui.login.LoginActivity
import com.kotlin.sups.ui.maps.MapsActivity

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        val detailViewModel: DetailViewModel by viewModels { factory }

        val story = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STORY, Story::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_STORY)
        }

        detailViewModel.getLoginState().observe(this) { isLogin ->
            if (!isLogin) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        binding.appBar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        binding.appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_maps -> {
                    val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                    startActivity(Intent(this, MapsActivity::class.java), option)
                    true
                }

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
                            detailViewModel.deleteToken()
                            detailViewModel.saveLoginState(false)
                        }.show()
                    true
                }

                else -> true
            }
        }

        story?.let {
            binding.apply {
                tvDetailName.text = it.name
                tvDetailDate.text = it.createdAt.formatDate()
                tvDetailDescription.text = it.description
                ivDetailPhoto.loadImage(it.photoUrl)

            }
        }

    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}