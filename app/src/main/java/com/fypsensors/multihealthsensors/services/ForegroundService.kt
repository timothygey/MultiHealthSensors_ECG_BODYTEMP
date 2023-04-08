package com.fypsensors.multihealthsensors.services


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import com.fypsensors.multihealthsensors.R
import com.fypsensors.multihealthsensors.activities.DashboardActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class ForegroundService : Service() {
    private var mIsRunning = false
    private var rightVersion = false
    // creating a variable for
    // our Firebase Database.
    var firebaseDatabase: FirebaseDatabase? = null
    var channel: NotificationChannel? = null
    override fun onCreate() {
        super.onCreate()
        mIsRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startID: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        run {
            channel = NotificationChannel(
                "my_channel_id",
                "my channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val notificationIntent = Intent(this, DashboardActivity::class.java)
        notificationIntent.putExtra("data", "true")
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_IMMUTABLE)

        notificationManager.createNotificationChannel(channel!!)
        val notification: Notification = Notification.Builder(this, "my_channel_id")
            .setContentText("MHS Fall Alert is running in background.")
            .setContentTitle("Fall Detection")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 500, 500)) // Vibrate for 500ms, pause for 500ms, and repeat
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .build()
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

        startForeground(NOTIFICATION_ID, notification)
        if (!mIsRunning) {
            mIsRunning = true
            val conditions: CustomModelDownloadConditions = CustomModelDownloadConditions.Builder()
                .requireWifi() // Also possible: .requireCharging() and .requireDeviceIdle()
                .build()
            FirebaseModelDownloader.getInstance()
                .getModel(
                    "fall_alert_V1",
                    DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                    conditions
                )
                .addOnSuccessListener(OnSuccessListener<CustomModel> { model -> // Download complete. Depending on your app, you could enable the ML
                    // feature, or switch from the local model to the remote model, etc.

                    // The CustomModel object contains the local path of the model file,
                    // which you can use to instantiate a TensorFlow Lite interpreter.
                    modelFile = model.file
                    if (modelFile != null) {
                        rightVersion = true
                        interpreter = Interpreter(modelFile!!)
                    }
                })
            // below line is used to get the instance
            // of our Firebase database.
            firebaseDatabase = FirebaseDatabase.getInstance()

            // below line is used to get
            // reference for our database.
            databaseReference =
                firebaseDatabase!!.reference.child("Fall_detection").child("1")

            // initializing our object class variable.


            // calling add value event listener method
            // for getting the values from database.
            databaseReference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // this method is call to get the realtime
                    // updates in the data.
                    // this method is called when the data is
                    // changed in our Firebase console.
                    // below line is for getting the data from
                    // snapshot of our database.
                    x_axis = snapshot.child("x_axis").getValue(
                        String::class.java
                    )
                    y_axis = snapshot.child("y_axis").getValue(
                        String::class.java
                    )
                    z_axis = snapshot.child("z_axis").getValue(
                        String::class.java
                    )
                    if (x_axis!!.length == 1500 && y_axis!!.length == 1500 && z_axis!!.length == 1500 && rightVersion) {
                        if (!startActivation) {
                            startActivation = true
                        } else {
                            for (i in 0..249) {
                                x_value[i] = x_axis!!.substring(i * 6, (i + 1) * 6).toFloat()
                                y_value[i] = y_axis!!.substring(i * 6, (i + 1) * 6).toFloat()
                                z_value[i] = z_axis!!.substring(i * 6, (i + 1) * 6).toFloat()
                                input_array[i * 3] = x_value[i]
                                input_array[i * 3 + 1] =
                                    y_value[i]
                                input_array[i * 3 + 2] =
                                    z_value[i]
                            }

                            // below is to put input_array into input (Bytebuffer).
                            val input =
                                ByteBuffer.allocateDirect(750 * 4).order(ByteOrder.nativeOrder())
                            input.clear()
                            for (i in 0..749) {
                                input.putFloat(input_array[i])
                            }


                            // pass the input (Bytebuffer) to the ml model ( the interpreter).
                            // results store in output_array
                            interpreter!!.run(input, output_array)
                            Log.d(
                                "TAGsss",
                                output_array[0][0].toString() + ',' + output_array[0][1].toString()
                            )
                            if (output_array[0][0] > output_array[0][1]) {
                                // no falling detected, do nothing
                                Log.e(
                                    "TAGNO",
                                    "NODETECT"
                                )
                            } else if (output_array[0][0] < output_array[0][1]) {
                                val intent = Intent("com.fypsensors.multihealthsensors.UPDATE_UI")
                                intent.putExtra(
                                    "button_text",
                                    "Activating EAS in 30 seconds!"
                                ) //in the form of pop-up notification
                                intent.putExtra(
                                    "text_view_text",
                                    "Fall Detected!"
                                ) //give user 30 seconds live countdown to press "Cancel" button
                                intent.putExtra("text_view_font", Typeface.BOLD)
                                intent.putExtra("text_view_color", Color.RED)
                                intent.putExtra("time", Calendar.getInstance().time.toString())
                                sendBroadcast(intent)
                            }
                            //Log.d("TAG", String.valueOf(input_array[749]));
                        }
                    }

                    // after getting the value we are setting
                    // our value to our text view in below line.
                }

                override fun onCancelled(error: DatabaseError) {
                    // calling on cancelled method when we receive
                    // any error or we are not able to get the data.
                    //Toast.makeText(getApplicationContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            })
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mIsRunning = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private var x_axis // x,y,z axis string data from firebase.
                : String? = null
        private var y_axis: String? = null
        private var z_axis: String? = null
        private var startActivation = false // a variable used to ignore the first time

        // running ml.
        private const val fallDetected = false // false for not detected a fall.

        // true for detected a fall.
        // set to false after pressing the button.
        private val x_value = FloatArray(250) // x,y,z axis data converted into float.
        private val y_value = FloatArray(250)
        private val z_value = FloatArray(250)
        private val input_array = FloatArray(750) // input data for ml model.
        private var interpreter // ml model interpreter.
                : Interpreter? = null
        private var modelFile // ml model file downloaded from firebase.
                : File? = null
        private val output_array = Array(1) {
            FloatArray(
                2
            )
        } // output array. should be [0.0,1.0] or [1.0,0.0].

        // creating a variable for our
        // Database Reference for Firebase.
        var databaseReference: DatabaseReference? = null
        private const val NOTIFICATION_ID = 123
    }
}


