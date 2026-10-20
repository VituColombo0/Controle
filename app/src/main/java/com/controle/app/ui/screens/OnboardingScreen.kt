package com.controle.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.service.UsageTracker
import com.controle.app.ui.theme.MintGlow
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SkyBlue
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.SurfaceElevated
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextMuted
import com.controle.app.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Halo de fundo decorativo
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MintGlow.copy(alpha = 0.4f),
                            SkyBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        when (currentStep) {
            0 -> WelcomeStep(onNext = { currentStep = 1 })
            1 -> PermissionsStep(onNext = { currentStep = 2 })
            2 -> FinalStep(onDone = {
                ControleApp.instance.preferences.setOnboardingDone()
                onFinished()
            })
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = MintNeon,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bem-vindo ao Controle",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tome o controle do seu tempo de tela. " +
                            "O Controle ajuda você a parar de usar o celular automaticamente, " +
                            "pausar antes de abrir apps distrativos e criar hábitos digitais saudáveis.",
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Features list
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    FeatureItem("🧘", "Pausa Consciente com exercício de respiração")
                    FeatureItem("🎯", "Intenções — reflita antes de abrir um app")
                    FeatureItem("🚫", "Bloqueio de Reels, Shorts e feeds infinitos")
                    FeatureItem("⏱", "Limites diários e sessões de foco")
                    FeatureItem("📅", "Agendamentos e lembretes inteligentes")
                    FeatureItem("🌐", "Bloqueio de websites distrativos")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Vamos Começar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = TextGray, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun PermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current

    var hasAccessibility by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasUsageStats by remember { mutableStateOf(false) }
    var isBatteryIgnoring by remember { mutableStateOf(false) }

    fun checkPerms() {
        hasAccessibility = isAccessibilityServiceEnabled(context)
        hasOverlay = Settings.canDrawOverlays(context)
        hasUsageStats = UsageTracker.hasUsagePermission(context)
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isBatteryIgnoring = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    LaunchedEffect(Unit) {
        while (true) {
            checkPerms()
            delay(1500)
        }
    }

    val allCritical = hasAccessibility && hasOverlay && hasUsageStats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Configurar Permissões",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "O Controle precisa dessas permissões para funcionar corretamente",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Permission cards
        OnboardingPermCard(
            title = "Acessibilidade",
            desc = "Detectar quando apps são abertos",
            isGranted = hasAccessibility,
            icon = Icons.Default.AccessibilityNew,
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OnboardingPermCard(
            title = "Sobrepor apps",
            desc = "Exibir tela de pausa por cima",
            isGranted = hasOverlay,
            icon = Icons.Default.Layers,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OnboardingPermCard(
            title = "Dados de Uso",
            desc = "Calcular tempo de tela dos apps",
            isGranted = hasUsageStats,
            icon = Icons.Default.QueryStats,
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OnboardingPermCard(
            title = "Economia de Bateria",
            desc = "Evitar que o sistema encerre o serviço",
            isGranted = isBatteryIgnoring,
            icon = Icons.Default.BatteryAlert,
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = allCritical,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allCritical) MintNeon else SurfaceElevated,
                contentColor = if (allCritical) PureBlack else TextMuted
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = if (allCritical) "Continuar" else "Conceda as permissões essenciais",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingPermCard(
    title: String,
    desc: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                if (isGranted) MintNeon.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isGranted) MintNeon.copy(alpha = 0.15f) else SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) MintNeon else TextGray,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = if (isGranted) "✓ Concedida" else desc,
                color = if (isGranted) MintNeon else TextGray,
                fontSize = 12.sp
            )
        }

        if (!isGranted) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ativar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FinalStep(onDone: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { -it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 64.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tudo Pronto!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "O Controle está configurado e protegendo seu tempo. " +
                            "Agora, cada vez que você abrir um app monitorado, " +
                            "uma pausa consciente aparecerá para ajudar você a refletir.",
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dicas:",
                    fontSize = 14.sp,
                    color = MintNeon,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Ative o Modo Estrito para apps mais viciantes", color = TextGray, fontSize = 13.sp)
                    Text("• Configure limites diários para cada app", color = TextGray, fontSize = 13.sp)
                    Text("• Use as Sessões de Foco para produtividade", color = TextGray, fontSize = 13.sp)
                    Text("• Bloqueie Reels/Shorts para evitar doomscrolling", color = TextGray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Começar a Usar o Controle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/com.controle.app.service.ControleAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(service)
}
