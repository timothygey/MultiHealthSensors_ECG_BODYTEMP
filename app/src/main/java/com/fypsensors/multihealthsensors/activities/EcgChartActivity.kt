package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.fypsensors.multihealthsensors.databinding.ActivityEcgChartBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

class EcgChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEcgChartBinding
    private var bpmReadingsRef: DatabaseReference? = null
    private val entries: ArrayList<Entry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcgChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bpmReadingsRef = FirebaseDatabase.getInstance().reference.child("heartbeat_bpm")
        binding.homeIv.setOnClickListener {
            onBackPressed()
        }
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
                        Log.e("userrrr", snapshot.child("BPM").value.toString().toDouble().toFloat().toString())
                        entries.add(
                            Entry(
                                entries.size.toFloat(),
                                snapshot.child("BPM").value.toString().toDouble().toFloat()
                            )
                        )

                        val dataSet = LineDataSet(entries, "BPM")
                        dataSet.color = Color.BLUE
                        dataSet.setCircleColor(Color.BLUE)
                        dataSet.setDrawValues(false)
                        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                        val lineData = LineData(dataSet)
                        binding.chart.data = lineData
                        binding.chart.invalidate()

                        val yAxis: YAxis = binding.chart.axisLeft
                        yAxis.axisMinimum = 50f
                        yAxis.axisMaximum = 100f








                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.loading.visibility = View.GONE
                    Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            })
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Adjust the chart size and layout based on the new orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape orientation, set the chart width to match parent and height to 600dp
            binding.chart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.chart.layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 600f, resources.displayMetrics
            ).toInt()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait orientation, set the chart width to match parent and height to 400dp
            binding.chart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.chart.layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 400f, resources.displayMetrics
            ).toInt()
        }
    }

}