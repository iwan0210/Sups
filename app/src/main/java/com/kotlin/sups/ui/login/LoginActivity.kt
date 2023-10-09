package com.kotlin.sups.ui.login

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kotlin.sups.R
import com.kotlin.sups.data.Result
import com.kotlin.sups.databinding.ActivityLoginBinding
import com.kotlin.sups.helper.ViewModelFactory
import com.kotlin.sups.ui.home.MainActivity
import com.kotlin.sups.ui.register.RegisterActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val timeInterval: Int = 2000
    private var mBackPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 31) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L
                slideUp.doOnEnd { splashScreenView.remove() }
                slideUp.start()
            }
        }

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

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        val loginViewModel: LoginViewModel by viewModels { factory }

        loginViewModel.getLoginState().observe(this) { isLogin ->
            if (isLogin) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        loginViewModel.result.observe(this) { event ->
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
                            loginViewModel.saveToken(result.data.token)
                            delay(2000)
                            showLoading(false)
                            loginViewModel.saveLoginState(true)
                        }
                    }
                }
            }
        }

        binding.registerNow.setOnClickListener {
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
            startActivity(Intent(this, RegisterActivity::class.java), option)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.editText?.text.toString().trim()
            val password = binding.edLoginPassword.editText?.text.toString().trim()
            when {
                email.isEmpty() -> binding.edLoginEmail.error = getString(R.string.email_invalid)
                password.isEmpty() -> binding.edLoginPassword.error =
                    getString(R.string.password_invalid)

                binding.edLoginEmail.error != null || binding.edLoginPassword.error != null -> Unit
                else -> {
                    loginViewModel.login(email, password)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.visibility = View.VISIBLE
            binding.loginButton.visibility = View.INVISIBLE
        } else {
            binding.loading.visibility = View.INVISIBLE
            binding.loginButton.visibility = View.VISIBLE
        }
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