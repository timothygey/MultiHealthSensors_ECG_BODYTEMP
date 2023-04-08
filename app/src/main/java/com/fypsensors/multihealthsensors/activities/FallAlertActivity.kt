package com.fypsensors.multihealthsensors.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.databinding.ActivityFallAlertBinding
import java.util.*

class FallAlertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFallAlertBinding
    private var mReceiver: BroadcastReceiver? = null
    private var filter: IntentFilter? = null
    private var countDownTimer: CountDownTimer? = null
    private val notID = 1
    private var falling_and_not_read = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFallAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeIv.setOnClickListener {
            val intent = Intent(this@FallAlertActivity, DashboardActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }



        binding.lastTimeFallTv.text = "Time of previous fall incident:"
        binding.timeBoxTv.text = "NA"
        binding.remarkTv.text = "Read"
        binding.remarkTv.setTextColor(Color.GREEN)
        binding.idTVRetrieveData.text = "No fall was detected"
        binding.idTVRetrieveData.setTextColor(Color.GREEN)
        binding.timeBoxTv.text = Calendar.getInstance().time.toString()
        binding.timeBoxTv.setTextColor(Color.GREEN)
        binding.button.text = "No Action Needed."


        val bundle = intent.extras
        if (bundle != null) {
            val isDetected = bundle.getBoolean("isDetected")
            if (isDetected) {
                val buttonText = intent.getStringExtra("button_text")
                val textViewText = intent.getStringExtra("text_view_text")
                val textViewFont = intent.getIntExtra("text_view_font", 0)
                val textViewColor = intent.getIntExtra("text_view_color", 0)
                val time = intent.getStringExtra("time")
                val test = intent.getStringExtra("nonsense")



                binding.button.text = buttonText
                binding.idTVRetrieveData.text = textViewText
                binding.idTVRetrieveData.setTypeface(null, textViewFont)
                binding.idTVRetrieveData.setTextColor(textViewColor)
                startCounter(buttonText)
                if (textViewColor == Color.RED) {
                    falling_and_not_read = true
                    binding.timeBoxTv.text = time
                    binding.remarkTv.text = "Unread"
                    binding.remarkTv.setTextColor(Color.RED)
                }
            }
        }



        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val buttonText = intent.getStringExtra("button_text")
                val textViewText = intent.getStringExtra("text_view_text")
                val textViewFont = intent.getIntExtra("text_view_font", 0)
                val textViewColor = intent.getIntExtra("text_view_color", 0)
                val time = intent.getStringExtra("time")
                val test = intent.getStringExtra("nonsense")



                binding.button.text = buttonText
                binding.idTVRetrieveData.text = textViewText
                binding.idTVRetrieveData.setTypeface(null, textViewFont)
                binding.idTVRetrieveData.setTextColor(setTheColor(textViewColor))
                if (countDownTimer != null) {
                    countDownTimer?.cancel()
                }
                startCounter(buttonText)
                if (textViewColor == Color.RED) {
                    falling_and_not_read = true
                    binding.timeBoxTv.text = time
                    binding.remarkTv.text = "Unread"
                    binding.remarkTv.setTextColor(Color.RED)
                }
            }
        }

        binding.button.setOnClickListener {
            if (falling_and_not_read) {
                falling_and_not_read = false
                binding.remarkTv.text = "Read"
                binding.remarkTv.setTextColor(Color.GREEN)
                binding.idTVRetrieveData.text = "No fall was detected."
                binding.idTVRetrieveData.setTypeface(null, Typeface.NORMAL)
                binding.idTVRetrieveData.setTextColor(Color.GREEN)
                binding.button.text = "No action needed."
            }
        }

        filter = IntentFilter("com.fypsensors.multihealthsensors.UPDATE_UI")
        registerReceiver(mReceiver, filter)


    }

    private fun setTheColor(theColor: Int): Int {
        if (theColor == Color.RED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the notification channel
                val name = "My Channel"
                val description = "My Channel Description"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("my_channel_id_new", name, importance).apply {
                    this.description = description
                }

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            val intent1 = Intent(applicationContext, DashboardActivity::class.java)
            intent1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent1,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "my_channel_id_new")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Fall detected!")
                .setContentText("Please check the patient who may have fallen!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(12, notification)

        }
        return theColor
    }

    private fun startCounter(buttonText: String?) {
        countDownTimer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the UI with the remaining time
                val seconds = millisUntilFinished / 1_000
                binding.button.text = "$buttonText $seconds"
            }

            override fun onFinish() {
                binding.lastTimeFallTv.text = "Time of previous fall incident:"
                binding.timeBoxTv.text = "NA"
                binding.remarkTv.text = "Read"
                binding.remarkTv.setTextColor(Color.GREEN)
                binding.idTVRetrieveData.text = "No fall was detected"
                binding.idTVRetrieveData.setTextColor(Color.GREEN)
                binding.timeBoxTv.text = Calendar.getInstance().time.toString()
                binding.timeBoxTv.setTextColor(Color.GREEN)
                binding.button.text = "No Action Needed."
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the counter when the activity is destroyed
        countDownTimer?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@FallAlertActivity, DashboardActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}