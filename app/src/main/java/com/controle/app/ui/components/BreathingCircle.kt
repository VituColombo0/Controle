package com.controle.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.ui.theme.MintGlow
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.SkyBlue
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextWhite
import com.controle.app.util.HapticHelper
import kotlinx.coroutines.delay

enum class BreathPhase(val label: String) {
    INHALE("Inspire profundamente..."),
    HOLD("Segure a respiração..."),
    EXHALE("Expire devagar...")
}

@Composable
fun BreathingCircle(
    durationSeconds: Int = 8,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.72f) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsRemaining by remember { mutableIntStateOf(durationSeconds) }
    val isVibration = remember { ControleApp.instance.preferences.isVibrationEnabled() }

    // Ciclos sincronizados de respiração
    LaunchedEffect(Unit) {
        val half = (durationSeconds / 2).coerceAtLeast(3)

        while (secondsRemaining > 0) {
            // Fase 1: Inspire
            currentPhase = BreathPhase.INHALE
            if (isVibration) HapticHelper.gentleBreathIn(context)
            scale.animateTo(
                targetValue = 1.30f,
                animationSpec = tween(durationMillis = (half * 750), easing = FastOutSlowInEasing)
            )

            // Fase 2: Segure
            currentPhase = BreathPhase.HOLD
            if (isVibration) HapticHelper.gentleTick(context)
            delay(1200)

            // Fase 3: Expire
            currentPhase = BreathPhase.EXHALE
            if (isVibration) HapticHelper.gentleTick(context)
            scale.animateTo(
                targetValue = 0.80f,
                animationSpec = tween(durationMillis = (half * 750), easing = FastOutSlowInEasing)
            )
        }
    }

    // Temporizador da contagem regressiva
    LaunchedEffect(durationSeconds) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(290.dp)
    ) {
        // Halo de luz difusa e suave de fundo
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale.value * 1.15f)
                .blur(36.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MintGlow.copy(alpha = 0.5f),
                            SkyBlue.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Círculo central com animação de expansão e contração
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        ) {
            // Anel externo translúcido
            drawCircle(
                color = MintNeon.copy(alpha = 0.4f),
                radius = size.minDimension / 2,
                style = Stroke(width = 3.dp.toPx())
            )

            // Preenchimento gradiente suave
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MintNeon.copy(alpha = 0.45f),
                        SkyBlue.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                radius = (size.minDimension / 2) - 8f
            )
        }

        // Barra de progresso circular dos segundos totais
        Canvas(modifier = Modifier.size(224.dp)) {
            val progress = (durationSeconds - secondsRemaining).toFloat() / durationSeconds.toFloat()
            drawArc(
                color = MintNeon,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Textos da fase e contador de segundos
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentPhase.label,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${secondsRemaining}s",
                color = TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
