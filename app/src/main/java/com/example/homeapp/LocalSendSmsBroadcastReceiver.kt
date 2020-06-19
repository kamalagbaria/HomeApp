package com.example.homeapp

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class LocalSendSmsBroadcastReceiver : BroadcastReceiver() {
    private var PHONE: String? = null
    private var CONTENT: String? = null
    private val channelId = "CHANNEL_ID_FOR_NONIMPORTANT_NOTIFICATIONS"


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "POST_PC.ACTION_SEND_SMS") {
            PHONE = intent.getStringExtra("PH")
            CONTENT = intent.getStringExtra("text")
            SmsManager.getDefault().sendTextMessage(PHONE, null, CONTENT, null, null);
            fireNotification("Sending Sms: "+CONTENT,context)
        }
    }
    fun fireNotification(msg: String,context: Context){
        createChannelIfNotExists(context)
        actualFire(msg,context)
    }

    fun createChannelIfNotExists(context:Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notificationChannels.forEach { channel ->
                if (channel.id == channelId) {
                    return
                }
            }

            // Create the NotificationChannel
            val name = "non-important"
            val descriptionText = "channel forr non important notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun actualFire(msg: String,context:Context) {
        val intentToOpenBlue = Intent(context, MainActivity::class.java)
        intentToOpenBlue.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pending = PendingIntent.getActivity(context, 123, intentToOpenBlue, 0)

        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .build()


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        notificationManager.notify(123, notification)

    }
}
