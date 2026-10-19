package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AutoClipRepository
import com.example.data.PipelineStage
import com.example.data.VideoCategory
import com.example.data.VideoProjectEntity
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ViralAmber
import com.example.ui.theme.ViralEmerald
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsDashboardScreen(
    uiState: AutoClipUiState,
    onSelectProject: (Long, Boolean) -> Unit,
    onCreateProject: (String, String, String, VideoCategory, String) -> Unit,
    onImportLocalUri: (android.net.Uri) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onNavigateToApkSync: () -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImportLocalUri(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("projects_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner: AutoClip Studio Identity + Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.28f),
                                    CyberCyanBright.copy(alpha = 0.14f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberCyanBright.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = "AutoClip Icon",
                                        tint = CyberCyanBright
                                    )
                                }
                                Column {
                                    Text(
                                        text = "zhouxiaoka / AutoClip",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyberCyanBright
                                    )
                                    Text(
                                        text = "AI Highlight Clipper & 9:16 Studio",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ViralEmerald.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, ViralEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { onNavigateToApkSync() }
                                    .testTag("badge_debug_apk_ready")
                            ) {
                                Text(
                                    text = "debug.apk SYNCED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ViralEmerald,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = "6-Stage AI video processing pipeline: Outline Extraction → Timeline Segmentation → Multi-Dimensional Viral Scoring → Title & Copywriting → Thematic Collections → 9:16 Subtitle Burn-In.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricPill(
                                label = "Projects",
                                value = uiState.projects.size.toString(),
                                accent = CyberCyanBright,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Viral Clips",
                                value = uiState.allSegments.size.toString(),
                                accent = ElectricIndigo,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Collections",
                                value = uiState.allCollections.size.toString(),
                                accent = ViralAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_video_task_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyanBright,
                                    contentColor = Color(0xFF032329)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Video Task",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("URL / SRT Task", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_local_video_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberCyanBright.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoFile,
                                    contentDescription = "Import Local Video",
                                    tint = CyberCyanBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Local MP4", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Processing Tasks (${uiState.projects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap project to inspect 6-stage pipeline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(uiState.projects, key = { it.id }) { project ->
            val projectClips = uiState.allSegments.filter { it.projectId == project.id }
            val projectCollections = uiState.allCollections.filter { it.projectId == project.id }
            val isSelected = uiState.activeProject?.id == project.id

            ProjectTaskCard(
                project = project,
                clipCount = projectClips.size,
                collectionCount = projectCollections.size,
                topScore = projectClips.maxOfOrNull { it.finalScore } ?: 0f,
                isSelected = isSelected,
                onOpenPipeline = { onSelectProject(project.id, true) },
                onDelete = { onDeleteProject(project.id) }
            )
        }
    }

    if (showCreateDialog) {
        NewAutoClipProjectDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, sourceType, url, category, srt ->
                showCreateDialog = false
                onCreateProject(title, sourceType, url, category, srt)
            }
        )
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProjectTaskCard(
    project: VideoProjectEntity,
    clipCount: Int,
    collectionCount: Int,
    topScore: Float,
    isSelected: Boolean,
    onOpenPipeline: () -> Unit,
    onDelete: () -> Unit
) {
    val category = VideoCategory.entries.firstOrNull { it.id == project.categoryId } ?: VideoCategory.KNOWLEDGE
    val stage = PipelineStage.entries.firstOrNull { it.name == project.currentStage } ?: PipelineStage.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPipeline() }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) CyberCyanBright else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricIndigo.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = project.sourceType,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Project",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = project.sourceUrlOrUri,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            LinearProgressIndicator(
                progress = { project.stageProgress.coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (stage == PipelineStage.COMPLETED) ViralEmerald else CyberCyanBright,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Stage Status",
                            tint = if (stage == PipelineStage.COMPLETED) ViralEmerald else CyberCyanBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stage.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$clipCount Clips • $collectionCount Collections",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanBright
                    )
                }

                if (topScore > 0f) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ViralAmber.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "Peak %.2f", topScore),
                            style = MaterialTheme.typography.labelSmall,
                            color = ViralAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onOpenPipeline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_pipeline_button_${project.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = CyberCyanBright
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Open 6-Stage Pipeline",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Inspect 6-Stage Clips & 9:16 Subtitles")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewAutoClipProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, VideoCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("DeepSeek & Qwen Agent Architecture Keynote") }
    var sourceType by remember { mutableStateOf("YOUTUBE") }
    var sourceUrl by remember { mutableStateOf("https://www.youtube.com/watch?v=autoclip_demo") }
    var selectedCategory by remember { mutableStateOf(VideoCategory.KNOWLEDGE) }
    var transcriptSrt by remember {
        mutableStateOf(
            """
            1
            00:00:12,000 --> 00:00:34,000
            Why do 90% of video editors burn out? Because scrubbing a 2-hour podcast for three 45-second TikTok hooks takes an entire afternoon.

            2
            00:01:05,000 --> 00:01:42,000
            In zhouxiaoka/AutoClip, Step 1 extracts the macro outline first, then Step 2 maps exact timestamp boundaries so you never cut a speaker mid-sentence.

            3
            00:02:15,000 --> 00:02:58,000
            Step 3 grades every candidate segment from 0.0 to 1.0 on Hook Tension, Emotional Resonance, and Standalone Completeness before Step 6 burns 9:16 subtitles.
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New AutoClip Video Task",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("YOUTUBE", "BILIBILI", "LOCAL_SRT").forEach { type ->
                        FilterChip(
                            selected = sourceType == type,
                            onClick = { sourceType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyanBright.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyanBright
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project / Video Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_title")
                )

                OutlinedTextField(
                    value = sourceUrl,
                    onValueChange = { sourceUrl = it },
                    label = { Text("YouTube / Bilibili URL or Source Path") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = "Source URL")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_url")
                )

                Text(
                    text = "Content Category (Tailors Step 1-4 AI Prompts):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VideoCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = transcriptSrt,
                    onValueChange = { transcriptSrt = it },
                    label = { Text("SRT Subtitles / Transcript (Auto-Extracted or Paste)") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_project_srt")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(title, sourceType, sourceUrl, selectedCategory, transcriptSrt)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyanBright,
                    contentColor = Color(0xFF032329)
                ),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Pipeline")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start 6-Stage AI Pipeline", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
