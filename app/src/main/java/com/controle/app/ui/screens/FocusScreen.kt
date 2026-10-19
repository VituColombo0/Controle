package com.controle.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.data.FocusSession
import com.controle.app.ui.theme.CoralWarning
import com.controle.app.ui.theme.MintGlow
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SkyBlue
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.SurfaceElevated
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun FocusScreen() {
    val prefs = ControleApp.instance.preferences
    var focusSession by remember { mutableStateOf(prefs.getFocusSession()) }
    var secondsRemaining by remember { mutableLongStateOf(0L) }

    LaunchedEffect(focusSession) {
        if (focusSession.isActive) {
            val now = System.currentTimeMillis()
            val diff = (focusSession.endTimeMillis - now) / 1000L
            secondsRemaining = if (diff > 0) diff else 0L

            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining--
            }

            if (secondsRemaining <= 0) {
                prefs.saveFocusSession(FocusSession(isActive = false))
                focusSession = prefs.getFocusSession()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sessão de Foco",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Bloqueio total de distrações para entrar em estado de fluxo",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Timer Circular de Foco
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(2.dp, if (focusSession.isActive) MintNeon else SurfaceBorder, CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = if (focusSession.isActive) MintNeon else TextGray,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val mins = secondsRemaining / 60
                val secs = secondsRemaining % 60
                val timeFormatted = String.format("%02d:%02d", mins, secs)

                Text(
                    text = if (focusSession.isActive) timeFormatted else "Pronto",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = if (focusSession.isActive) "Tempo restante" else "Selecione a duração",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Presets de Foco
        if (!focusSession.isActive) {
            Text(
                text = "Escolha um modo de foco:",
                color = TextGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FocusPresetRow(
                    title = "🍅 Pomodoro Rápido",
                    subtitle = "25 minutos de atenção focada",
                    minutes = 25,
                    onSelect = {
                        val now = System.currentTimeMillis()
                        val session = FocusSession(
                            isActive = true,
                            durationMinutes = 25,
                            startTimeMillis = now,
                            endTimeMillis = now + (25 * 60 * 1000L),
                            sessionName = "Pomodoro"
                        )
                        prefs.saveFocusSession(session)
                        focusSession = session
                    }
                )

                FocusPresetRow(
                    title = "💻 Trabalho Profundo",
                    subtitle = "50 minutos de alta produtividade",
                    minutes = 50,
                    onSelect = {
                        val now = System.currentTimeMillis()
                        val session = FocusSession(
                            isActive = true,
                            durationMinutes = 50,
                            startTimeMillis = now,
                            endTimeMillis = now + (50 * 60 * 1000L),
                            sessionName = "Trabalho Profundo"
                        )
                        prefs.saveFocusSession(session)
                        focusSession = session
                    }
                )

                FocusPresetRow(
                    title = "🌿 Detox Digital (Offtime)",
                    subtitle = "2 horas sem redes sociais",
                    minutes = 120,
                    onSelect = {
                        val now = System.currentTimeMillis()
                        val session = FocusSession(
                            isActive = true,
                            durationMinutes = 120,
                            startTimeMillis = now,
                            endTimeMillis = now + (120 * 60 * 1000L),
                            sessionName = "Detox Digital"
                        )
                        prefs.saveFocusSession(session)
                        focusSession = session
                    }
                )
            }
        } else {
            Button(
                onClick = {
                    prefs.saveFocusSession(FocusSession(isActive = false))
                    focusSession = prefs.getFocusSession()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralWarning, contentColor = TextWhite),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Interromper Sessão de Foco", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FocusPresetRow(
    title: String,
    subtitle: String,
    minutes: Int,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "${minutes} min", color = MintNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = TextGray, fontSize = 12.sp)
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Iniciar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
