package com.chantalbortolussi.tattooapp.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chantalbortolussi.tattooapp.MainActivity
import java.util.*

class AftercareReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.chantalbortolussi.tattooapp.ACTION_AFTERCARE_ALARM") {
            val reminderId = intent.getIntExtra("reminder_id", 1)
            triggerNotification(context, reminderId)
            
            // Self-reschedule for the next day to maintain continuous alarm chain
            rescheduleAlarmForNextDay(context, reminderId)
        } else if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all alarms upon device restart if enabled
            val sharedPrefs = context.getSharedPreferences("chantal_diary_prefs", Context.MODE_PRIVATE)
            val isEnabled = sharedPrefs.getBoolean("notificationsEnabled", false)
            val hasActiveDiary = sharedPrefs.getBoolean("hasActiveDiary", false)
            if (isEnabled && hasActiveDiary) {
                scheduleAllAlarms(context)
            }
        }
    }

    private fun triggerNotification(context: Context, reminderId: Int) {
        val channelId = "chantal_aftercare_channel"
        
        // Create Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aftercare Reminders"
            val descriptionText = "Notifiche quotidiane per la cura del tatuaggio"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Determine content details matching iOS
        val (title, body) = when (reminderId) {
            1 -> Pair(
                "Aftercare Tatuaggio 🧼",
                "È ora del lavaggio mattutino con sapone neutro e di applicare un velo sottile di crema."
            )
            2 -> Pair(
                "Idratazione Tatuaggio 🧴",
                "Pelle secca? Applica un leggero strato di crema per favorire la rigenerazione."
            )
            3 -> Pair(
                "Aftercare Serale ✨",
                "Esegui l'ultimo lavaggio delicato e applica la crema idratante prima di andare a dormire."
            )
            else -> Pair(
                "Idratazione Tatuaggio 🧴",
                "Applica la crema idratante per proteggere la pelle del tuo tatuaggio."
            )
        }

        // Click action opens MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Standard icon fallback
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(reminderId, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+ yet, ignore gracefully
        }
    }

    private fun rescheduleAlarmForNextDay(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AftercareReceiver::class.java).apply {
            action = "com.chantalbortolussi.tattooapp.ACTION_AFTERCARE_ALARM"
            putExtra("reminder_id", reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val (hour, minute) = when (reminderId) {
                1 -> Pair(10, 0)
                2 -> Pair(15, 0)
                3 -> Pair(21, 0)
                else -> Pair(12, 0)
            }
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.DATE, 1) // Force schedule tomorrow!
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun scheduleAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times = listOf(
            Triple(10, 0, 1),
            Triple(15, 0, 2),
            Triple(21, 0, 3)
        )
        for (time in times) {
            val intent = Intent(context, AftercareReceiver::class.java).apply {
                action = "com.chantalbortolussi.tattooapp.ACTION_AFTERCARE_ALARM"
                putExtra("reminder_id", time.third)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                time.third,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.first)
                set(Calendar.MINUTE, time.second)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
