package com.kotlin.sups.ui.home

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kotlin.sups.R
import com.kotlin.sups.adapter.LoadingStateAdapter
import com.kotlin.sups.adapter.StoryAdapter
import com.kotlin.sups.databinding.ActivityMainBinding
import com.kotlin.sups.helper.ViewModelFactory
import com.kotlin.sups.ui.addStory.AddStoryActivity
import com.kotlin.sups.ui.detail.DetailActivity
import com.kotlin.sups.ui.login.LoginActivity
import com.kotlin.sups.ui.maps.MapsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private val timeInterval: Int = 2000
    private var mBackPressed: Long = 0
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                exitOnBackPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        exitOnBackPressed()
                    }
                }
            )
        }

        mainViewModel.getLoginState().observe(this) { isLogin ->
            if (!isLogin) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
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
                            mainViewModel.deleteToken()
                            mainViewModel.saveLoginState(false)
                        }.show()
                    true
                }

                else -> true
            }
        }

        binding.addButton.setOnClickListener {
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
            startActivity(Intent(this, AddStoryActivity::class.java), option)
        }

        val orientation = resources.configuration.orientation
        binding.rvStories.layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }

        getData()

        binding.swipeToRefresh.setColorSchemeColors(getColor(R.color.dark_blue))
        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
            showLoading()
        }
    }

    private fun getData() {
        adapter = StoryAdapter(onClick = { story, view ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY, story)
            val optionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this as Activity,
                    Pair(view.ivItemPhoto, "image"),
                    Pair(view.tvItemName, "name"),
                    Pair(view.tvItemDate, "date"),
                    Pair(view.tvItemDescription, "desc")
                )
            startActivity(intent, optionsCompat.toBundle())
        })

        adapter.addLoadStateListener {
            showError(adapter.itemCount <= 0)
        }

        mainViewModel.story.observe(this) { data ->
            adapter.submitData(lifecycle, data)
        }

        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        showLoading()
    }

    private fun showLoading() {
        binding.loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(2000)
            binding.loading.visibility = View.INVISIBLE
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun showError(
        isError: Boolean
    ) {
        binding.apply {
            if (isError) {
                tvStatus.visibility = View.VISIBLE
                ivStatus.visibility = View.VISIBLE
                rvStories.visibility = View.INVISIBLE
            } else {
                tvStatus.visibility = View.INVISIBLE
                ivStatus.visibility = View.INVISIBLE
                rvStories.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
        showLoading()
    }

    private fun exitOnBackPressed() {
        if (mBackPressed + timeInterval > System.currentTimeMillis()) {
            finish()
            return
        }

        Toast.makeText(this, getString(R.string.double_back_exit), Toast.LENGTH_SHORT).show()
        mBackPressed = System.currentTimeMillis()
    }
}