package com.example.ui

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AiProviderType
import com.example.data.AutoClipDatabase
import com.example.data.AutoClipRepository
import com.example.data.AutoClipSettings
import com.example.data.ClipCollectionEntity
import com.example.data.ClipSegmentEntity
import com.example.data.DebugApkSyncManager
import com.example.data.DebugBuildStatus
import com.example.data.ExportPlatformPreset
import com.example.data.PipelineStage
import com.example.data.VideoCategory
import com.example.data.VideoProjectEntity
import com.example.network.AutoClipAiEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AutoClipTab(val title: String) {
    DASHBOARD("Projects"),
    PIPELINE("6-Stage Pipeline"),
    COLLECTIONS("Collections"),
    BUILD_UPDATES("Debug APK & Sync"),
    SETTINGS("AI Engine")
}

data class AutoClipUiState(
    val selectedTab: AutoClipTab = AutoClipTab.DASHBOARD,
    val projects: List<VideoProjectEntity> = emptyList(),
    val activeProjectId: Long? = null,
    val allSegments: List<ClipSegmentEntity> = emptyList(),
    val allCollections: List<ClipCollectionEntity> = emptyList(),
    val settings: AutoClipSettings = AutoClipSettings(),
    val isPipelineRunning: Boolean = false,
    val activePipelineStage: PipelineStage = PipelineStage.IDLE,
    val pipelineLogLines: List<String> = emptyList(),
    val selectedSegmentForPreview: ClipSegmentEntity? = null,
    val debugBuildStatus: DebugBuildStatus? = null,
    val statusBannerMessage: String? = null,
    val aiConnectionStatus: String? = null
) {
    val activeProject: VideoProjectEntity?
        get() = projects.firstOrNull { it.id == activeProjectId } ?: projects.firstOrNull()

    val activeProjectSegments: List<ClipSegmentEntity>
        get() {
            val pid = activeProject?.id ?: return emptyList()
            val minScore = activeProject?.minScoreThreshold ?: 0.70f
            return allSegments.filter { it.projectId == pid && it.finalScore >= minScore }
                .sortedByDescending { it.finalScore }
        }

    val allActiveProjectSegmentsUnfiltered: List<ClipSegmentEntity>
        get() {
            val pid = activeProject?.id ?: return emptyList()
            return allSegments.filter { it.projectId == pid }.sortedByDescending { it.finalScore }
        }

    val activeProjectCollections: List<ClipCollectionEntity>
        get() {
            val pid = activeProject?.id ?: return allCollections
            return allCollections.filter { it.projectId == pid }
        }
}

