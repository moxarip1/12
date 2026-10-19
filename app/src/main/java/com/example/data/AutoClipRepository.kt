package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "autoclip_settings")

data class AutoClipSettings(
    val activeProviderId: String = AiProviderType.GEMINI_FLASH.id,
    val activeModelName: String = "gemini-3.5-flash",
    val chunkSizeChars: Int = 4500,
    val defaultMinScore: Float = 0.78f,
    val maxClipsPerCollection: Int = 5,
    val autoBurnSubtitles: Boolean = true,
    val activeSpeakerTracking: Boolean = true,
    val autoSyncDebugApk: Boolean = true
)

class AutoClipRepository(private val context: Context, private val dao: AutoClipDao) {

    private object Keys {
        val PROVIDER_ID = stringPreferencesKey("provider_id")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val CHUNK_SIZE = intPreferencesKey("chunk_size")
        val MIN_SCORE = floatPreferencesKey("min_score")
        val MAX_CLIPS_COLLECTION = intPreferencesKey("max_clips_collection")
        val AUTO_BURN_SUBS = booleanPreferencesKey("auto_burn_subs")
        val SPEAKER_TRACKING = booleanPreferencesKey("speaker_tracking")
        val AUTO_SYNC_APK = booleanPreferencesKey("auto_sync_apk")
        val SEEDED_SAMPLE_DATA = booleanPreferencesKey("seeded_sample_data")
    }

    val allProjects: Flow<List<VideoProjectEntity>> = dao.getAllProjects()
    val allSegments: Flow<List<ClipSegmentEntity>> = dao.getAllSegments()
    val allCollections: Flow<List<ClipCollectionEntity>> = dao.getAllCollections()

    val settingsFlow: Flow<AutoClipSettings> = context.dataStore.data.map { prefs ->
        AutoClipSettings(
            activeProviderId = prefs[Keys.PROVIDER_ID] ?: AiProviderType.GEMINI_FLASH.id,
            activeModelName = prefs[Keys.MODEL_NAME] ?: "gemini-3.5-flash",
            chunkSizeChars = prefs[Keys.CHUNK_SIZE] ?: 4500,
            defaultMinScore = prefs[Keys.MIN_SCORE] ?: 0.78f,
            maxClipsPerCollection = prefs[Keys.MAX_CLIPS_COLLECTION] ?: 5,
            autoBurnSubtitles = prefs[Keys.AUTO_BURN_SUBS] ?: true,
            activeSpeakerTracking = prefs[Keys.SPEAKER_TRACKING] ?: true,
            autoSyncDebugApk = prefs[Keys.AUTO_SYNC_APK] ?: true
        )
    }

    suspend fun updateSettings(transform: (AutoClipSettings) -> AutoClipSettings) {
        val current = settingsFlow.first()
        val updated = transform(current)
        context.dataStore.edit { prefs ->
            prefs[Keys.PROVIDER_ID] = updated.activeProviderId
            prefs[Keys.MODEL_NAME] = updated.activeModelName
            prefs[Keys.CHUNK_SIZE] = updated.chunkSizeChars
            prefs[Keys.MIN_SCORE] = updated.defaultMinScore
            prefs[Keys.MAX_CLIPS_COLLECTION] = updated.maxClipsPerCollection
            prefs[Keys.AUTO_BURN_SUBS] = updated.autoBurnSubtitles
            prefs[Keys.SPEAKER_TRACKING] = updated.activeSpeakerTracking
            prefs[Keys.AUTO_SYNC_APK] = updated.autoSyncDebugApk
        }
    }

