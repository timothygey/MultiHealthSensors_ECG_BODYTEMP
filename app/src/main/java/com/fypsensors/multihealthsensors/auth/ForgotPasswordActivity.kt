package com.fypsensors.multihealthsensors.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fypsensors.multihealthsensors.databinding.ActivityForgotPasswordBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.sendBtn.setOnClickListener {
            if (binding.etEmail.text.toString().isEmpty()) {
                binding.etEmail.error = "Please enter email"
                return@setOnClickListener
            }

            //sendEmail
            sendResetEmail()
        }


    }

    private fun sendResetEmail() {
        binding.loading.visibility = View.VISIBLE
        binding.sendBtn.isEnabled = false
        mAuth!!.sendPasswordResetEmail(binding.etEmail.text.toString())
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    binding.loading.visibility = View.GONE
                    val snackbar = Snackbar
                        .make(
                            binding.root,
                            "Please check your email to reset Password",
                            Snackbar.LENGTH_LONG
                        )
                    snackbar.show()
                    val intent =
                        Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }.addOnFailureListener { e: Exception ->
                binding.loading.visibility = View.GONE
                binding.sendBtn.isEnabled = true
                val snackbar = Snackbar
                    .make(binding.root, e.message!!, Snackbar.LENGTH_LONG)
                snackbar.show()
            }
    }
}