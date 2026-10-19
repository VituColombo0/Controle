package com.controle.app.data

data class AppRule(
    val packageName: String,
    val appName: String,
    val pauseEnabled: Boolean = true,
    val pauseDurationSeconds: Int = 8,
    val dailyLimitMinutes: Int = 0, // 0 = sem limite
    val blockShortsReels: Boolean = false,
    val isStrict: Boolean = false,
    val strictPhrase: String = "Eu escolho conscientemente abrir este app"
)

data class ControleStats(
    val minutesSaved: Int = 0,
    val breathsCompleted: Int = 0,
    val opensPrevented: Int = 0,
    val streakDays: Int = 1,
    val lastActiveDate: String = ""
)

data class FocusSession(
    val isActive: Boolean = false,
    val durationMinutes: Int = 25,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val sessionName: String = "Sessão de Foco"
)
