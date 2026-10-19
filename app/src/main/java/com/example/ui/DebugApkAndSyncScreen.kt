package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AiProviderType
import com.example.data.DebugApkSyncManager
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ViralAmber
import com.example.ui.theme.ViralEmerald
import java.io.File
import java.util.Locale

@Composable
fun DebugApkAndSyncScreen(
    uiState: AutoClipUiState,
    onSyncNow: () -> Unit
) {
    val context = LocalContext.current
    val status = uiState.debugBuildStatus

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("debug_apk_sync_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, ViralEmerald.copy(alpha = 0.65f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Debug APK Bundle",
                            tint = ViralEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "/debug/debug.apk • Continuous Release Hub",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Always synced with latest updates via Gradle finalizedBy hook",
                                style = MaterialTheme.typography.bodySmall,
                                color = ViralEmerald
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            InfoLine("Workspace Artifact", status?.debugFolderPath ?: "/debug/debug.apk")
                            InfoLine("Device Mirror Path", status?.apkFilePath ?: "files/debug/debug.apk")
                            InfoLine("APK Package Size", status?.apkSizeFormatted ?: "Calculating...")
                            InfoLine("Build Version", status?.buildVersion ?: "v1.4.0-autoclip-debug")
                            InfoLine("Upstream Source", status?.upstreamRepo ?: "https://github.com/zhouxiaoka/AutoClip")
                            InfoLine("Auto-Update Hook", "Gradle assembleDebug -> syncDebugApkToRoot (ACTIVE)")
                            InfoLine("Last Synced", status?.lastSyncedFormatted ?: "Just now")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSyncNow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_debug_apk_now_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ViralEmerald,
                                contentColor = Color(0xFF022C22)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync APK Now", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync debug.apk Now", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val path = status?.apkFilePath
                                if (!path.isNullOrBlank()) {
                                    val file = File(path)
                                    if (file.exists()) {
                                        DebugApkSyncManager.shareFileIntent(
                                            context = context,
                                            file = file,
                                            mimeType = "application/vnd.android.package-archive"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_debug_apk_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberCyanBright)
                        ) {
                            Icon(
                                Icons.Default.IosShare,
                                contentDescription = "Share debug.apk",
                                tint = CyberCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share debug.apk", color = CyberCyanBright)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Included Updates",
                            tint = CyberCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cumulative Updates Included in /debug/debug.apk",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    (status?.changelog ?: emptyList()).forEach { entry ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Included",
                                tint = ViralEmerald,
                                modifier = Modifier.size(15.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = CyberCyanBright
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiEngineSettingsScreen(
    uiState: AutoClipUiState,
    onSaveSettings: (AiProviderType, String, Int, Float, Boolean, Boolean) -> Unit,
    onTestConnection: (AiProviderType, String) -> Unit
) {
    val currentSettings = uiState.settings
    var selectedProvider by remember(currentSettings.activeProviderId) {
        mutableStateOf(
            AiProviderType.entries.firstOrNull { it.id == currentSettings.activeProviderId }
                ?: AiProviderType.GEMINI_FLASH
        )
    }
    var modelName by remember(currentSettings.activeModelName) {
        mutableStateOf(currentSettings.activeModelName)
    }
    var chunkSize by remember(currentSettings.chunkSizeChars) {
        mutableIntStateOf(currentSettings.chunkSizeChars)
    }
    var minScore by remember(currentSettings.defaultMinScore) {
        mutableFloatStateOf(currentSettings.defaultMinScore)
    }
    var autoBurnSubs by remember(currentSettings.autoBurnSubtitles) {
        mutableStateOf(currentSettings.autoBurnSubtitles)
    }
    var speakerTracking by remember(currentSettings.activeSpeakerTracking) {
        mutableStateOf(currentSettings.activeSpeakerTracking)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CyberCyanBright.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "AI Engine Configuration",
                            tint = CyberCyanBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Google AI Studio (Gemini API) & Free Engine",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Powered by free Google AI Studio Gemini API (BuildConfig.GEMINI_API_KEY) + Zero-Key Local Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Select Free AI Studio Gemini Model or Local Engine:",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanBright
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiProviderType.entries.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = {
                                    selectedProvider = provider
                                    modelName = provider.defaultModel
                                },
                                label = { Text(provider.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyanBright.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyanBright
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Active Gemini / Free Model ID (e.g., gemini-3.5-flash, gemini-3.1-flash-lite-preview)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_ai_model_name")
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transcript Chunk Size (Step 1 Slicer)", style = MaterialTheme.typography.bodySmall)
                            Text("$chunkSize chars", style = MaterialTheme.typography.labelMedium, color = CyberCyanBright)
                        }
                        Slider(
                            value = chunkSize.toFloat(),
                            onValueChange = { chunkSize = it.toInt() },
                            valueRange = 2000f..8000f
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Default Viral Score Threshold (Step 3)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                String.format(Locale.US, "%.2f", minScore),
                                style = MaterialTheme.typography.labelMedium,
                                color = ViralAmber
                            )
                        }
                        Slider(
                            value = minScore,
                            onValueChange = { minScore = it },
                            valueRange = 0.60f..0.95f
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Burn Synchronized SRT Subtitles (Step 6)", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Burns styled dynamic captions onto 9:16 vertical exports",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = autoBurnSubs, onCheckedChange = { autoBurnSubs = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active Speaker Center Tracking", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Centers active speaker face bounding box when cropping 16:9 -> 9:16",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = speakerTracking, onCheckedChange = { speakerTracking = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveSettings(
                                    selectedProvider,
                                    modelName,
                                    chunkSize,
                                    minScore,
                                    autoBurnSubs,
                                    speakerTracking
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_ai_settings_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyanBright,
                                contentColor = Color(0xFF032329)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Pipeline Config", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onTestConnection(selectedProvider, modelName) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_ai_connection_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test AI Endpoint")
                        }
                    }

                    if (!uiState.aiConnectionStatus.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.aiConnectionStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyanBright,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Security Caution Notice for Prototype BuildConfig keys
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ViralAmber.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(
                    containerColor = ViralAmber.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "API Key Security Notice",
                        tint = ViralAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.security_warning_prototype),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
