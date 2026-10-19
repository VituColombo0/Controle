package com.controle.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ControlePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("controle_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_APP_RULES = "key_app_rules"
        private const val KEY_STATS = "key_stats"
        private const val KEY_FOCUS_SESSION = "key_focus_session"
        private const val KEY_VIBRATION = "key_vibration"
        private const val KEY_DEFAULT_PAUSE_SECONDS = "key_default_pause_seconds"
    }

    // Regras de monitoramento de aplicativos
    fun getAppRules(): Map<String, AppRule> {
        val json = prefs.getString(KEY_APP_RULES, null) ?: return defaultApps()
        val type = object : TypeToken<Map<String, AppRule>>() {}.type
        return try {
            gson.fromJson(json, type) ?: defaultApps()
        } catch (_: Exception) {
            defaultApps()
        }
    }

    fun saveAppRules(rules: Map<String, AppRule>) {
        val json = gson.toJson(rules)
        prefs.edit().putString(KEY_APP_RULES, json).apply()
    }

    fun setAppRule(rule: AppRule) {
        val current = getAppRules().toMutableMap()
        current[rule.packageName] = rule
        saveAppRules(current)
    }

    fun removeAppRule(packageName: String) {
        val current = getAppRules().toMutableMap()
        current.remove(packageName)
        saveAppRules(current)
    }

    private fun defaultApps(): Map<String, AppRule> {
        val defaults = listOf(
            AppRule("com.instagram.android", "Instagram", pauseEnabled = true, pauseDurationSeconds = 8, blockShortsReels = true),
            AppRule("com.zhiliaoapp.musically", "TikTok", pauseEnabled = true, pauseDurationSeconds = 8, blockShortsReels = true),
            AppRule("com.google.android.youtube", "YouTube", pauseEnabled = true, pauseDurationSeconds = 8, blockShortsReels = true),
            AppRule("com.twitter.android", "X / Twitter", pauseEnabled = true, pauseDurationSeconds = 8),
            AppRule("com.reddit.frontpage", "Reddit", pauseEnabled = true, pauseDurationSeconds = 8),
            AppRule("com.facebook.katana", "Facebook", pauseEnabled = true, pauseDurationSeconds = 8)
        )
        return defaults.associateBy { it.packageName }
    }

    // Estatísticas de tempo salvo e streak
    fun getStats(): ControleStats {
        val json = prefs.getString(KEY_STATS, null) ?: return ControleStats(lastActiveDate = todayDate())
        return try {
            gson.fromJson(json, ControleStats::class.java) ?: ControleStats(lastActiveDate = todayDate())
        } catch (_: Exception) {
            ControleStats(lastActiveDate = todayDate())
        }
    }

    fun recordPreventedOpen(minutesSavedEstimate: Int = 12) {
        val current = getStats()
        val today = todayDate()
        val updated = current.copy(
            minutesSaved = current.minutesSaved + minutesSavedEstimate,
            opensPrevented = current.opensPrevented + 1,
            breathsCompleted = current.breathsCompleted + 1,
            lastActiveDate = today
        )
        saveStats(updated)
    }

    fun recordCompletedBreath() {
        val current = getStats()
        val updated = current.copy(
            breathsCompleted = current.breathsCompleted + 1,
            lastActiveDate = todayDate()
        )
        saveStats(updated)
    }

    private fun saveStats(stats: ControleStats) {
        val json = gson.toJson(stats)
        prefs.edit().putString(KEY_STATS, json).apply()
    }

    // Sessão de Foco
    fun getFocusSession(): FocusSession {
        val json = prefs.getString(KEY_FOCUS_SESSION, null) ?: return FocusSession()
        return try {
            gson.fromJson(json, FocusSession::class.java) ?: FocusSession()
        } catch (_: Exception) {
            FocusSession()
        }
    }

    fun saveFocusSession(session: FocusSession) {
        val json = gson.toJson(session)
        prefs.edit().putString(KEY_FOCUS_SESSION, json).apply()
    }

    // Ajustes
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()

    private fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
