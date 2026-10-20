package com.controle.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.data.ControleStats
import com.controle.app.data.FocusSession
import com.controle.app.service.UsageTracker
import com.controle.app.ui.theme.AmberAlert
import com.controle.app.ui.theme.CoralWarning
import com.controle.app.ui.theme.MintGlow
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SkyBlue
import com.controle.app.ui.theme.SkyGlow
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.SurfaceElevated
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onNavigateToApps: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    val context = LocalContext.current
    val prefs = ControleApp.instance.preferences
    val sessionManager = ControleApp.instance.sessionManager

    var stats by remember { mutableStateOf(prefs.getStats()) }
    var totalScreenTimeMillis by remember { mutableLongStateOf(0L) }
    var focusSession by remember { mutableStateOf(prefs.getFocusSession()) }
    var rulesCount by remember { mutableStateOf(0) }

    // Refresh periódico a cada 10 segundos
    LaunchedEffect(Unit) {
        while (true) {
            stats = prefs.getStats()
            totalScreenTimeMillis = UsageTracker.getTotalScreenTimeTodayMillis(context)
            focusSession = prefs.getFocusSession()
            rulesCount = prefs.getAppRules().count { it.value.pauseEnabled }
            delay(10_000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Cabeçalho Principal & Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Controle",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Tempo de Tela Consciente",
                    fontSize = 13.sp,
                    color = TextGray
                )
            }

            // Streak Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("🔥", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${stats.streakDays} dias",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Card: Tempo Economizado no Dia
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(MintNeon.copy(alpha = 0.5f), SurfaceBorder)),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MintGlow.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                    .padding(22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TEMPO POUPADO HOJE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintNeon,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MintNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val hoursSaved = stats.minutesSaved / 60
                val minsSaved = stats.minutesSaved % 60
                val displaySaved = if (hoursSaved > 0) "${hoursSaved}h ${minsSaved}m" else "${stats.minutesSaved} min"

                Text(
                    text = displaySaved,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tempo recuperado por desistir de aberturas impulsivas após respirar.",
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Cards de Métricas Principais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricPillCard(
                title = "Respirações",
                value = stats.breathsCompleted.toString(),
                icon = Icons.Default.SelfImprovement,
                iconTint = MintNeon,
                modifier = Modifier.weight(1f)
            )
            MetricPillCard(
                title = "Desistências",
                value = stats.opensPrevented.toString(),
                icon = Icons.Default.Block,
                iconTint = SkyBlue,
                modifier = Modifier.weight(1f)
            )
            MetricPillCard(
                title = "Tela Hoje",
                value = if (totalScreenTimeMillis > 0) UsageTracker.formatDuration(totalScreenTimeMillis) else "0m",
                icon = Icons.Default.Smartphone,
                iconTint = TextGray,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status de proteção
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MintNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$rulesCount apps monitorados • ${prefs.getWebsiteBlocks().size} sites bloqueados",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Card de Sessão de Foco Rápido
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFocus() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkyGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Modo Foco / Offtime",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (sessionManager.isGlobalFocusActive()) "Foco em andamento" else "Bloqueio temporário total",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                    Text(
                        text = "Ver mais ›",
                        color = SkyBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (sessionManager.isGlobalFocusActive()) {
                    Button(
                        onClick = {
                            prefs.saveFocusSession(FocusSession(isActive = false))
                            focusSession = prefs.getFocusSession()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralWarning),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encerrar Modo Foco")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 25, 50).forEach { mins ->
                            Button(
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    val session = FocusSession(
                                        isActive = true,
                                        durationMinutes = mins,
                                        startTimeMillis = now,
                                        endTimeMillis = now + (mins * 60 * 1000L)
                                    )
                                    prefs.saveFocusSession(session)
                                    focusSession = session
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${mins}m", color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Ações Rápidas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToApps,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Text("Gerenciar Apps", color = TextWhite, fontSize = 13.sp)
            }

            Button(
                onClick = onNavigateToPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Text("Configurações", color = TextWhite, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun MetricPillCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = modifier.border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextGray
            )
        }
    }
}
