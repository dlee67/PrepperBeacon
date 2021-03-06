package com.example.bunkerbeacon.fragments

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bunkerbeacon.receiver.AlarmReceiver
import com.example.bunkerbeacon.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val NOTIFICATION_ID = 0
    val PRIMARY_CHANNEL_ID = "primary_notification_channel"

    lateinit var notificationManager: NotificationManager
    lateinit var firstNumberView: EditText
    lateinit var secondNumberView: EditText
    lateinit var daysBeforePanicView: EditText
    lateinit var panicDate: TextView

    var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val homeFragment = inflater.inflate(R.layout.fragment_home, container, false)
        initClassVars(homeFragment)

        var alarmManager = context?.getSystemService(ALARM_SERVICE) as AlarmManager

        homeFragment.findViewById<Button>(R.id.sendButton).setOnClickListener {
            Log.i("dhl", "Sending message")

            var firstNumber = firstNumberView.text
            var secondNumber = secondNumberView.text
            var notifyIntent = Intent(context, AlarmReceiver::class.java)
            notifyIntent.putExtra("firstNumber", firstNumber.toString())
            notifyIntent.putExtra("secondNumber", secondNumber.toString())
            var pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID, notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            if(daysBeforePanicView.text.toString() != "" &&
                (firstNumber.toString() != "" || secondNumber.toString() != "")) {
                var amountOfDays = daysBeforePanicView.text.toString()
                var preferenceEdit = preferences?.edit()
                Log.i("dhl", "Amount of days before panic at: " + amountOfDays)

                var calendar = Calendar.getInstance()
                calendar.add(Calendar.DATE, daysBeforePanicView.text.toString().toInt())
                panicDate.text = "Will panic on: " + calendar.time.toString()
                preferenceEdit?.putString("persist-string-panic-date", "Will panic on: " + calendar.time.toString())
                preferenceEdit?.commit()

                Log.i("dhl", "Calendar at: " + calendar.time.toString());

                var waitInterval = TimeUnit.DAYS.toMillis(amountOfDays.toLong())

                if (alarmManager != null) {
                    alarmManager?.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            // elapsedRealtime() is required to actually the correct amount of pass.
                            SystemClock.elapsedRealtime() + 10000, // 10 seconds
                            pendingIntent)
                }

                Toast.makeText(context,
                        "Don't forget to check-in daily",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        "Gotta have the amount of days and gotta at least have one phone number",
                        Toast.LENGTH_SHORT).show()
            }
        }

        createNotificationChannel()

        // Inflate the layout for this fragment
        return homeFragment
    }

    fun createNotificationChannel() {
        notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Stand up notification",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notification test description"
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun initClassVars(homeFragment: View) {
        notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        firstNumberView = homeFragment.findViewById(R.id.first_phone_number)
        secondNumberView = homeFragment.findViewById(R.id.second_phone_number)
        daysBeforePanicView = homeFragment.findViewById(R.id.amt_of_days_before_panic)
        panicDate = homeFragment.findViewById(R.id.amt_until_panic_text)
        preferences = activity?.getPreferences(MODE_PRIVATE)

        panicDate.text = preferences?.getString("persist-string-panic-date",
            "Calm like the falling snow")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}