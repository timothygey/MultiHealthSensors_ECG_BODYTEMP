package com.fypsensors.multihealthsensors.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fypsensors.multihealthsensors.databinding.ActivityFallAlertBinding
import com.fypsensors.multihealthsensors.databinding.ActivityHeartRateBinding

class FallAlertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFallAlertBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFallAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeIv.setOnClickListener {
            onBackPressed()
        }
    }
}