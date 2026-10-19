package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClipSegmentEntity
import com.example.data.ExportPlatformPreset
import com.example.data.PipelineStage
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ViralAmber
import com.example.ui.theme.ViralEmerald
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PipelineWorkbenchScreen(
    uiState: AutoClipUiState,
    onRunPipeline: () -> Unit,
    onUpdateMinScore: (Float) -> Unit,
    onSelectSegmentForPreview: (ClipSegmentEntity) -> Unit,
    onExportAndShareSegment: (ClipSegmentEntity, ExportPlatformPreset, Boolean) -> Unit,
    onUpdateSegmentMetadata: (ClipSegmentEntity, String, String, ExportPlatformPreset) -> Unit
) {
    val activeProject = uiState.activeProject
    if (activeProject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active project selected. Create or select a project from the Dashboard.")
        }
        return
    }

    val segments = uiState.activeProjectSegments
    val previewSegment = uiState.selectedSegmentForPreview?.takeIf { it.projectId == activeProject.id }
        ?: segments.firstOrNull()
        ?: uiState.allActiveProjectSegmentsUnfiltered.firstOrNull()

    var editingSegment by remember { mutableStateOf<ClipSegmentEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pipeline_workbench_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Project Header + 6-Stage Stepper Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE TASK • ${activeProject.sourceType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyanBright
                            )
                            Text(
                                text = activeProject.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onRunPipeline,
                            enabled = !uiState.isPipelineRunning,
                            modifier = Modifier.testTag("run_six_stage_pipeline_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyanBright,
                                contentColor = Color(0xFF032329)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Run 6-Stage AI Pipeline",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isPipelineRunning) "Processing..." else "Re-Run 6 Stages",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 6-Stage Pipeline Visual Stepper
                    SixStageStepperGrid(
                        currentProjectStageName = activeProject.currentStage,
                        isRunning = uiState.isPipelineRunning,
                        activeStage = uiState.activePipelineStage
                    )

                    if (activeProject.outlineJson.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Step 1 Macro Outline Summary:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyberCyanBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeProject.outlineJson,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9:16 Vertical Reframe & Burned-In SRT Preview Studio
        if (previewSegment != null) {
            item {
                Vertical916PreviewStudioCard(
                    segment = previewSegment,
                    speakerTrackingEnabled = uiState.settings.activeSpeakerTracking,
                    autoBurnSubtitles = uiState.settings.autoBurnSubtitles,
                    onEditMetadata = { editingSegment = previewSegment },
                    onExportAndShare = { preset, share ->
                        onExportAndShareSegment(previewSegment, preset, share)
                    }
                )
            }
        }

        // Heuristic Minimum Viral Score Threshold Slider (Step 3 Filter)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 3 Viral Score Sensitivity Threshold",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberCyanBright.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Min Score >= %.2f (%d/%d clips)",
                                    activeProject.minScoreThreshold,
                                    segments.size,
                                    uiState.allActiveProjectSegmentsUnfiltered.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyanBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = activeProject.minScoreThreshold,
                        onValueChange = onUpdateMinScore,
                        valueRange = 0.60f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyanBright,
                            activeTrackColor = CyberCyanBright
                        ),
                        modifier = Modifier.testTag("score_threshold_slider")
                    )
                }
            }
        }

        // Detected Highlight Segments List
        item {
            Text(
                text = "Detected Viral Segments (${segments.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(segments, key = { it.id }) { segment ->
            val isSelected = previewSegment?.id == segment.id
            DetectedSegmentCard(
                segment = segment,
                isSelected = isSelected,
                onSelectForPreview = { onSelectSegmentForPreview(segment) },
                onEditClip = { editingSegment = segment },
                onQuickExport = {
                    val preset = ExportPlatformPreset.entries.firstOrNull { it.id == segment.exportPresetId }
                        ?: ExportPlatformPreset.TIKTOK
                    onExportAndShareSegment(segment, preset, false)
                }
            )
        }

        // Live Pipeline Telemetry Console
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF070A12)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Pipeline Telemetry Console",
                            tint = CyberCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AutoClip Worker Log (step1_outline.py .. step6_video.py)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyanBright
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    uiState.pipelineLogLines.takeLast(8).forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA5F3FC),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }

    editingSegment?.let { seg ->
        EditClipMetadataDialog(
            segment = seg,
            onDismiss = { editingSegment = null },
            onSave = { newTitle, newHook, newPreset ->
                editingSegment = null
                onUpdateSegmentMetadata(seg, newTitle, newHook, newPreset)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SixStageStepperGrid(
    currentProjectStageName: String,
    isRunning: Boolean,
    activeStage: PipelineStage
) {
    val stages = listOf(
        PipelineStage.STEP1_OUTLINE,
        PipelineStage.STEP2_TIMELINE,
        PipelineStage.STEP3_SCORING,
        PipelineStage.STEP4_TITLE,
        PipelineStage.STEP5_CLUSTERING,
        PipelineStage.STEP6_VIDEO
    )
    val isCompleted = !isRunning && currentProjectStageName == PipelineStage.COMPLETED.name

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        stages.forEach { stage ->
            val isCurrent = isRunning && activeStage == stage
            val isDone = isCompleted || (isRunning && activeStage.stepNumber > stage.stepNumber)

            val borderColor = when {
                isCurrent -> CyberCyanBright
                isDone -> ViralEmerald.copy(alpha = 0.65f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrent) CyberCyanBright.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "STEP ${stage.stepNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) ViralEmerald else CyberCyanBright
                        )
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Stage Complete",
                                tint = ViralEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = stage.scriptName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Vertical916PreviewStudioCard(
    segment: ClipSegmentEntity,
    speakerTrackingEnabled: Boolean,
    autoBurnSubtitles: Boolean,
    onEditMetadata: () -> Unit,
    onExportAndShare: (ExportPlatformPreset, Boolean) -> Unit
) {
    var selectedPreset by remember(segment.id, segment.exportPresetId) {
        mutableStateOf(
            ExportPlatformPreset.entries.firstOrNull { it.id == segment.exportPresetId }
                ?: ExportPlatformPreset.TIKTOK
        )
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "9:16 Vertical Reframe Monitor",
                        tint = CyberCyanBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Step 6 • 9:16 Speaker Crop & Burned Subtitle Monitor",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Clip #${segment.segmentIndex}: ${segment.startTimeStr} → ${segment.endTimeStr} (${segment.durationSec}s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanBright
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditMetadata,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_segment_metadata_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Hook", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Copy", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Simulated 9:16 Frame Monitor + Live Audio Waveform + Burned SRT Overlay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 9:16 Phone Canvas Preview
                Box(
                    modifier = Modifier
                        .width(138.dp)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF090D16),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(10.dp)
                ) {
                    // Speaker Tracking Reticle Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (speakerTrackingEnabled) {
                            val boxW = size.width * 0.72f
                            val boxH = size.height * 0.42f
                            drawRoundRect(
                                color = Color(0xFF22D3EE).copy(alpha = 0.55f),
                                topLeft = Offset((size.width - boxW) / 2f, size.height * 0.22f),
                                size = Size(boxW, boxH),
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 2.5f)
                            )
                        }
                        // Audio waveform bars near bottom
                        val barCount = 16
                        val barWidth = size.width / (barCount * 1.6f)
                        for (i in 0 until barCount) {
                            val ratio = ((i * 37 + segment.segmentIndex * 19) % 100) / 100f
                            val barH = 8f + ratio * 24f
                            drawRoundRect(
                                color = Color(0xFF6366F1),
                                topLeft = Offset(
                                    x = i * (size.width / barCount) + 2f,
                                    y = size.height * 0.68f - barH / 2f
                                ),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }

                    // Top Cover Hook Banner (Step 4 coverHookText)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ViralAmber,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = segment.coverHookText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Center Play Indicator
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Preview 9:16 Segment",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.Center)
                    )

                    // Bottom Burned-In Subtitle Box (Step 6 Subtitle Burn)
                    if (autoBurnSubtitles) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.82f),
                            border = BorderStroke(1.dp, CyberCyanBright.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = segment.transcriptExcerpt.take(68),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFEF08A),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }

                // Right Column: Multi-Axis Scores + SRT Preview + Platform Preset Picker
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = segment.generatedViralTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ScoreBarRow("Final Viral Score", segment.finalScore, ViralEmerald)
                    ScoreBarRow("3s Hook Tension", segment.hookScore, CyberCyanBright)
                    ScoreBarRow("Emotional Resonance", segment.emotionScore, ViralAmber)
                    ScoreBarRow("Information Density", segment.infoDensityScore, ElectricIndigo)

                    Text(
                        text = "Platform Export Preset:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExportPlatformPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = {
                                    Text(
                                        text = "${preset.id.uppercase()} (${preset.aspectRatio})",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyanBright.copy(alpha = 0.2f),
                                    selectedLabelColor = CyberCyanBright
                                )
                            )
                        }
                    }
                }
            }

            // Export & Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onExportAndShare(selectedPreset, false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("render_clip_package_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyanBright,
                        contentColor = Color(0xFF032329)
                    )
                ) {
                    Icon(Icons.Default.DownloadDone, contentDescription = "Render Clip", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Render 9:16 + SRT", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onExportAndShare(selectedPreset, true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_clip_package_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberCyanBright)
                ) {
                    Icon(
                        Icons.Default.IosShare,
                        contentDescription = "Share Exported Package",
                        tint = CyberCyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Package", color = CyberCyanBright)
                }
            }
        }
    }
}

