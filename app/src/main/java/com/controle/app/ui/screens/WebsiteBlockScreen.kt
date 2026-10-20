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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.controle.app.ControleApp
import com.controle.app.data.WebsiteBlock
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

@Composable
fun WebsiteBlockScreen() {
    val prefs = ControleApp.instance.preferences
    var blocks by remember { mutableStateOf(prefs.getWebsiteBlocks()) }
    var newUrl by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bloqueio de Websites",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Bloqueie URLs distrativas no navegador do celular",
            fontSize = 13.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Informação
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SkyBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = SkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quando você visitar um site bloqueado no Chrome ou outro navegador, " +
                            "o Controle interceptará e exibirá a tela de pausa.",
                    color = TextGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de adicionar URL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newUrl,
                onValueChange = {
                    newUrl = it
                    showError = false
                },
                placeholder = { Text("Ex: reddit.com, twitter.com", color = TextMuted) },
                singleLine = true,
                isError = showError,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = MintNeon,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val url = newUrl.trim()
                        .removePrefix("https://")
                        .removePrefix("http://")
                        .removePrefix("www.")
                        .trimEnd('/')
                    if (url.isNotEmpty() && url.contains(".")) {
                        prefs.addWebsiteBlock(url)
                        blocks = prefs.getWebsiteBlocks()
                        newUrl = ""
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MintNeon, contentColor = PureBlack),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        if (showError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Digite um domínio válido (ex: reddit.com)",
                color = CoralWarning,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (blocks.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nenhum website bloqueado",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adicione URLs de sites distrativos acima",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(blocks, key = { it.url }) { block ->
                    WebsiteBlockCard(
                        block = block,
                        onToggle = { enabled ->
                            val updated = blocks.map {
                                if (it.url == block.url) it.copy(isEnabled = enabled) else it
                            }
                            prefs.saveWebsiteBlocks(updated)
                            blocks = updated
                        },
                        onRemove = {
                            prefs.removeWebsiteBlock(block.url)
                            blocks = prefs.getWebsiteBlocks()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun WebsiteBlockCard(
    block: WebsiteBlock,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (block.isEnabled) CoralWarning.copy(alpha = 0.4f) else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (block.isEnabled) CoralWarning.copy(alpha = 0.15f) else SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = if (block.isEnabled) CoralWarning else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.url,
                    color = TextWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = if (block.isEnabled) "Bloqueado" else "Desativado",
                    color = if (block.isEnabled) CoralWarning else TextMuted,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = block.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureBlack,
                    checkedTrackColor = CoralWarning,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceElevated
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
