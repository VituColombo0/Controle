package com.controle.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reinicia o serviço em primeiro plano do Controle automaticamente
 * quando o dispositivo é reiniciado.
 */
class ControleBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            ControleForegroundService.startService(context)
        }
    }
}
