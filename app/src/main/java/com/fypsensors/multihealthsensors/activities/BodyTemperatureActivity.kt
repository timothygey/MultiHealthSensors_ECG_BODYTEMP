package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.adapters.ReadingsAdapter
import com.fypsensors.multihealthsensors.auth.LoginActivity
import com.fypsensors.multihealthsensors.databinding.ActivityBodyTemperatureBinding
import com.fypsensors.multihealthsensors.models.Readings
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dev.shreyaspatil.MaterialDialog.MaterialDialog

class BodyTemperatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBodyTemperatureBinding
    private var mAuth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null
    private val readingsArrayList: ArrayList<Readings> = ArrayList()
    private val averageArray: ArrayList<Double> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBodyTemperatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("UsersData")


        binding.homeIv.setOnClickListener {
            onBackPressed()
        }

        getReadingsData()
    }




    private fun getReadingsData() {
        binding.loading.visibility = View.VISIBLE
        userRef!!.child("Gku0FBzoJCeZzink9wyQRxluOa22").child("readings")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.loading.visibility = View.GONE
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {

                            val readings = dataSnapshot.getValue(Readings::class.java)
                            readingsArrayList.add(readings!!)

                        }

                        readingsArrayList.reverse()

                        binding.temperatureTv.text =
                            readingsArrayList[0].bodytemperature + " \u2103"

                        if (readingsArrayList.size >= 30) {
                            for (i in 0 until 30) {
                                val item = readingsArrayList[i]
                                averageArray.add(item.bodytemperature.toDouble())
                            }
                            val averageReading = averageArray.average()
                            var patientCondition = ""
                            if (averageReading < 20.00) patientCondition = "Hypothermia"
                            if (averageReading in 20.00..32.00) patientCondition = "Normal"
                            if (averageReading in 32.00..36.00) patientCondition = "Fever"
                            if (averageReading > 36.00) patientCondition = "High Fever"

                            binding.currentTv.text = patientCondition
                            if (averageReading > 32.00) binding.feverTv.text =
                                "Fever in last 24 hours: Yes ${
                                    String.format(
                                        "%.2f",
                                        averageReading
                                    )
                                }"
                            else binding.feverTv.text = "Fever in last 24 hours: No  ${
                                String.format(
                                    "%.2f",
                                    averageReading
                                )
                            }"

                        }
                        val mAdapter = ReadingsAdapter(this@BodyTemperatureActivity, readingsArrayList)
                        val layoutManager =
                            LinearLayoutManager(this@BodyTemperatureActivity, RecyclerView.VERTICAL, false)
                        binding.readingsRv.layoutManager = layoutManager
                        binding.readingsRv.adapter = mAdapter

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loading.visibility = View.GONE
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
                }
            })

    }

}