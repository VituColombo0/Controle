package com.controle.app.service

import com.controle.app.data.ControlePreferences
import java.util.concurrent.ConcurrentHashMap

class SessionManager(private val preferences: ControlePreferences) {

    // Mapa de: packageName -> timestamp (milissegundos) quando a sessão expira
    private val activeSessions = ConcurrentHashMap<String, Long>()

    /**
     * Libera o uso do aplicativo por X minutos (ex: 5 min, 10 min, 15 min).
     */
    fun grantSession(packageName: String, durationMinutes: Int) {
        val expireAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        activeSessions[packageName] = expireAt
    }

    /**
     * Verifica se o aplicativo possui um passe temporário ativo e válido.
     */
    fun isSessionActive(packageName: String): Boolean {
        val expireAt = activeSessions[packageName] ?: return false
        val now = System.currentTimeMillis()
        if (now < expireAt) {
            return true
        }
        activeSessions.remove(packageName)
        return false
    }

    /**
     * Retorna quantos segundos restam no passe de uso temporário.
     */
    fun getRemainingSeconds(packageName: String): Long {
        val expireAt = activeSessions[packageName] ?: return 0L
        val diff = (expireAt - System.currentTimeMillis()) / 1000L
        return if (diff > 0) diff else 0L
    }

    /**
     * Revoga a sessão de um pacote imediatamente.
     */
    fun revokeSession(packageName: String) {
        activeSessions.remove(packageName)
    }

    /**
     * Verifica se o Modo Foco global está em vigor.
     */
    fun isGlobalFocusActive(): Boolean {
        val session = preferences.getFocusSession()
        if (!session.isActive) return false
        val now = System.currentTimeMillis()
        return now < session.endTimeMillis
    }
}
