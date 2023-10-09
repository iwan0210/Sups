package com.kotlin.sups.ui.register

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kotlin.sups.R
import com.kotlin.sups.data.Result
import com.kotlin.sups.databinding.ActivityRegisterBinding
import com.kotlin.sups.helper.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        val registerViewModel: RegisterViewModel by viewModels { factory }

        registerViewModel.result.observe(this) { event ->
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

        binding.loginNow.setOnClickListener {
            finishAfterTransition()
        }

        binding.registerButton.setOnClickListener {
            val name = binding.edRegisterName.editText?.text.toString().trim()
            val email = binding.edRegisterEmail.editText?.text.toString().trim()
            val password = binding.edRegisterPassword.editText?.text.toString().trim()
            when {
                name.isEmpty() -> binding.edRegisterName.error = getString(R.string.cannot_be_empty)
                email.isEmpty() -> binding.edRegisterEmail.error = getString(R.string.email_invalid)
                password.isEmpty() -> binding.edRegisterPassword.error =
                    getString(R.string.password_invalid)

                binding.edRegisterName.error != null || binding.edRegisterEmail.error != null || binding.edRegisterPassword.error != null -> Unit
                else -> {
                    registerViewModel.register(name, email, password)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.visibility = View.VISIBLE
            binding.registerButton.visibility = View.INVISIBLE
        } else {
            binding.loading.visibility = View.INVISIBLE
            binding.registerButton.visibility = View.VISIBLE
        }
    }
}