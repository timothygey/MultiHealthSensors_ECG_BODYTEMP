package com.fypsensors.multihealthsensors.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fypsensors.multihealthsensors.activities.BodyTemperatureActivity
import com.fypsensors.multihealthsensors.activities.DashboardActivity
import com.fypsensors.multihealthsensors.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.forgotPasswordTv.setOnClickListener {
            val mainIntent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(mainIntent)
        }
        binding.registerTv.setOnClickListener {
            val mainIntent = Intent(this, RegisterActivity::class.java)
            startActivity(mainIntent)
        }
        binding.loginBtn.setOnClickListener {
            if (Objects.requireNonNull(binding.emailEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter valid email"
                return@setOnClickListener
            }
            if (Objects.requireNonNull(binding.passwordEt.text).toString().isEmpty()) {
                binding.passwordEt.error = "Please enter password"
                return@setOnClickListener
            }

            loginUser()
        }
    }

    private fun loginUser() {

        binding.loading.visibility = View.VISIBLE
        binding.loginBtn.isEnabled = false

        mAuth!!.signInWithEmailAndPassword(
            binding.emailEt.text.toString(),
            binding.passwordEt.text.toString()
        )
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {

                    val user = FirebaseAuth.getInstance().currentUser
                    user!!.reload()

                    if (user.isEmailVerified)
                    {
                        binding.loading.visibility = View.GONE
                        Snackbar.make(binding.root, "Login Successfully", Snackbar.LENGTH_LONG)
                            .show()
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java).setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                        ))
                        finishAffinity()
                    }
                    else
                    {
                        mAuth!!.signOut()
                        binding.loading.visibility = View.GONE
                        binding.loginBtn.isEnabled = true
                        Snackbar.make(binding.root, "Please verify your email", Snackbar.LENGTH_LONG)
                            .show()
                    }


                }
            }.addOnFailureListener { e: Exception ->
                binding.loading.visibility = View.GONE
                binding.loginBtn.isEnabled = true
                Snackbar
                    .make(binding.root, e.message!!, Snackbar.LENGTH_LONG).show()
            }


    }
}