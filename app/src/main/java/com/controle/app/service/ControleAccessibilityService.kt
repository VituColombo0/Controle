package com.controle.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.controle.app.ControleApp
import com.controle.app.data.AppRule
import com.controle.app.ui.screens.ControlePauseActivity

class ControleAccessibilityService : AccessibilityService() {

    private var lastInterceptedPackage: String = ""
    private var lastInterceptTime: Long = 0L

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

        val rules = preferences.getAppRules()
        val rule = rules[packageName] ?: return

        // 2. Verifica se há uma sessão de foco global ativa
        val isGlobalFocus = sessionManager.isGlobalFocusActive()

        // 3. Se o usuário estiver dentro de um passe de 5-15 min concedido
        if (!isGlobalFocus && sessionManager.isSessionActive(packageName)) {
            // Se configurado para bloquear Reels/Shorts, inspeciona o nó de conteúdo
            if (rule.blockShortsReels && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                checkShortsOrReelsContent(rule)
            }
            return
        }

        // 4. Previne repetições e loops rápidos de eventos
        val now = System.currentTimeMillis()
        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 2500L) {
            return
        }

        // 5. Verifica se atingiu limite diário
        var isDailyLimitReached = false
        if (rule.dailyLimitMinutes > 0) {
            val usageToday = UsageTracker.getPackageUsageTodayMillis(this, packageName)
            val limitMillis = rule.dailyLimitMinutes * 60 * 1000L
            if (usageToday >= limitMillis) {
                isDailyLimitReached = true
            }
        }

        // 6. Intercepta se a pausa estiver ativada, limite diário estourado ou modo foco ativo
        if (rule.pauseEnabled || isDailyLimitReached || isGlobalFocus) {
            lastInterceptedPackage = packageName
            lastInterceptTime = now
            launchPauseScreen(rule, isDailyLimitReached, isGlobalFocus, isShortsBlocked = false)
        }
    }

    private fun launchPauseScreen(
        rule: AppRule,
        isDailyLimitReached: Boolean,
        isGlobalFocus: Boolean,
        isShortsBlocked: Boolean
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
        }
        startActivity(intent)
    }

    private fun checkShortsOrReelsContent(rule: AppRule) {
        val rootNode = rootInActiveWindow ?: return
        try {
            val isReelsOrShorts = containsKeywords(
                rootNode,
                listOf("reels", "shorts", "reel_viewer", "para você", "for you")
            )
            if (isReelsOrShorts) {
                val now = System.currentTimeMillis()
                if ((now - lastInterceptTime) > 3000L) {
                    lastInterceptTime = now
                    launchPauseScreen(rule, isDailyLimitReached = false, isGlobalFocus = false, isShortsBlocked = true)
                }
            }
        } catch (_: Exception) {
            // Nós reciclados pelo Android durante a navegação
        }
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

    private fun isSystemOrLauncher(pkg: String): Boolean {
        return pkg.contains("launcher") ||
                pkg.contains("systemui") ||
                pkg == "android" ||
                pkg.contains("inputmethod") ||
                pkg.contains("settings")
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
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
