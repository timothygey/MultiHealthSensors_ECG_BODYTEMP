package com.fypsensors.multihealthsensors.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.auth.LoginActivity
import com.fypsensors.multihealthsensors.databinding.ActivityDashboardBinding
import com.fypsensors.multihealthsensors.services.ForegroundService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dev.shreyaspatil.MaterialDialog.MaterialDialog

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private var mAuth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null
    private var isDetected: Boolean = false
    private var mReceiver: BroadcastReceiver? = null
    private var filter: IntentFilter? = null
    private var buttonText: String? = null
    private var textViewText: String? = null
    private var time: String? = null
    private var test: String? = null
    private var textViewFont: Int? = null
    private var textViewColor: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(12)
            val intent = Intent(this@DashboardActivity, FallAlertActivity::class.java)
            intent.putExtra("isDetected", isDetected)
            intent.putExtra("button_text", buttonText)
            intent.putExtra("text_view_text", textViewText)
            intent.putExtra("text_view_font", textViewFont)
            intent.putExtra("text_view_color", textViewColor)
            intent.putExtra("time", time)
            intent.putExtra("nonsense", test)
            startActivity(intent)
        }
        binding.glucoseMonitorCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, GlucoseMonitorActivity::class.java))
        }
        binding.updateMedicalRecordCard.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, UpdateMedicalRecordActivity::class.java))
        }
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
        getUserData()


        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                buttonText = intent.getStringExtra("button_text")
                textViewText = intent.getStringExtra("text_view_text")
                textViewFont = intent.getIntExtra("text_view_font", 0)
                textViewColor = intent.getIntExtra("text_view_color", 0)
                time = intent.getStringExtra("time")
                test = intent.getStringExtra("nonsense")
                isDetected = true
                setTheColor(textViewColor!!)
                val handler = Handler()
                var isRed = true
                val colors = arrayOf(
                    R.color.color_primary,
                    R.color.white
                )
                val updateColorRunnable = object : Runnable {
                    override fun run() {
                        if (isRed) {
                            binding.fallAlertCard.setCardBackgroundColor(resources.getColor(colors[0]))
                        } else {
                            binding.fallAlertCard.setCardBackgroundColor(resources.getColor(colors[1]))
                        }
                        isRed = !isRed // Toggle the boolean flag
                        handler.postDelayed(
                            this,
                            1000
                        ) // Schedule the runnable to run again after 1 second
                    }
                }

                handler.post(updateColorRunnable)
            }
        }

        filter = IntentFilter("com.fypsensors.multihealthsensors.UPDATE_UI")
        registerReceiver(mReceiver, filter)

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {

            isDetected = true
            val handler = Handler()
            var isRed = true
            val colors = arrayOf(
                R.color.color_primary,
                R.color.white
            )
            val updateColorRunnable = object : Runnable {
                override fun run() {
                    if (isRed) {
                        binding.fallAlertCard.setCardBackgroundColor(resources.getColor(colors[0]))
                    } else {
                        binding.fallAlertCard.setCardBackgroundColor(resources.getColor(colors[1]))
                    }
                    isRed = !isRed // Toggle the boolean flag
                    handler.postDelayed(
                        this,
                        1000
                    ) // Schedule the runnable to run again after 1 second
                }
            }

            handler.post(updateColorRunnable)
        }

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
                .setContentText("Please check on the patient who might have fallen!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(12, notification)

        }
        return theColor
    }
}