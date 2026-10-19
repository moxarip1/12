package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AiEngineSettingsScreen
import com.example.ui.AutoClipTab
import com.example.ui.AutoClipUiState
import com.example.ui.AutoClipViewModel
import com.example.ui.CollectionsAndSettingsScreens
import com.example.ui.DebugApkAndSyncScreen
import com.example.ui.PipelineWorkbenchScreen
import com.example.ui.ProjectsDashboardScreen
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ViralEmerald

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val viewModel: AutoClipViewModel = viewModel(
                    factory = AutoClipViewModel.provideFactory(applicationContext)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                AutoClipStudioApp(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoClipStudioApp(
    uiState: AutoClipUiState,
    viewModel: AutoClipViewModel
) {
    if (uiState.selectedTab != AutoClipTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(AutoClipTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "AutoClip Logo",
                            tint = CyberCyanBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.subtitle_tagline),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(AutoClipTab.DASHBOARD, Icons.Default.Dashboard, "Projects"),
                    Triple(AutoClipTab.PIPELINE, Icons.Default.AutoAwesome, "Pipeline"),
                    Triple(AutoClipTab.COLLECTIONS, Icons.Default.AutoAwesomeMotion, "Collections"),
                    Triple(AutoClipTab.BUILD_UPDATES, Icons.Default.Android, "debug.apk"),
                    Triple(AutoClipTab.SETTINGS, Icons.Default.Memory, "AI Engine")
                )

                navItems.forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF032329),
                            selectedTextColor = CyberCyanBright,
                            indicatorColor = CyberCyanBright,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = !uiState.statusBannerMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricIndigo.copy(alpha = 0.25f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.statusBannerMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ViralEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearStatusBanner() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Banner",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.selectedTab) {
                    AutoClipTab.DASHBOARD -> {
                        ProjectsDashboardScreen(
                            uiState = uiState,
                            onSelectProject = { id, openPipeline ->
                                viewModel.selectProject(id, openPipeline)
                            },
                            onCreateProject = { title, sourceType, url, category, srt ->
                                viewModel.createNewVideoProject(
                                    title = title,
                                    sourceType = sourceType,
                                    sourceUrlOrUri = url,
                                    category = category,
                                    transcriptSrt = srt,
                                    runImmediately = true
                                )
                            },
                            onImportLocalUri = { uri ->
                                viewModel.importLocalVideoUri(uri)
                            },
                            onDeleteProject = { id ->
                                viewModel.deleteProject(id)
                            },
                            onNavigateToApkSync = {
                                viewModel.selectTab(AutoClipTab.BUILD_UPDATES)
                            }
                        )
                    }

                    AutoClipTab.PIPELINE -> {
                        PipelineWorkbenchScreen(
                            uiState = uiState,
                            onRunPipeline = { viewModel.runSixStagePipeline() },
                            onUpdateMinScore = { threshold ->
                                viewModel.updateMinScoreThreshold(threshold)
                            },
                            onSelectSegmentForPreview = { seg ->
                                viewModel.selectSegmentForPreview(seg)
                            },
                            onExportAndShareSegment = { seg, preset, share ->
                                viewModel.exportAndShareClip(seg, preset, share)
                            },
                            onUpdateSegmentMetadata = { seg, newTitle, newHook, preset ->
                                viewModel.updateSegmentTitleAndPreset(seg, newTitle, newHook, preset)
                            }
                        )
                    }

                    AutoClipTab.COLLECTIONS -> {
                        CollectionsAndSettingsScreens(
                            uiState = uiState,
                            onCreateSmartCollection = { title, desc ->
                                viewModel.createSmartCollectionFromTopClips(title, desc)
                            },
                            onDeleteCollection = { id ->
                                viewModel.deleteCollection(id)
                            }
                        )
                    }

                    AutoClipTab.BUILD_UPDATES -> {
                        DebugApkAndSyncScreen(
                            uiState = uiState,
                            onSyncNow = { viewModel.syncDebugApkNow() }
                        )
                    }

                    AutoClipTab.SETTINGS -> {
                        AiEngineSettingsScreen(
                            uiState = uiState,
                            onSaveSettings = { provider, model, chunk, minScore, burnSubs, speakerTrack ->
                                viewModel.updateAiSettings(
                                    provider = provider,
                                    modelName = model,
                                    chunkSize = chunk,
                                    defaultMinScore = minScore,
                                    autoBurnSubs = burnSubs,
                                    speakerTracking = speakerTrack
                                )
                            },
                            onTestConnection = { provider, model ->
                                viewModel.testAiProviderConnection(provider, model)
                            }
                        )
                    }
                }
            }
        }
    }
}