@Composable
private fun ScoreBarRow(label: String, score: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                text = String.format(Locale.US, "%.2f", score),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { score.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun DetectedSegmentCard(
    segment: ClipSegmentEntity,
    isSelected: Boolean,
    onSelectForPreview: () -> Unit,
    onEditClip: () -> Unit,
    onQuickExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectForPreview() }
            .testTag("segment_card_${segment.segmentIndex}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) CyberCyanBright else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberCyanBright.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "#${segment.segmentIndex} • ${segment.startTimeStr} - ${segment.endTimeStr}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyanBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (segment.isExported) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ViralEmerald.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "EXPORTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = ViralEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ViralAmber.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = String.format(Locale.US, "Score %.2f", segment.finalScore),
                        style = MaterialTheme.typography.labelMedium,
                        color = ViralAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = segment.generatedViralTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "AI Reason: ${segment.recommendReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = segment.hashtags.split(",").joinToString(" "),
                style = MaterialTheme.typography.labelSmall,
                color = ElectricIndigo
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSelectForPreview) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Load in 9:16 Monitor", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview 9:16 & SRT")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEditClip) {
                        Text("Edit Title")
                    }
                    OutlinedButton(
                        onClick = onQuickExport,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Quick Export", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditClipMetadataDialog(
    segment: ClipSegmentEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, ExportPlatformPreset) -> Unit
) {
    var viralTitle by remember(segment.id) { mutableStateOf(segment.generatedViralTitle) }
    var coverHook by remember(segment.id) { mutableStateOf(segment.coverHookText) }
    var selectedPreset by remember(segment.id) {
        mutableStateOf(
            ExportPlatformPreset.entries.firstOrNull { it.id == segment.exportPresetId }
                ?: ExportPlatformPreset.TIKTOK
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Step 4 Viral Title & Cover Hook") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = viralTitle,
                    onValueChange = { viralTitle = it },
                    label = { Text("Viral Short Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = coverHook,
                    onValueChange = { coverHook = it },
                    label = { Text("9:16 Thumbnail Cover Hook (4-6 Words)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Burned-In SRT Preview:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyanBright
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = segment.srtContent,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(viralTitle, coverHook, selectedPreset) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