    suspend fun seedInitialProjectsIfEmpty() {
        val alreadySeeded = context.dataStore.data.first()[Keys.SEEDED_SAMPLE_DATA] ?: false
        if (alreadySeeded) return

        context.dataStore.edit { it[Keys.SEEDED_SAMPLE_DATA] = true }

        val project1 = VideoProjectEntity(
            title = "AI Agent Engineering & Autonomous Video Workflows (EP. 42)",
            sourceType = "YOUTUBE",
            sourceUrlOrUri = "https://www.youtube.com/watch?v=autoclip_ai_agents",
            categoryId = VideoCategory.KNOWLEDGE.id,
            durationSeconds = 2640,
            transcriptSrt = SAMPLE_SRT_1,
            currentStage = PipelineStage.COMPLETED.name,
            stageProgress = 1.0f,
            minScoreThreshold = 0.78f,
            outlineJson = "Step 1: Why Long-Form Video Needs Semantic Slicing (00:00:00 - 00:08:30)\nStep 2: Multi-Stage LLM Scoring vs Keyword Matching (00:08:30 - 00:19:15)\nStep 3: 9:16 Active Speaker Reframing & Burned SRTs (00:19:15 - 00:32:00)\nStep 4: Automated Thematic Collections (00:32:00 - 00:44:00)"
        )
        val project1Id = dao.insertProject(project1)

        val p1Segments = listOf(
            ClipSegmentEntity(
                projectId = project1Id,
                segmentIndex = 1,
                outlineTitle = "Why Long-Form Video Needs Semantic Slicing",
                generatedViralTitle = "Why 95% of Creators Waste 6 Hours Cutting Podcast Clips Manually",
                coverHookText = "STOP EDITING MANUALLY",
                startTimeStr = "00:02:14",
                endTimeStr = "00:03:09",
                durationSec = 55,
                finalScore = 0.96f,
                hookScore = 0.98f,
                emotionScore = 0.92f,
                infoDensityScore = 0.97f,
                recommendReason = "Opens with a counter-intuitive statistic and immediately contrasts manual timeline scrubbing against 6-stage AI slicing.",
                hashtags = "#AutoClip,#AICreator,#VideoEditing,#PodcastClips,#Shorts",
                transcriptExcerpt = "Most creators spend 6 hours scrubbing timelines for a single 45-second hook. When you let a 6-stage LLM pipeline read the structural outline first, it spots emotional peaks before touching a frame.",
                srtContent = """
                    1
                    00:02:14,000 --> 00:02:31,500
                    Most creators spend 6 hours scrubbing timelines for a single 45-second hook.

                    2
                    00:02:31,500 --> 00:02:51,000
                    When you let a 6-stage LLM pipeline read the structural outline first,

                    3
                    00:02:51,000 --> 00:03:09,000
                    it spots emotional peaks and viral soundbites before touching a single frame.
                """.trimIndent(),
                exportPresetId = ExportPlatformPreset.TIKTOK.id,
                isExported = true,
                exportedFilePath = "/exports/autoclip_seg1_tiktok_package.txt"
            ),
            ClipSegmentEntity(
                projectId = project1Id,
                segmentIndex = 2,
                outlineTitle = "Multi-Stage LLM Scoring vs Keyword Matching",
                generatedViralTitle = "The 3 Metrics That Predict a Million-View Short Before You Export",
                coverHookText = "THE 3 VIRAL METRICS",
                startTimeStr = "00:11:40",
                endTimeStr = "00:12:38",
                durationSec = 58,
                finalScore = 0.92f,
                hookScore = 0.94f,
                emotionScore = 0.89f,
                infoDensityScore = 0.93f,
                recommendReason = "Clear 3-part mental model (Hook Strength, Emotional Arc, Self-Contained Completeness) ideal for educational Shorts.",
                hashtags = "#ContentStrategy,#ViralHooks,#AutoClip,#YouTubeShorts",
                transcriptExcerpt = "In Step 3 Scoring, we never just look for loud audio. We grade every candidate clip on three axes: opening 3-second hook tension, emotional resonance, and standalone narrative completeness.",
                srtContent = """
                    1
                    00:11:40,000 --> 00:11:59,000
                    In Step 3 Scoring, we never just look for loud audio spikes.

                    2
                    00:11:59,000 --> 00:12:20,000
                    We grade every candidate clip on 3 axes: 3-second hook tension,

                    3
                    00:12:20,000 --> 00:12:38,000
                    emotional resonance, and standalone narrative completeness.
                """.trimIndent(),
                exportPresetId = ExportPlatformPreset.SHORTS.id,
                isExported = false
            ),
            ClipSegmentEntity(
                projectId = project1Id,
                segmentIndex = 3,
                outlineTitle = "9:16 Active Speaker Reframing & Burned SRTs",
                generatedViralTitle = "How Local FFmpeg + Whisper Burns Dynamic Subtitles Without Cloud Fees",
                coverHookText = "100% LOCAL & ZERO WATERMARK",
                startTimeStr = "00:23:05",
                endTimeStr = "00:23:54",
                durationSec = 49,
                finalScore = 0.89f,
                hookScore = 0.90f,
                emotionScore = 0.86f,
                infoDensityScore = 0.91f,
                recommendReason = "Directly addresses creator pain points around costly SaaS subscriptions and slow cloud uploads.",
                hashtags = "#OpenSource,#FFmpeg,#WhisperAI,#LocalFirst,#AutoClip",
                transcriptExcerpt = "Because video cutting and subtitle burning run locally with hardware acceleration, you never upload a 4GB raw file to a cloud server or pay monthly watermark removal fees.",
                srtContent = """
                    1
                    00:23:05,000 --> 00:23:22,000
                    Because video cutting and subtitle burning run locally on your device,

                    2
                    00:23:22,000 --> 00:23:39,000
                    you never upload a 4GB raw recording to a slow cloud queue

                    3
                    00:23:39,000 --> 00:23:54,000
                    or pay monthly subscriptions just to remove watermarks.
                """.trimIndent(),
                exportPresetId = ExportPlatformPreset.REELS.id,
                isExported = false
            )
        )
        val insertedIds = dao.insertSegments(p1Segments)

        val p1Collections = listOf(
            ClipCollectionEntity(
                projectId = project1Id,
                collectionTitle = "AI Video Automation Masterclass (Top 3 Viral Hooks)",
                themeDescription = "Clustered by Step 5 AI: Combines the highest-scoring hooks on semantic slicing, 3-axis viral scoring, and local rendering.",
                clipIdsCsv = insertedIds.joinToString(","),
                totalDurationSec = 162,
                avgViralScore = 0.92f,
                publishingCopy = "How open-source AI turns a 44-minute technical podcast into 3 viral vertical clips in minutes. #AutoClip #AIVideo #CreatorTools"
            )
        )
        dao.insertCollections(p1Collections)

        // Second sample project (Bilibili Deep Dive)
        val project2 = VideoProjectEntity(
            title = "Founder Breakdown: Building Open-Source AI Products From 0 to 10K Stars",
            sourceType = "BILIBILI",
            sourceUrlOrUri = "https://www.bilibili.com/video/BV1autoclip10k",
            categoryId = VideoCategory.BUSINESS.id,
            durationSeconds = 1980,
            transcriptSrt = SAMPLE_SRT_2,
            currentStage = PipelineStage.COMPLETED.name,
            stageProgress = 1.0f,
            minScoreThreshold = 0.75f,
            outlineJson = "Step 1: Solving Your Own Workflow Bottleneck (00:00:00 - 00:10:00)\nStep 2: Designing Modular Prompt Pipelines (00:10:00 - 00:22:00)\nStep 3: Community Feedback & Rapid Iteration (00:22:00 - 00:33:00)"
        )
        val project2Id = dao.insertProject(project2)
        val p2Segments = listOf(
            ClipSegmentEntity(
                projectId = project2Id,
                segmentIndex = 1,
                outlineTitle = "Solving Your Own Workflow Bottleneck",
                generatedViralTitle = "The #1 Mistake Developers Make When Shipping AI Tools",
                coverHookText = "DON'T BUILD WRAPPERS",
                startTimeStr = "00:04:12",
                endTimeStr = "00:05:02",
                durationSec = 50,
                finalScore = 0.91f,
                hookScore = 0.93f,
                emotionScore = 0.88f,
                infoDensityScore = 0.92f,
                recommendReason = "Strong opinionated founder insight with immediate practical relevance.",
                hashtags = "#OpenSource,#StartupLessons,#AIEngineering,#Bilibili",
                transcriptExcerpt = "Don't build a thin chat wrapper. Build an end-to-end deterministic pipeline where each prompt stage validates timestamps, scores quality, and hands off structured JSON to the next stage.",
                srtContent = """
                    1
                    00:04:12,000 --> 00:04:29,000
                    Never build a thin single-prompt wrapper for complex media workflows.

                    2
                    00:04:29,000 --> 00:04:46,000
                    Build a deterministic 6-stage pipeline where each stage validates timestamps

                    3
                    00:04:46,000 --> 00:05:02,000
                    and hands off clean structured JSON to the video cutter.
                """.trimIndent(),
                exportPresetId = ExportPlatformPreset.BILIBILI.id,
                isExported = false
            )
        )
        val p2SegIds = dao.insertSegments(p2Segments)
        dao.insertCollections(
            listOf(
                ClipCollectionEntity(
                    projectId = project2Id,
                    collectionTitle = "Open-Source AI Product Playbook",
                    themeDescription = "Curated founder insights for Bilibili & Xiaohongshu 3:4 vertical cards.",
                    clipIdsCsv = p2SegIds.joinToString(","),
                    totalDurationSec = 50,
                    avgViralScore = 0.91f,
                    publishingCopy = "Why deterministic multi-stage pipelines beat generic chat wrappers every time. #OpenSource #AutoClip"
                )
            )
        )
    }

