package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fypsensors.multihealthsensors.adapters.EcgReadingsAdapter
import com.fypsensors.multihealthsensors.databinding.ActivityEcgReadingBinding
import com.fypsensors.multihealthsensors.models.EcgReadings
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

class EcgReadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEcgReadingBinding
    private val readingsArrayList: ArrayList<EcgReadings> = ArrayList()
    private val averageArray: ArrayList<Double> = ArrayList()
    private var bpmReadingsRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcgReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bpmReadingsRef = FirebaseDatabase.getInstance().reference.child("heartbeat_bpm")
        binding.homeIv.setOnClickListener {
            onBackPressed()
        }

        getEcgData()
        getCurrentBpm()
    }

    private fun getCurrentBpm() {
        binding.loading.visibility = View.VISIBLE
        bpmReadingsRef!!.child("1-setbpm")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.loading.visibility = View.GONE
                    if (snapshot.exists()) {
                        binding.bpmTv.text =
                            String.format("%.2f", snapshot.child("BPM").value.toString().toDouble())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loading.visibility = View.GONE
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            })
    }

    private fun getEcgData() {
        binding.loading.visibility = View.VISIBLE
        bpmReadingsRef!!
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.loading.visibility = View.GONE
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {

                            val readings = dataSnapshot.getValue(EcgReadings::class.java)
                            readingsArrayList.add(readings!!)

                        }

                        readingsArrayList.reverse()
                        readingsArrayList.removeAt(0)

                        if (readingsArrayList.size >= 30) {
                            for (i in 0 until 30) {
                                val item = readingsArrayList[i]
                                averageArray.add(item.bpm)
                            }
                            val averageReading = averageArray.average()

                            binding.averageTv.text = String.format("%.2f", averageReading)

                        } else {
                            for (i in 0 until readingsArrayList.size) {
                                val item = readingsArrayList[i]
                                averageArray.add(item.bpm)
                            }
                            val averageReading = averageArray.average()

                            binding.averageTv.text = String.format("%.2f", averageReading)
                        }
                        val mAdapter =
                            EcgReadingsAdapter(this@EcgReadingActivity, readingsArrayList)
                        val layoutManager =
                            LinearLayoutManager(
                                this@EcgReadingActivity,
                                RecyclerView.VERTICAL,
                                false
                            )
                        binding.readingsRv.layoutManager = layoutManager
                        binding.readingsRv.adapter = mAdapter

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loading.visibility = View.GONE
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            })

    }
}