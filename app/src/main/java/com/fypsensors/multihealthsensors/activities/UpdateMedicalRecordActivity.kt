package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fypsensors.multihealthsensors.databinding.ActivityUpdateMedicalRecordBinding
import com.fypsensors.multihealthsensors.models.Users
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class UpdateMedicalRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateMedicalRecordBinding
    private var mAuth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateMedicalRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("UsersData")

        binding.firstNameEt.isFocusable = false
        binding.lastNameEt.isFocusable = false
        binding.emailEt.isFocusable = false
        binding.ageEt.isFocusable = false
        binding.medicalConditionEt.isFocusable = false

        binding.homeIv.setOnClickListener {
            onBackPressed()
        }

        binding.editBtn.setOnClickListener {
            binding.firstNameEt.isFocusable = true
            binding.firstNameEt.isFocusableInTouchMode = true
            binding.lastNameEt.isFocusable = true
            binding.lastNameEt.isFocusableInTouchMode = true
            binding.ageEt.isFocusable = true
            binding.ageEt.isFocusableInTouchMode = true
            binding.medicalConditionEt.isFocusable = true
            binding.medicalConditionEt.isFocusableInTouchMode = true
            binding.editBtn.visibility = View.GONE
            binding.updateBtn.visibility = View.VISIBLE


        }
        binding.updateBtn.setOnClickListener {
            if (Objects.requireNonNull(binding.firstNameEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter first name"
                return@setOnClickListener
            }
            if (Objects.requireNonNull(binding.lastNameEt.text).toString().isEmpty()) {
                binding.emailEt.error = "Please enter last name"
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

            updateRecord()

        }

        getUserData()

    }

    private fun updateRecord() {
        //update user detail in firebase
        binding.loading.visibility = View.VISIBLE
        val userMap = HashMap<String, Any>()
        userMap["firstName"] = binding.firstNameEt.text.toString()
        userMap["lastName"] = binding.lastNameEt.text.toString()
        userMap["age"] = binding.ageEt.text.toString()
        userMap["medicalCondition"] = binding.medicalConditionEt.text.toString()

        userRef!!.child(Objects.requireNonNull(mAuth!!.uid.toString())).updateChildren(userMap)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(
                        this@UpdateMedicalRecordActivity,
                        "Medical Record Updated Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    onBackPressed()
                }
            }.addOnFailureListener { e: Exception ->
                binding.loading.visibility = View.GONE
                val snackbar = Snackbar
                    .make(
                        binding.root,
                        Objects.requireNonNull(e.message).toString(),
                        Snackbar.LENGTH_LONG
                    )
                snackbar.show()
            }

    }

    private fun getUserData() {
        userRef!!.child(mAuth!!.uid.toString()).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    binding.firstNameEt.setText(user!!.firstName)
                    binding.lastNameEt.setText(user.lastName)
                    binding.emailEt.setText(user.email)
                    binding.medicalConditionEt.setText(user.medicalCondition)
                    binding.ageEt.setText(user.age)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
            }
        })
    }
}