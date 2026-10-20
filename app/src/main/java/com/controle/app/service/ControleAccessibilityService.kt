package com.controle.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.controle.app.ControleApp
import com.controle.app.data.AppRule
import com.controle.app.ui.screens.ControlePauseActivity
import java.util.Calendar

class ControleAccessibilityService : AccessibilityService() {

    private var lastInterceptedPackage: String = ""
    private var lastInterceptTime: Long = 0L
    private var currentForegroundPackage: String = ""
    private var foregroundEntryTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        // Inicia o serviço em primeiro plano para garantir longevidade
        ControleForegroundService.startService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val app = ControleApp.instance
        val preferences = app.preferences
        val sessionManager = app.sessionManager

        val packageName = event.packageName?.toString() ?: return

        // 1. Ignora o próprio aplicativo e componentes do sistema
        if (packageName == applicationContext.packageName) return
        if (isSystemOrLauncher(packageName)) return

        // 2. Detecta mudança de app em primeiro plano para tracking de reminder
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName != currentForegroundPackage) {
                currentForegroundPackage = packageName
                foregroundEntryTime = System.currentTimeMillis()
            }
        }

        val rules = preferences.getAppRules()
        val rule = rules[packageName]

        // 3. Verificar bloqueio de websites no navegador
        if (isBrowserPackage(packageName) && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            checkBlockedWebsites(preferences.getWebsiteBlocks().filter { it.isEnabled }.map { it.url })
        }

        // Se não há regra para este pacote, sair
        if (rule == null) return

        // 4. Verificar agendamento (schedule) - se o app está bloqueado por horário
        val isScheduleBlocked = rule.scheduleEnabled && isWithinSchedule(
            rule.scheduleStartHour, rule.scheduleStartMinute,
            rule.scheduleEndHour, rule.scheduleEndMinute
        )

        // 5. Verifica se há uma sessão de foco global ativa
        val isGlobalFocus = sessionManager.isGlobalFocusActive()

        // 6. Se o usuário estiver dentro de um passe temporário
        if (!isGlobalFocus && !isScheduleBlocked && sessionManager.isSessionActive(packageName)) {
            // Se configurado para bloquear Reels/Shorts, inspeciona o nó de conteúdo
            if (rule.blockShortsReels && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                checkShortsOrReelsContent(rule)
            }

            // 7. Verificar sistema de lembretes (reminder)
            if (rule.reminderMinutes > 0 && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                checkReminderTrigger(rule, preferences)
            }
            return
        }

        // 8. Previne repetições e loops rápidos de eventos
        val now = System.currentTimeMillis()
        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 2500L) {
            return
        }

        // 9. Verifica se atingiu limite diário
        var isDailyLimitReached = false
        if (rule.dailyLimitMinutes > 0) {
            val usageToday = UsageTracker.getPackageUsageTodayMillis(this, packageName)
            val limitMillis = rule.dailyLimitMinutes * 60 * 1000L
            if (usageToday >= limitMillis) {
                isDailyLimitReached = true
            }
        }

        // 10. Intercepta se a pausa estiver ativada, limite diário, modo foco, ou schedule
        if (rule.pauseEnabled || isDailyLimitReached || isGlobalFocus || isScheduleBlocked) {
            lastInterceptedPackage = packageName
            lastInterceptTime = now
            launchPauseScreen(
                rule = rule,
                isDailyLimitReached = isDailyLimitReached,
                isGlobalFocus = isGlobalFocus,
                isShortsBlocked = false,
                isScheduleBlocked = isScheduleBlocked,
                isReminder = false
            )
        }
    }

    private fun checkReminderTrigger(rule: AppRule, preferences: com.controle.app.data.ControlePreferences) {
        val now = System.currentTimeMillis()
        val lastShown = preferences.getReminderLastShown(rule.packageName)
        val reminderIntervalMillis = rule.reminderMinutes * 60 * 1000L

        // Verifica quanto tempo está no app desde que entrou
        if (currentForegroundPackage == rule.packageName && foregroundEntryTime > 0) {
            val timeInApp = now - foregroundEntryTime
            if (timeInApp >= reminderIntervalMillis && (now - lastShown) >= reminderIntervalMillis) {
                preferences.setReminderLastShown(rule.packageName, now)
                lastInterceptTime = now
                launchPauseScreen(
                    rule = rule,
                    isDailyLimitReached = false,
                    isGlobalFocus = false,
                    isShortsBlocked = false,
                    isScheduleBlocked = false,
                    isReminder = true
                )
            }
        }
    }

    private fun isWithinSchedule(startH: Int, startM: Int, endH: Int, endM: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentH = calendar.get(Calendar.HOUR_OF_DAY)
        val currentM = calendar.get(Calendar.MINUTE)
        val currentMinutes = currentH * 60 + currentM
        val startMinutes = startH * 60 + startM
        val endMinutes = endH * 60 + endM

        return if (startMinutes <= endMinutes) {
            // Ex: 08:00 até 17:00
            currentMinutes in startMinutes..endMinutes
        } else {
            // Ex: 22:00 até 07:00 (atravessa meia-noite)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    private fun launchPauseScreen(
        rule: AppRule,
        isDailyLimitReached: Boolean,
        isGlobalFocus: Boolean,
        isShortsBlocked: Boolean,
        isScheduleBlocked: Boolean,
        isReminder: Boolean
    ) {
        val intent = Intent(this, ControlePauseActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(ControlePauseActivity.EXTRA_PACKAGE_NAME, rule.packageName)
            putExtra(ControlePauseActivity.EXTRA_APP_NAME, rule.appName)
            putExtra(ControlePauseActivity.EXTRA_PAUSE_SECONDS, rule.pauseDurationSeconds)
            putExtra(ControlePauseActivity.EXTRA_IS_LIMIT_REACHED, isDailyLimitReached)
            putExtra(ControlePauseActivity.EXTRA_IS_GLOBAL_FOCUS, isGlobalFocus)
            putExtra(ControlePauseActivity.EXTRA_IS_SHORTS_BLOCKED, isShortsBlocked)
            putExtra(ControlePauseActivity.EXTRA_IS_STRICT, rule.isStrict)
            putExtra(ControlePauseActivity.EXTRA_STRICT_PHRASE, rule.strictPhrase)
            putExtra(ControlePauseActivity.EXTRA_IS_SCHEDULE_BLOCKED, isScheduleBlocked)
            putExtra(ControlePauseActivity.EXTRA_IS_REMINDER, isReminder)
        }
        startActivity(intent)
    }

    private fun checkShortsOrReelsContent(rule: AppRule) {
        val rootNode = rootInActiveWindow ?: return
        try {
            val isReelsOrShorts = containsKeywords(
                rootNode,
                listOf(
                    "reels", "shorts", "reel_viewer", "para você", "for you",
                    "short_video", "reels_viewer", "clips_tab", "reel_tab",
                    "youtube_shorts", "shorts_container", "reel_container"
                )
            )
            if (isReelsOrShorts) {
                val now = System.currentTimeMillis()
                if ((now - lastInterceptTime) > 3000L) {
                    lastInterceptTime = now
                    launchPauseScreen(
                        rule = rule,
                        isDailyLimitReached = false,
                        isGlobalFocus = false,
                        isShortsBlocked = true,
                        isScheduleBlocked = false,
                        isReminder = false
                    )
                }
            }
        } catch (_: Exception) {
            // Nós reciclados pelo Android durante a navegação
        }
    }

    private fun checkBlockedWebsites(blockedUrls: List<String>) {
        if (blockedUrls.isEmpty()) return
        val rootNode = rootInActiveWindow ?: return
        try {
            val urlBarText = findUrlBarText(rootNode)
            if (urlBarText.isNotEmpty()) {
                val isBlocked = blockedUrls.any { blocked ->
                    urlBarText.contains(blocked, ignoreCase = true)
                }
                if (isBlocked) {
                    val now = System.currentTimeMillis()
                    if ((now - lastInterceptTime) > 3000L) {
                        lastInterceptTime = now
                        val rule = AppRule(
                            packageName = currentForegroundPackage,
                            appName = "Website Bloqueado",
                            pauseEnabled = false
                        )
                        launchPauseScreen(
                            rule = rule,
                            isDailyLimitReached = true,
                            isGlobalFocus = false,
                            isShortsBlocked = false,
                            isScheduleBlocked = false,
                            isReminder = false
                        )
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun findUrlBarText(node: AccessibilityNodeInfo): String {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        // IDs comuns de barras de URL em navegadores
        if (viewId.contains("url_bar") || viewId.contains("url_field") ||
            viewId.contains("omnibox") || viewId.contains("search_box") ||
            viewId.contains("address_bar") || viewId.contains("url_edit") ||
            viewId.contains("com.android.chrome:id/url_bar") ||
            viewId.contains("com.android.chrome:id/search_box_text")
        ) {
            return node.text?.toString() ?: ""
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findUrlBarText(child)
            if (found.isNotEmpty()) return found
        }
        return ""
    }

    private fun containsKeywords(node: AccessibilityNodeInfo, keywords: List<String>): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        for (kw in keywords) {
            if (text.contains(kw) || desc.contains(kw) || viewId.contains(kw)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (containsKeywords(child, keywords)) {
                return true
            }
        }
        return false
    }

    private fun isBrowserPackage(pkg: String): Boolean {
        return pkg.contains("chrome") ||
                pkg.contains("browser") ||
                pkg.contains("firefox") ||
                pkg.contains("opera") ||
                pkg.contains("samsung.sbrowser") ||
                pkg.contains("brave") ||
                pkg.contains("edge")
    }

    private fun isSystemOrLauncher(pkg: String): Boolean {
        return pkg.contains("launcher") ||
                pkg.contains("systemui") ||
                pkg == "android" ||
                pkg.contains("inputmethod") ||
                pkg.contains("settings") ||
                pkg.contains("com.android.vending") // Play Store
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        // Tenta reiniciar o serviço
        ControleForegroundService.startService(applicationContext)
    }

    companion object {
        var instance: ControleAccessibilityService? = null
            private set

        /**
         * Executa o comando de voltar para a tela inicial instantaneamente via acessibilidade.
         */
        fun goHomeViaAccessibility(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }
    }
}
