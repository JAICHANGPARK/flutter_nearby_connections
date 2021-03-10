package com.nankai.flutter_nearby_connections

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

object Actions {
    private const val prefix = "com.nankai.flutter_nearby_connections.action."
    const val MAIN = prefix + "main"
    const val PREV = prefix + "prev"
    const val NEXT = prefix + "next"
    const val PLAY = prefix + "play"
    const val START_FOREGROUND = prefix + "startforeground"
    const val STOP_FOREGROUND = prefix + "stopforeground"
}

class NearbyService : Service() {
    private val binder: IBinder = LocalBinder(this)
    private lateinit var callbackUtils: CallbackUtils
    private lateinit var connectionsClient: ConnectionsClient

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, ">>> NearbyService onCreate()")
        stopForeground(true)
        startForeground(NOTIFICATION_ID, getNotification())
    }

    fun initService(callbackUtils: CallbackUtils) {
        connectionsClient = Nearby.getConnectionsClient(this)
        this@NearbyService.callbackUtils = callbackUtils
    }

    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    override fun stopService(name: Intent?): Boolean {
        Log.e(TAG, "stopService Action Received = ${name?.action}")
        when (name?.action) {
            Actions.START_FOREGROUND -> {
                Log.e(TAG, "Start Foreground 인텐트를 받음")
//                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                Log.e(TAG, "Stop Foreground 인텐트를 받음")
                stopForegroundService()
            }
            Actions.PREV -> Log.e(TAG, "Clicked = 이전")
            Actions.PLAY -> Log.e(TAG, "Clicked = 재생")
            Actions.NEXT -> Log.e(TAG, "Clicked = 다음")
        }
        return true;
    }

    fun sendStringPayload(endpointId: String, str: String) {
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
    }

    fun startAdvertising(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startAdvertising()")
        connectionsClient.startAdvertising(
                deviceName, SERVICE_ID, callbackUtils.connectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    fun startDiscovery(strategy: Strategy) {
        Log.d(TAG, "startDiscovery()")
        connectionsClient.startDiscovery(
                SERVICE_ID, callbackUtils.endpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
    }

    fun connect(endpointId: String, displayName: String) {
        connectionsClient.requestConnection(displayName, endpointId, callbackUtils.connectionLifecycleCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "Action Received = ${intent?.action}")
        // intent가 시스템에 의해 재생성되었을때 null값이므로 Java에서는 null check 필수
        when (intent?.action) {
            Actions.START_FOREGROUND -> {
                Log.e(TAG, "Start Foreground 인텐트를 받음")
//                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                Log.e(TAG, "Stop Foreground 인텐트를 받음")
                stopForegroundService()
            }
            Actions.PREV -> Log.e(TAG, "Clicked = 이전")
            Actions.PLAY -> Log.e(TAG, "Clicked = 재생")
            Actions.NEXT -> Log.e(TAG, "Clicked = 다음")
        }
        return START_STICKY
    }


    override fun onDestroy() {
        Log.d(TAG, ">>> NearbyService onDestroy()")
        stopForeground(true)
        stopAdvertising()
        stopDiscovery()
        connectionsClient.stopAllEndpoints()
        super.onDestroy()

    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }


    private fun getNotification(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nearby Service")
                .setContentText("Wi-Fi Direct")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build()
    }
}

internal class LocalBinder(private val nearbyService: NearbyService) : Binder() {
    val service: NearbyService
        get() = nearbyService
}