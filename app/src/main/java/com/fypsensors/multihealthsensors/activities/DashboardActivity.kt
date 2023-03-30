package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.auth.LoginActivity
import com.fypsensors.multihealthsensors.databinding.ActivityDashboardBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dev.shreyaspatil.MaterialDialog.MaterialDialog

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private var mAuth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("UsersData")

        binding.logoutIv.setOnClickListener {
            logout()
        }
        binding.bodyTemperatureCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, BodyTemperatureActivity::class.java))
        }
        binding.heartRateCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, HeartRateActivity::class.java))
        }
        binding.fallAlertCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, FallAlertActivity::class.java))
        }
        binding.glucoseMonitorCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, GlucoseMonitorActivity::class.java))
        }
        binding.updateMedicalRecordCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, UpdateMedicalRecordActivity::class.java))
        }

        getUserData()
    }
    private fun getUserData() {
        userRef!!.child(mAuth!!.uid.toString()).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.userNameTv.text =
                        "Welcome, " + snapshot.child("firstName").value.toString() + " " + snapshot.child(
                            "lastName"
                        ).value.toString()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
            }
        })
    }
    private fun logout() {

        val mDialog = MaterialDialog.Builder(this).setTitle("Logout?")
            .setMessage("Are you sure want to logout?").setCancelable(false).setPositiveButton(
                "Yes", R.drawable.ic_baseline_check_24
            ) { dialogInterface, _ ->
                mAuth!!.signOut()
                dialogInterface.dismiss()
                Snackbar.make(binding.root, "Logout Successfully", Snackbar.LENGTH_LONG).show()
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finishAffinity()


            }.setNegativeButton(
                "No", R.drawable.ic_baseline_close_24
            ) { dialogInterface, _ -> dialogInterface.dismiss() }.build()

        // Show Dialog
        mDialog.show()

    }
}