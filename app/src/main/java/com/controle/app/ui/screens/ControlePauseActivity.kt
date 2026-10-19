package com.controle.app.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.controle.app.service.ControleAccessibilityService
import com.controle.app.ui.components.BreathingCircle
import com.controle.app.ui.theme.ControleTheme
import com.controle.app.ui.theme.CoralWarning
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

class ControlePauseActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PAUSE_SECONDS = "extra_pause_seconds"
        const val EXTRA_IS_LIMIT_REACHED = "extra_is_limit_reached"
        const val EXTRA_IS_GLOBAL_FOCUS = "extra_is_global_focus"
        const val EXTRA_IS_SHORTS_BLOCKED = "extra_is_shorts_blocked"
        const val EXTRA_IS_STRICT = "extra_is_strict"
        const val EXTRA_STRICT_PHRASE = "extra_strict_phrase"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Aplicativo"
        val pauseSeconds = intent.getIntExtra(EXTRA_PAUSE_SECONDS, 8)
        val isLimitReached = intent.getBooleanExtra(EXTRA_IS_LIMIT_REACHED, false)
        val isGlobalFocus = intent.getBooleanExtra(EXTRA_IS_GLOBAL_FOCUS, false)
        val isShortsBlocked = intent.getBooleanExtra(EXTRA_IS_SHORTS_BLOCKED, false)
        val isStrict = intent.getBooleanExtra(EXTRA_IS_STRICT, false)
        val strictPhrase = intent.getStringExtra(EXTRA_STRICT_PHRASE) ?: "Eu escolho conscientemente abrir este app"

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })

        setContent {
            ControleTheme {
                ControlePauseScreen(
                    appName = appName,
                    pauseSeconds = pauseSeconds,
                    isLimitReached = isLimitReached,
                    isGlobalFocus = isGlobalFocus,
                    isShortsBlocked = isShortsBlocked,
                    isStrict = isStrict,
                    strictPhrase = strictPhrase,
                    onGoHome = {
                        ControleApp.instance.preferences.recordPreventedOpen()
                        goHome()
                    },
                    onContinue = { minutes ->
                        ControleApp.instance.preferences.recordCompletedBreath()
                        ControleApp.instance.sessionManager.grantSession(targetPackage, minutes)
                        finish()
                    }
                )
            }
        }
    }

    private fun goHome() {
        val handled = ControleAccessibilityService.goHomeViaAccessibility()
        if (!handled) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(homeIntent)
        }
        finish()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlePauseScreen(
    appName: String,
    pauseSeconds: Int,
    isLimitReached: Boolean,
    isGlobalFocus: Boolean,
    isShortsBlocked: Boolean,
    isStrict: Boolean,
    strictPhrase: String,
    onGoHome: () -> Unit,
    onContinue: (Int) -> Unit
) {
    var isBreathDone by remember { mutableStateOf(false) }
    var selectedIntention by remember { mutableStateOf<String?>(null) }
    var showStrictDialog by remember { mutableStateOf(false) }
    var typedPhrase by remember { mutableStateOf("") }
    var phraseError by remember { mutableStateOf(false) }
    var pendingMinutes by remember { mutableStateOf(5) }

    val intentions = listOf(
        "💼 Trabalho / Estudo",
        "💬 Responder urgente",
        "🥱 Tédio momentâneo",
        "📱 Hábito automático",
        "🔍 Buscar algo específico"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PureBlack
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Badge de Estado Superior
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Icon(
                    imageVector = when {
                        isLimitReached -> Icons.Default.Lock
                        isGlobalFocus -> Icons.Default.HourglassTop
                        isShortsBlocked -> Icons.Default.VideocamOff
                        else -> Icons.Default.SelfImprovement
                    },
                    contentDescription = null,
                    tint = if (isLimitReached || isShortsBlocked) CoralWarning else MintNeon,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isLimitReached -> "Limite Diário Esgotado"
                        isGlobalFocus -> "Sessão de Foco Ativa"
                        isShortsBlocked -> "Reels & Shorts Bloqueado"
                        else -> "Pausa Consciente"
                    },
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Você está abrindo o",
                color = TextGray,
                fontSize = 14.sp
            )
            Text(
                text = appName,
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Círculo de Respiração Guiada
            BreathingCircle(
                durationSeconds = pauseSeconds,
                onFinished = { isBreathDone = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Intenções reflexivas (Marca registrada do Ascent)
            if (!isLimitReached && !isGlobalFocus && !isShortsBlocked) {
                Text(
                    text = "Qual é o seu propósito agora?",
                    color = TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    intentions.forEach { tag ->
                        val isSelected = selectedIntention == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) MintDark.copy(alpha = 0.5f) else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MintNeon else SurfaceBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedIntention = tag }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) MintNeon else TextGray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = when {
                        isLimitReached -> "Você atingiu o limite de tempo estipulado para hoje. Desconecte-se."
                        isGlobalFocus -> "Você está no meio de uma sessão de foco. Proteja sua atenção."
                        else -> "Feed infinito desabilitado para evitar perda de tempo. Use apenas chats e mensagens."
                    },
                    color = CoralWarning,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão Principal Heroico: "Mudei de ideia"
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintNeon,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mudei de ideia (Fechar app)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão Secundário: Liberar temporariamente por 5m ou 15m
            if (!isLimitReached && !isGlobalFocus && !isShortsBlocked) {
                AnimatedVisibility(
                    visible = isBreathDone,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isStrict) {
                                    pendingMinutes = 5
                                    showStrictDialog = true
                                } else {
                                    onContinue(5)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usar por 5 min", color = TextGray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = {
                                if (isStrict) {
                                    pendingMinutes = 15
                                    showStrictDialog = true
                                } else {
                                    onContinue(15)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usar por 15 min", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }

                if (!isBreathDone) {
                    Text(
                        text = "Conclua a respiração para liberar a opção de prosseguir",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Diálogo de Modo Estrito: Digitar Frase Consciente para desbloquear
        if (showStrictDialog) {
            AlertDialog(
                onDismissRequest = { showStrictDialog = false },
                containerColor = SurfaceCard,
                title = {
                    Text("Modo Estrito Ativo", color = TextWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Para quebrar o bloqueio, digite exatamente a frase abaixo:",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"$strictPhrase\"",
                            color = MintNeon,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = typedPhrase,
                            onValueChange = {
                                typedPhrase = it
                                phraseError = false
                            },
                            isError = phraseError,
                            placeholder = { Text("Digite aqui...", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = MintNeon,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (phraseError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("A frase não coincide exatamente.", color = CoralWarning, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (typedPhrase.trim() == strictPhrase.trim()) {
                                showStrictDialog = false
                                onContinue(pendingMinutes)
                            } else {
                                phraseError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showStrictDialog = false }) {
                        Text("Cancelar", color = TextGray)
                    }
                }
            )
        }
    }
}