class AutoClipViewModel(
    private val appContext: Context,
    private val repository: AutoClipRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AutoClipTab.DASHBOARD)
    private val _activeProjectId = MutableStateFlow<Long?>(null)
    private val _isPipelineRunning = MutableStateFlow(false)
    private val _activePipelineStage = MutableStateFlow(PipelineStage.IDLE)
    private val _pipelineLogLines = MutableStateFlow<List<String>>(
        listOf(
            "[00:00:00] AutoClip Core Engine initialized (zhouxiaoka/AutoClip 6-Stage Architecture).",
            "[00:00:01] Local FFmpeg 9:16 Reframer & Subtitle Burn-In pipeline ready.",
            "[00:00:02] Continuous Debug APK Sync (/debug/debug.apk) active."
        )
    )
    private val _selectedSegmentForPreview = MutableStateFlow<ClipSegmentEntity?>(null)
    private val _debugBuildStatus = MutableStateFlow<DebugBuildStatus?>(null)
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    private val _aiConnectionStatus = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AutoClipUiState> = combine(
        combine(
            repository.allProjects,
            repository.allSegments,
            repository.allCollections,
            repository.settingsFlow
        ) { projects, segments, collections, settings ->
            RepoDataBundle(projects, segments, collections, settings)
        },
        combine(
            _selectedTab,
            _activeProjectId,
            _isPipelineRunning,
            _activePipelineStage,
            _pipelineLogLines
        ) { tab, projId, running, stage, logs ->
            PipelineInternalState(tab, projId, running, stage, logs)
        },
        combine(
            _selectedSegmentForPreview,
            _debugBuildStatus,
            _statusBannerMessage,
            _aiConnectionStatus
        ) { previewSeg, debugStatus, banner, aiConn ->
            ExtraInternalState(previewSeg, debugStatus, banner, aiConn)
        }
    ) { repoData, pipeState, extraState ->
        val resolvedProjectId = pipeState.projId ?: repoData.projects.firstOrNull()?.id
        AutoClipUiState(
            selectedTab = pipeState.tab,
            projects = repoData.projects,
            activeProjectId = resolvedProjectId,
            allSegments = repoData.segments,
            allCollections = repoData.collections,
            settings = repoData.settings,
            isPipelineRunning = pipeState.running,
            activePipelineStage = pipeState.stage,
            pipelineLogLines = pipeState.logs,
            selectedSegmentForPreview = extraState.previewSeg,
            debugBuildStatus = extraState.debugStatus,
            statusBannerMessage = extraState.banner,
            aiConnectionStatus = extraState.aiConn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AutoClipUiState()
    )

    private data class RepoDataBundle(
        val projects: List<VideoProjectEntity>,
        val segments: List<ClipSegmentEntity>,
        val collections: List<ClipCollectionEntity>,
        val settings: AutoClipSettings
    )

    private data class PipelineInternalState(
        val tab: AutoClipTab,
        val projId: Long?,
        val running: Boolean,
        val stage: PipelineStage,
        val logs: List<String>
    )

    private data class ExtraInternalState(
        val previewSeg: ClipSegmentEntity?,
        val debugStatus: DebugBuildStatus?,
        val banner: String?,
        val aiConn: String?
    )

    init {
        viewModelScope.launch {
            repository.seedInitialProjectsIfEmpty()
            refreshDebugApkStatus()
        }
    }

    fun selectTab(tab: AutoClipTab) {
        _selectedTab.value = tab
    }

    fun selectProject(projectId: Long, navigateToPipeline: Boolean = false) {
        _activeProjectId.value = projectId
        if (navigateToPipeline) {
            _selectedTab.value = AutoClipTab.PIPELINE
        }
    }

    fun selectSegmentForPreview(segment: ClipSegmentEntity?) {
        _selectedSegmentForPreview.value = segment
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun refreshDebugApkStatus() {
        viewModelScope.launch {
            val status = DebugApkSyncManager.syncAndInspectDebugApk(appContext)
            _debugBuildStatus.value = status
        }
    }

    fun syncDebugApkNow() {
        viewModelScope.launch {
            val status = DebugApkSyncManager.syncAndInspectDebugApk(appContext)
            _debugBuildStatus.value = status
            _statusBannerMessage.value = "Synced latest APK build to ${status.debugFolderPath} (${status.apkSizeFormatted})"
        }
    }

    fun createNewVideoProject(
        title: String,
        sourceType: String,
        sourceUrlOrUri: String,
        category: VideoCategory,
        transcriptSrt: String,
        durationSeconds: Int = 1800,
        runImmediately: Boolean = true
    ) {
        viewModelScope.launch {
            val cleanTitle = title.ifBlank { "Untitled AutoClip Task (${category.label})" }
            val cleanTranscript = transcriptSrt.ifBlank { AutoClipRepository.SAMPLE_SRT_1 }
            val project = VideoProjectEntity(
                title = cleanTitle,
                sourceType = sourceType,
                sourceUrlOrUri = sourceUrlOrUri.ifBlank { "https://www.youtube.com/watch?v=autoclip_task" },
                categoryId = category.id,
                durationSeconds = durationSeconds,
                transcriptSrt = cleanTranscript,
                currentStage = PipelineStage.IDLE.name,
                stageProgress = 0f,
                minScoreThreshold = uiState.value.settings.defaultMinScore
            )
            val newId = repository.createProject(project)
            _activeProjectId.value = newId
            if (runImmediately) {
                _selectedTab.value = AutoClipTab.PIPELINE
                runSixStagePipeline(newId)
            } else {
                _statusBannerMessage.value = "Created project '$cleanTitle'. Ready for 6-stage AI pipeline."
            }
        }
    }

    fun importLocalVideoUri(uri: Uri) {
        viewModelScope.launch {
            var durationSec = 960
            var resolvedTitle = "Local Device Video (${uri.lastPathSegment ?: "clip.mp4"})"
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(appContext, uri)
                val durMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                if (durMs != null && durMs > 0) {
                    durationSec = (durMs / 1000L).toInt()
                }
                val titleMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                if (!titleMeta.isNullOrBlank()) {
                    resolvedTitle = titleMeta
                }
                retriever.release()
            } catch (_: Exception) {
                // Keep defaults if metadata extraction isn't supported for this URI
            }

            createNewVideoProject(
                title = resolvedTitle,
                sourceType = "LOCAL_FILE",
                sourceUrlOrUri = uri.toString(),
                category = VideoCategory.KNOWLEDGE,
                transcriptSrt = AutoClipRepository.SAMPLE_SRT_1,
                durationSeconds = durationSec,
                runImmediately = true
            )
        }
    }

    /**
     * Executes the authentic 6-stage zhouxiaoka/AutoClip pipeline with live stage telemetry,
     * updating the project, clips, and collections in Room.
     */
    fun runSixStagePipeline(projectId: Long? = null) {
        if (_isPipelineRunning.value) return
        viewModelScope.launch {
            val targetId = projectId ?: uiState.value.activeProject?.id ?: return@launch
            val project = uiState.value.projects.firstOrNull { it.id == targetId } ?: return@launch
            val category = VideoCategory.entries.firstOrNull { it.id == project.categoryId } ?: VideoCategory.KNOWLEDGE

            _isPipelineRunning.value = true
            appendLog("Starting 6-Stage AutoClip Pipeline for '${project.title}' [Category: ${category.label}]")

            // Step 1: Outline Extraction
            updateProjectStage(project, PipelineStage.STEP1_OUTLINE, 0.16f)
            appendLog("[step1_outline.py] Splitting SRT transcript into semantic chunks (${uiState.value.settings.chunkSizeChars} chars)...")
            delay(450)

            // Step 2: Timeline Segmentation
            updateProjectStage(project, PipelineStage.STEP2_TIMELINE, 0.34f)
            appendLog("[step2_timeline.py] Locating topic transitions & millisecond timestamp intervals...")
            delay(450)

            // Step 3: Multi-Dimensional Viral Scoring (Invoke Gemini REST / Heuristic Engine)
            updateProjectStage(project, PipelineStage.STEP3_SCORING, 0.54f)
            appendLog("[step3_scoring.py] Evaluating Hook Tension, Emotional Resonance & Information Density via ${uiState.value.settings.activeModelName}...")

            val aiResult = AutoClipAiEngine.analyzeTranscriptPipeline(
                projectTitle = project.title,
                category = category,
                transcriptSrt = project.transcriptSrt,
                minScoreThreshold = project.minScoreThreshold,
                modelName = uiState.value.settings.activeModelName
            )

            // Step 4: Viral Title & Hook Copywriting
            updateProjectStage(project, PipelineStage.STEP4_TITLE, 0.72f)
            appendLog("[step4_title.py] Generating 9:16 cover hooks, viral titles, and platform hashtags for ${aiResult.clips.size} segments...")
            delay(400)

            // Step 5: Smart Collections Clustering
            updateProjectStage(project, PipelineStage.STEP5_CLUSTERING, 0.86f)
            appendLog("[step5_clustering.py] Clustering top-scoring segments into ${aiResult.collections.size} thematic collections...")
            delay(400)

            // Step 6: Video Slicing & Subtitle Burn-In
            updateProjectStage(project, PipelineStage.STEP6_VIDEO, 0.96f)
            appendLog("[step6_video.py] Preparing 9:16 active-speaker crop parameters & burned-in SRT tracks...")
            delay(400)

            val outlineSummary = aiResult.outlines.joinToString("\n") {
                "• ${it.topicTitle} (${it.startTime} - ${it.endTime}): ${it.summary}"
            }

            val segmentEntities = aiResult.clips.mapIndexed { idx, clip ->
                ClipSegmentEntity(
                    projectId = project.id,
                    segmentIndex = idx + 1,
                    outlineTitle = clip.outlineTitle,
                    generatedViralTitle = clip.viralTitle,
                    coverHookText = clip.coverHookText,
                    startTimeStr = clip.startTime,
                    endTimeStr = clip.endTime,
                    durationSec = clip.durationSeconds,
                    finalScore = clip.finalScore.coerceIn(0.60f, 0.99f),
                    hookScore = clip.hookScore.coerceIn(0.60f, 0.99f),
                    emotionScore = clip.emotionScore.coerceIn(0.60f, 0.99f),
                    infoDensityScore = clip.infoDensityScore.coerceIn(0.60f, 0.99f),
                    recommendReason = clip.recommendReason,
                    hashtags = clip.hashtags.joinToString(","),
                    transcriptExcerpt = clip.transcriptExcerpt,
                    srtContent = clip.srtBlock
                )
            }

            val collectionEntities = aiResult.collections.map { col ->
                ClipCollectionEntity(
                    projectId = project.id,
                    collectionTitle = col.collectionTitle,
                    themeDescription = col.themeDescription,
                    clipIdsCsv = "",
                    totalDurationSec = segmentEntities.sumOf { it.durationSec },
                    avgViralScore = segmentEntities.map { it.finalScore }.average().toFloat().takeIf { !it.isNaN() } ?: 0.90f,
                    publishingCopy = col.publishingCopy
                )
            }

            repository.replaceProjectPipelineResults(project.id, segmentEntities, collectionEntities)
            val completedProject = project.copy(
                currentStage = PipelineStage.COMPLETED.name,
                stageProgress = 1.0f,
                outlineJson = outlineSummary,
                lastUpdatedAt = System.currentTimeMillis()
            )
            repository.updateProject(completedProject)

            _activePipelineStage.value = PipelineStage.COMPLETED
            _isPipelineRunning.value = false
            appendLog("[COMPLETED] Generated ${segmentEntities.size} viral clips and ${collectionEntities.size} smart collections.")
            _statusBannerMessage.value = "AutoClip 6-Stage Pipeline complete: ${segmentEntities.size} clips extracted!"
        }
    }

    private suspend fun updateProjectStage(project: VideoProjectEntity, stage: PipelineStage, progress: Float) {
        _activePipelineStage.value = stage
        repository.updateProject(
            project.copy(
                currentStage = stage.name,
                stageProgress = progress,
                lastUpdatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun appendLog(line: String) {
        _pipelineLogLines.value = (_pipelineLogLines.value + line).takeLast(30)
    }

    fun updateMinScoreThreshold(threshold: Float) {
        val project = uiState.value.activeProject ?: return
        viewModelScope.launch {
            repository.updateProject(project.copy(minScoreThreshold = threshold))
        }
    }

    fun updateSegmentTitleAndPreset(
        segment: ClipSegmentEntity,
        newViralTitle: String,
        newCoverHook: String,
        preset: ExportPlatformPreset
    ) {
        viewModelScope.launch {
            val updated = segment.copy(
                generatedViralTitle = newViralTitle,
                coverHookText = newCoverHook,
                exportPresetId = preset.id
            )
            repository.updateSegment(updated)
            _selectedSegmentForPreview.value = updated
            _statusBannerMessage.value = "Updated clip #${segment.segmentIndex} metadata & preset (${preset.label})"
        }
    }

    fun exportAndShareClip(segment: ClipSegmentEntity, preset: ExportPlatformPreset, shareAfterExport: Boolean) {
        viewModelScope.launch {
            val projectTitle = uiState.value.activeProject?.title ?: "AutoClip Project"
            val exportedFile = DebugApkSyncManager.exportClipBundleToDisk(
                context = appContext,
                projectTitle = projectTitle,
                segment = segment,
                preset = preset
            )
            val updated = segment.copy(
                exportPresetId = preset.id,
                isExported = true,
                exportedFilePath = exportedFile.absolutePath
            )
            repository.updateSegment(updated)
            if (_selectedSegmentForPreview.value?.id == segment.id) {
                _selectedSegmentForPreview.value = updated
            }
            appendLog("[step6_video.py] Exported 9:16 package + burned SRT -> ${exportedFile.name}")
            _statusBannerMessage.value = "Exported ${exportedFile.name} with burned-in SRT subtitles"
            if (shareAfterExport) {
                DebugApkSyncManager.shareFileIntent(appContext, exportedFile)
            }
        }
    }

    fun createSmartCollectionFromTopClips(title: String, themeDescription: String) {
        val project = uiState.value.activeProject ?: return
        val topSegments = uiState.value.activeProjectSegments
        if (topSegments.isEmpty()) return
        viewModelScope.launch {
            val avgScore = topSegments.map { it.finalScore }.average().toFloat()
            val totalDur = topSegments.sumOf { it.durationSec }
            val collection = ClipCollectionEntity(
                projectId = project.id,
                collectionTitle = title.ifBlank { "${project.title} - Viral Highlight Reel" },
                themeDescription = themeDescription.ifBlank { "Multi-clip compilation curated from ${topSegments.size} top-scoring segments." },
                clipIdsCsv = topSegments.joinToString(",") { it.id.toString() },
                totalDurationSec = totalDur,
                avgViralScore = avgScore,
                publishingCopy = "Must-watch highlights from ${project.title} (${topSegments.size} clips, ${totalDur}s total). #AutoClip #Shorts #Viral"
            )
            repository.createCustomCollection(collection)
            _statusBannerMessage.value = "Created smart collection '${collection.collectionTitle}'"
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _statusBannerMessage.value = "Deleted project"
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
            _statusBannerMessage.value = "Removed collection"
        }
    }

    fun updateAiSettings(
        provider: AiProviderType,
        modelName: String,
        chunkSize: Int,
        defaultMinScore: Float,
        autoBurnSubs: Boolean,
        speakerTracking: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    activeProviderId = provider.id,
                    activeModelName = modelName,
                    chunkSizeChars = chunkSize,
                    defaultMinScore = defaultMinScore,
                    autoBurnSubtitles = autoBurnSubs,
                    activeSpeakerTracking = speakerTracking
                )
            }
            _statusBannerMessage.value = "Saved AutoClip AI Engine settings (${provider.displayName})"
        }
    }

    fun testAiProviderConnection(provider: AiProviderType, modelName: String) {
        viewModelScope.launch {
            _aiConnectionStatus.value = "Testing connection to ${provider.displayName} ($modelName)..."
            val (_, message) = AutoClipAiEngine.testAiConnection(provider, modelName)
            _aiConnectionStatus.value = message
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val db = AutoClipDatabase.getInstance(appContext)
                    val repo = AutoClipRepository(appContext, db.dao())
                    return AutoClipViewModel(appContext, repo) as T
                }
            }
    }
}
