package com.controle.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.service.UsageTracker
import com.controle.app.ui.theme.CoralWarning
import com.controle.app.ui.theme.MintDark
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.SurfaceElevated
import com.controle.app.ui.theme.TextGray
import com.controle.app.ui.theme.TextMuted
import com.controle.app.ui.theme.TextWhite

@Composable
fun PermissionsScreen() {
    val context = LocalContext.current

    var hasAccessibility by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasUsageStats by remember { mutableStateOf(false) }
    var isBatteryIgnoring by remember { mutableStateOf(false) }

    fun checkPermissions() {
        hasAccessibility = isAccessibilityServiceEnabled(context)
        hasOverlay = Settings.canDrawOverlays(context)
        hasUsageStats = UsageTracker.hasUsagePermission(context)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isBatteryIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Permissões do Sistema",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "O Controle precisa dessas permissões do Android para monitorar e pausar os apps.",
            fontSize = 13.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Acessibilidade
        PermissionCheckCard(
            title = "Serviço de Acessibilidade",
            description = "Essencial para detectar em tempo real quando você abre um app monitorado e abrir a respiração consciente.",
            icon = Icons.Default.AccessibilityNew,
            isGranted = hasAccessibility,
            isRequired = true,
            onAction = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Sobrepor a outros apps
        PermissionCheckCard(
            title = "Sobrepor a outros apps",
            description = "Permite que a tela da respiração apareça suavemente em cima do aplicativo que você abriu.",
            icon = Icons.Default.Layers,
            isGranted = hasOverlay,
            isRequired = true,
            onAction = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Acesso aos dados de uso
        PermissionCheckCard(
            title = "Acesso aos Dados de Uso",
            description = "Calcula com precisão o tempo gasto em cada app e avisa quando atingir os limites diários.",
            icon = Icons.Default.QueryStats,
            isGranted = hasUsageStats,
            isRequired = true,
            onAction = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Otimização de bateria
        PermissionCheckCard(
            title = "Economia de Bateria",
            description = "Impede que o Android encerre a proteção do Controle silenciosamente em segundo plano.",
            icon = Icons.Default.BatteryAlert,
            isGranted = isBatteryIgnoring,
            isRequired = false,
            onAction = {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PermissionCheckCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isGranted) MintNeon.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(if (isGranted) MintDark.copy(alpha = 0.5f) else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) MintNeon else TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isGranted) "Concedida" else if (isRequired) "Necessária" else "Opcional",
                            color = if (isGranted) MintNeon else if (isRequired) CoralWarning else TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) MintNeon else CoralWarning,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = TextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar no Android", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
