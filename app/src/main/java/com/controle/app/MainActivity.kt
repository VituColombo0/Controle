package com.controle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ui.screens.AppsSelectionScreen
import com.controle.app.ui.screens.DashboardScreen
import com.controle.app.ui.screens.FocusScreen
import com.controle.app.ui.screens.OnboardingScreen
import com.controle.app.ui.screens.PermissionsScreen
import com.controle.app.ui.screens.WebsiteBlockScreen
import com.controle.app.ui.theme.ControleTheme
import com.controle.app.ui.theme.MintNeon
import com.controle.app.ui.theme.PureBlack
import com.controle.app.ui.theme.SurfaceBorder
import com.controle.app.ui.theme.SurfaceCard
import com.controle.app.ui.theme.TextMuted
import com.controle.app.ui.theme.TextWhite

enum class ControleNavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Início", Icons.Default.Dashboard),
    APPS("Apps", Icons.Default.Apps),
    FOCUS("Foco", Icons.Default.HourglassTop),
    WEBSITES("Sites", Icons.Default.Language),
    PERMISSIONS("Config", Icons.Default.Shield)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ControleTheme {
                MainAppEntry()
            }
        }
    }
}

@Composable
fun MainAppEntry() {
    val prefs = ControleApp.instance.preferences
    var showOnboarding by remember { mutableStateOf(!prefs.isOnboardingDone()) }

    if (showOnboarding) {
        OnboardingScreen(onFinished = {
            showOnboarding = false
        })
    } else {
        MainAppNavigation()
    }
}

@Composable
fun MainAppNavigation() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabs = ControleNavTab.values()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                modifier = Modifier
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MintNeon else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) TextWhite else TextMuted,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MintNeon.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> DashboardScreen(
                    onNavigateToApps = { selectedIndex = 1 },
                    onNavigateToFocus = { selectedIndex = 2 },
                    onNavigateToPermissions = { selectedIndex = 4 }
                )
                1 -> AppsSelectionScreen()
                2 -> FocusScreen()
                3 -> WebsiteBlockScreen()
                4 -> PermissionsScreen()
            }
        }
    }
}
