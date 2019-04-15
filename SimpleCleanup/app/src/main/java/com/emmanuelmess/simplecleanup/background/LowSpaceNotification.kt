package com.emmanuelmess.simplecleanup.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.emmanuelmess.simplecleanup.Files
import com.emmanuelmess.simplecleanup.activities.MainActivity
import com.emmanuelmess.simplecleanup.R
import com.emmanuelmess.simplecleanup.helpers.isStorageFragmenting

class LowSpaceNotification(val context: Context) {
    val CHANNEL_ID = "emergency channel"
    val NOTIFICATION_ID = 0

    val builder: NotificationCompat.Builder

    init {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_description)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val isFragmenting = isStorageFragmenting(context, Files.getInternalDirectory())

        builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_error_white_24dp)
            setContentTitle(context.getString(
                    if(!isFragmenting) R.string.going_low
                    else R.string.no_space
                )
            )
            setContentText(context.getString(R.string.open_to_clean))
            priority =
                if(!isFragmenting) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_MAX
            color = context.resources.getColor(
                if(!isFragmenting) R.color.colorLowSpace
                else R.color.colorNoSpace
            )

            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
    }

    fun show() {
        // notificationId is a unique int for each notification that you must define
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun hide() {
        // notificationId is a unique int for each notification that you must define
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}