    suspend fun createProject(project: VideoProjectEntity): Long = dao.insertProject(project)
    suspend fun updateProject(project: VideoProjectEntity) = dao.updateProject(project)
    suspend fun deleteProject(projectId: Long) {
        dao.deleteSegmentsForProject(projectId)
        dao.deleteCollectionsForProject(projectId)
        dao.deleteProject(projectId)
    }

    suspend fun replaceProjectPipelineResults(
        projectId: Long,
        segments: List<ClipSegmentEntity>,
        collections: List<ClipCollectionEntity>
    ) {
        dao.deleteSegmentsForProject(projectId)
        dao.deleteCollectionsForProject(projectId)
        val segIds = dao.insertSegments(segments)
        val mappedCollections = collections.map { col ->
            val resolvedCsv = if (col.clipIdsCsv.isBlank() && segIds.isNotEmpty()) {
                segIds.take(2).joinToString(",")
            } else {
                col.clipIdsCsv
            }
            col.copy(clipIdsCsv = resolvedCsv)
        }
        dao.insertCollections(mappedCollections)
    }

    suspend fun updateSegment(segment: ClipSegmentEntity) = dao.updateSegment(segment)

    suspend fun createCustomCollection(collection: ClipCollectionEntity) = dao.insertCollection(collection)

    suspend fun deleteCollection(collectionId: Long) = dao.deleteCollectionById(collectionId)

    companion object {
        val SAMPLE_SRT_1 = """
            1
            00:02:14,000 --> 00:02:31,500
            Most creators spend 6 hours scrubbing timelines for a single 45-second hook.

            2
            00:02:31,500 --> 00:02:51,000
            When you let a 6-stage LLM pipeline read the structural outline first, it spots emotional peaks before touching a frame.

            3
            00:11:40,000 --> 00:12:20,000
            In Step 3 Scoring, we grade every candidate clip on three axes: opening 3-second hook tension, emotional resonance, and standalone narrative completeness.

            4
            00:23:05,000 --> 00:23:54,000
            Because video cutting and subtitle burning run locally with hardware acceleration, you never upload a 4GB raw file to a cloud server.
        """.trimIndent()

        val SAMPLE_SRT_2 = """
            1
            00:04:12,000 --> 00:04:46,000
            Don't build a thin chat wrapper. Build an end-to-end deterministic pipeline where each prompt stage validates timestamps and scores quality.
        """.trimIndent()
    }
}
