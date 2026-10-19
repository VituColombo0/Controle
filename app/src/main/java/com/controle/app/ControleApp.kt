package com.controle.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.controle.app.data.ControlePreferences
import com.controle.app.service.SessionManager

class ControleApp : Application() {

    lateinit var preferences: ControlePreferences
        private set

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = ControlePreferences(this)
        sessionManager = SessionManager(preferences)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                "Controle - Proteção Ativa",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantém a proteção de foco ativa em segundo plano sem gastar bateria."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_FOREGROUND = "channel_controle_active"
        lateinit var instance: ControleApp
            private set
    }
}
