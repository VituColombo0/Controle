package com.controle.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.data.AppRule
import com.controle.app.ui.theme.AmberAlert
import com.controle.app.ui.theme.MintDark
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SkyBlue
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.SurfaceElevated
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextMuted
import com.controle.app.ui.theme.TextWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val appName: String
)

@Composable
fun AppsSelectionScreen() {
    val context = LocalContext.current
    val prefs = ControleApp.instance.preferences

    val rules = remember { mutableStateMapOf<String, AppRule>() }
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        rules.putAll(prefs.getAppRules())

        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pkgAppsList = pm.queryIntentActivities(mainIntent, 0)
            val apps = pkgAppsList
                .map {
                    InstalledApp(
                        packageName = it.activityInfo.packageName,
                        appName = it.loadLabel(pm).toString()
                    )
                }
                .filter { it.packageName != context.packageName }
                .distinctBy { it.packageName }
                .sortedBy { it.appName.lowercase() }

            installedApps = apps
        }
    }

    val filtered = if (searchQuery.isBlank()) {
        installedApps
    } else {
        installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aplicativos & Limites",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Ative a pausa consciente e configure regras individuais",
            fontSize = 13.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Busca
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar aplicativo...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextGray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = MintNeon,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val currentRule = rules[app.packageName]
                val isMonitored = currentRule != null && currentRule.pauseEnabled

                AppConfigCard(
                    app = app,
                    rule = currentRule,
                    isMonitored = isMonitored,
                    onToggleMonitor = { enabled ->
                        val updated = (currentRule ?: AppRule(
                            packageName = app.packageName,
                            appName = app.appName
                        )).copy(pauseEnabled = enabled)

                        if (enabled) {
                            rules[app.packageName] = updated
                            prefs.setAppRule(updated)
                        } else {
                            rules.remove(app.packageName)
                            prefs.removeAppRule(app.packageName)
                        }
                    },
                    onUpdateRule = { updated ->
                        rules[app.packageName] = updated
                        prefs.setAppRule(updated)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun AppConfigCard(
    app: InstalledApp,
    rule: AppRule?,
    isMonitored: Boolean,
    onToggleMonitor: (Boolean) -> Unit,
    onUpdateRule: (AppRule) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isMonitored) MintNeon.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isMonitored) MintDark.copy(alpha = 0.5f) else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = if (isMonitored) MintNeon else TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = app.appName,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isMonitored) "Pausa consciente ativada" else "Sem intervenção",
                            color = if (isMonitored) MintNeon else TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isMonitored,
                    onCheckedChange = onToggleMonitor,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = MintNeon,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }

            if (isMonitored && rule != null) {
                Spacer(modifier = Modifier.height(14.dp))

                // Seletor de Limite Diário
                Text(
                    text = "Limite diário:",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0 to "Off", 15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (min, label) ->
                        val isSelected = rule.dailyLimitMinutes == min
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MintDark.copy(alpha = 0.5f) else SurfaceElevated)
                                .border(1.dp, if (isSelected) MintNeon else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onUpdateRule(rule.copy(dailyLimitMinutes = min)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MintNeon else TextGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Opção de Bloquear Reels & Shorts
                val isShortsPlatform = app.packageName.contains("instagram") ||
                        app.packageName.contains("youtube") ||
                        app.packageName.contains("musically")

                if (isShortsPlatform) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bloquear apenas feed de Reels/Shorts",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = rule.blockShortsReels,
                            onCheckedChange = { onUpdateRule(rule.copy(blockShortsReels = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureBlack,
                                checkedTrackColor = SkyBlue
                            )
                        )
                    }
                }

                // Lembrete de Tempo (Reminder)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (rule.reminderMinutes > 0) AmberAlert else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lembrete após:",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0 to "Off", 5 to "5m", 10 to "10m", 15 to "15m", 30 to "30m").forEach { (min, label) ->
                        val isSelected = rule.reminderMinutes == min
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MintDark.copy(alpha = 0.5f) else SurfaceElevated)
                                .border(1.dp, if (isSelected) AmberAlert.copy(alpha = 0.7f) else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onUpdateRule(rule.copy(reminderMinutes = min)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) AmberAlert else TextGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Agendamento por Horário (Schedule)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (rule.scheduleEnabled) SkyBlue else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Bloqueio por horário",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            if (rule.scheduleEnabled) {
                                Text(
                                    text = "${String.format("%02d", rule.scheduleStartHour)}:${String.format("%02d", rule.scheduleStartMinute)} " +
                                            "→ ${String.format("%02d", rule.scheduleEndHour)}:${String.format("%02d", rule.scheduleEndMinute)}",
                                    color = SkyBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Switch(
                        checked = rule.scheduleEnabled,
                        onCheckedChange = { onUpdateRule(rule.copy(scheduleEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = SkyBlue
                        )
                    )
                }

                // Seletor de horário quando schedule habilitado
                if (rule.scheduleEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Presets de horário
                        listOf(
                            Triple(22, 0, 7) to "22h-07h",
                            Triple(23, 0, 6) to "23h-06h",
                            Triple(0, 0, 8) to "00h-08h",
                            Triple(8, 0, 17) to "08h-17h"
                        ).forEach { (times, label) ->
                            val (h1, m1, h2) = times
                            val isSelected = rule.scheduleStartHour == h1 &&
                                    rule.scheduleStartMinute == m1 &&
                                    rule.scheduleEndHour == h2
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MintDark.copy(alpha = 0.5f) else SurfaceElevated)
                                    .border(1.dp, if (isSelected) SkyBlue else SurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onUpdateRule(
                                            rule.copy(
                                                scheduleStartHour = h1,
                                                scheduleStartMinute = m1,
                                                scheduleEndHour = h2,
                                                scheduleEndMinute = 0
                                            )
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) SkyBlue else TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Opção de Modo Estrito (Obriga a digitar frase para desbloquear)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (rule.isStrict) MintNeon else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Modo Estrito (Digitar frase para liberar)",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = rule.isStrict,
                        onCheckedChange = { onUpdateRule(rule.copy(isStrict = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = MintNeon
                        )
                    )
                }
            }
        }
    }
}
