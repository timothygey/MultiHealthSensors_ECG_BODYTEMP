package com.fypsensors.multihealthsensors.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fypsensors.multihealthsensors.databinding.ActivityRegisterBinding
import com.fypsensors.multihealthsensors.models.Users
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("UsersData")


        //click listeners
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }
        binding.registerBtn.setOnClickListener {

            //check validations
            if (Objects.requireNonNull(binding.firstNameEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter first name"
                return@setOnClickListener
            }
            if (Objects.requireNonNull(binding.lastNameEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter last name"
                return@setOnClickListener
            }

            if (Objects.requireNonNull(binding.emailEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter valid email"
                return@setOnClickListener
            }
            if (Objects.requireNonNull(binding.ageEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter your age"
                return@setOnClickListener
            }
            if (Objects.requireNonNull(binding.medicalConditionEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter your medical condition"
                return@setOnClickListener
            }

            if (Objects.requireNonNull(binding.passwordEt.text).toString().isEmpty()) {
                binding.passwordEt.error = "Please enter password"
                return@setOnClickListener
            }

            if (Objects.requireNonNull(binding.confirmPasswordEt.text).toString().isEmpty()) {
                binding.confirmPasswordEt.error = "Please enter confirm password"
                return@setOnClickListener
            }

            if (!binding.passwordEt.text.toString()
                    .equals(binding.confirmPasswordEt.text.toString())
            ) {
                binding.confirmPasswordEt.error = "Confirm password is not correct"
                return@setOnClickListener
            }
            //register user
            register()
        }

    }

    private fun register() {
        binding.loading.visibility = View.VISIBLE
        binding.registerBtn.isEnabled = false
        mAuth!!.createUserWithEmailAndPassword(
            binding.emailEt.text.toString(),
            binding.passwordEt.text.toString()
        )
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {

                    //store user detail in firebase
                    val users = Users(
                        mAuth!!.uid.toString(),
                        binding.firstNameEt.text.toString(),
                        binding.lastNameEt.text.toString(),
                        binding.ageEt.text.toString(),
                        binding.emailEt.text.toString(),
                        binding.medicalConditionEt.text.toString()
                    )
                    userRef!!.child(Objects.requireNonNull(mAuth!!.uid.toString())).setValue(users)
                        .addOnCompleteListener { task1: Task<Void?> ->
                            if (task1.isSuccessful) {


                                sendEmailVerification()

                            }
                        }

                }
            }.addOnFailureListener { e: Exception ->
                binding.loading.visibility = View.GONE
                binding.registerBtn.isEnabled = true
                val snackbar = Snackbar
                    .make(
                        binding.root,
                        Objects.requireNonNull(e.message).toString(),
                        Snackbar.LENGTH_LONG
                    )
                snackbar.show()
            }
    }

    private fun sendEmailVerification() {
        val user = mAuth!!.currentUser
        user!!.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.loading.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    "Verification email sent to " + user.email,
                    Snackbar.LENGTH_LONG
                ).show()
                mAuth!!.signOut()

                val intent =
                    Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                binding.loading.visibility = View.GONE
                Snackbar.make(
                    binding.root,
                    "Failed to send verification email.",
                    Snackbar.LENGTH_LONG
                ).show()
            }


        }
    }
}