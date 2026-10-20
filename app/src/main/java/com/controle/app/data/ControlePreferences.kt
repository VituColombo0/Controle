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
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_WEBSITE_BLOCKS = "key_website_blocks"
        private const val KEY_REMINDER_LAST_SHOWN = "key_reminder_last_shown"
    }

    // Onboarding
    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone() = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()

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
        val json = prefs.getString(KEY_STATS, null) ?: return ControleStats(
            lastActiveDate = todayDate(),
            statsDate = todayDate()
        )
        return try {
            val stats = gson.fromJson(json, ControleStats::class.java) ?: ControleStats(
                lastActiveDate = todayDate(),
                statsDate = todayDate()
            )
            // Auto-reset diário: se mudou o dia, zera minutesSaved, breathsCompleted, opensPrevented
            val today = todayDate()
            if (stats.statsDate != today) {
                val newStreak = calculateStreak(stats, today)
                val reset = stats.copy(
                    minutesSaved = 0,
                    breathsCompleted = 0,
                    opensPrevented = 0,
                    streakDays = newStreak,
                    lastActiveDate = today,
                    statsDate = today
                )
                saveStats(reset)
                return reset
            }
            stats
        } catch (_: Exception) {
            ControleStats(lastActiveDate = todayDate(), statsDate = todayDate())
        }
    }

    private fun calculateStreak(stats: ControleStats, today: String): Int {
        if (stats.lastActiveDate.isEmpty()) return 1
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val last = format.parse(stats.lastActiveDate)
            val now = format.parse(today)
            if (last != null && now != null) {
                val diffDays = ((now.time - last.time) / (1000 * 60 * 60 * 24)).toInt()
                when {
                    diffDays <= 1 -> stats.streakDays + if (diffDays == 1) 1 else 0
                    else -> 1 // Quebrou a sequência
                }
            } else 1
        } catch (_: Exception) {
            1
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
            val session = gson.fromJson(json, FocusSession::class.java) ?: FocusSession()
            // Auto-desativa se o tempo já expirou
            if (session.isActive && System.currentTimeMillis() >= session.endTimeMillis) {
                val ended = session.copy(isActive = false)
                saveFocusSession(ended)
                return ended
            }
            session
        } catch (_: Exception) {
            FocusSession()
        }
    }

    fun saveFocusSession(session: FocusSession) {
        val json = gson.toJson(session)
        prefs.edit().putString(KEY_FOCUS_SESSION, json).apply()
    }

    // Bloqueio de Websites
    fun getWebsiteBlocks(): List<WebsiteBlock> {
        val json = prefs.getString(KEY_WEBSITE_BLOCKS, null) ?: return emptyList()
        val type = object : TypeToken<List<WebsiteBlock>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveWebsiteBlocks(blocks: List<WebsiteBlock>) {
        val json = gson.toJson(blocks)
        prefs.edit().putString(KEY_WEBSITE_BLOCKS, json).apply()
    }

    fun addWebsiteBlock(url: String) {
        val current = getWebsiteBlocks().toMutableList()
        if (current.none { it.url.equals(url, ignoreCase = true) }) {
            current.add(WebsiteBlock(url = url))
            saveWebsiteBlocks(current)
        }
    }

    fun removeWebsiteBlock(url: String) {
        val current = getWebsiteBlocks().toMutableList()
        current.removeAll { it.url.equals(url, ignoreCase = true) }
        saveWebsiteBlocks(current)
    }

    // Lembretes - rastreia quando foi o último popup de lembrete para cada app
    fun getReminderLastShown(packageName: String): Long {
        return prefs.getLong("${KEY_REMINDER_LAST_SHOWN}_$packageName", 0L)
    }

    fun setReminderLastShown(packageName: String, timeMillis: Long) {
        prefs.edit().putLong("${KEY_REMINDER_LAST_SHOWN}_$packageName", timeMillis).apply()
    }

    // Ajustes
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()

    fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
