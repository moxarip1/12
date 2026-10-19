package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents the 6-stage pipeline of zhouxiaoka/AutoClip:
 * Step 1: Outline Extraction (step1_outline.py)
 * Step 2: Timeline Segmentation (step2_timeline.py)
 * Step 3: Viral Scoring & Ranking (step3_scoring.py)
 * Step 4: Title & Copywriting Generation (step4_title.py)
 * Step 5: Thematic Clustering / Collections (step5_clustering.py)
 * Step 6: Video Clipping & Subtitle Burning (step6_video.py)
 */
enum class PipelineStage(val stepNumber: Int, val title: String, val scriptName: String, val description: String) {
    IDLE(0, "Ready to Process", "idle", "Waiting to start the 6-stage AutoClip AI pipeline"),
    STEP1_OUTLINE(1, "Step 1: Structural Outline", "step1_outline.py", "Analyzing SRT transcript chunks & extracting thematic topics"),
    STEP2_TIMELINE(2, "Step 2: Timeline Location", "step2_timeline.py", "Mapping topic boundaries to exact millisecond timestamps"),
    STEP3_SCORING(3, "Step 3: Multi-Dimensional Scoring", "step3_scoring.py", "Evaluating viral hook, emotional resonance & completeness (0.0 - 1.0)"),
    STEP4_TITLE(4, "Step 4: Viral Title & Hook Copy", "step4_title.py", "Generating platform-ready titles, cover hooks & hashtags"),
    STEP5_CLUSTERING(5, "Step 5: Smart Collections", "step5_clustering.py", "Grouping top clips into thematic multi-segment compilations"),
    STEP6_VIDEO(6, "Step 6: 9:16 Reframe & Burn-In", "step6_video.py", "Cutting segments, speaker framing & burning styled SRT subtitles"),
    COMPLETED(7, "Pipeline Complete", "completed", "All clips, titles, SRTs, and collections are ready for export"),
    FAILED(-1, "Pipeline Error", "failed", "Encountered an error during processing")
}

enum class VideoCategory(val id: String, val label: String, val iconName: String, val promptHint: String) {
    KNOWLEDGE("knowledge", "Knowledge & Tech", "science", "Focus on mental models, core insights, counter-intuitive facts, and actionable takeaways."),
    PODCAST("podcast", "Podcast & Interview", "mic", "Focus on emotional stories, sharp debates, quotable soundbites, and high-chemistry exchanges."),
    BUSINESS("business", "Business & Finance", "trending_up", "Focus on market strategies, revenue breakdowns, founder lessons, and high-stakes decisions."),
    COURSE("course", "Course & Tutorial", "school", "Focus on step-by-step frameworks, common pitfalls, and high-clarity explanations."),
    ENTERTAINMENT("entertainment", "Entertainment & Gaming", "movie", "Focus on comedic timing, plot twists, high-energy reactions, and viral moments.")
}

enum class AiProviderType(val id: String, val displayName: String, val defaultModel: String, val baseUrl: String) {
    GEMINI_FLASH("gemini_flash", "Google AI Studio • Gemini 3.5 Flash (Default Free Tier)", "gemini-3.5-flash", "https://generativelanguage.googleapis.com/"),
    GEMINI_LITE("gemini_lite", "Google AI Studio • Gemini 3.1 Flash Lite (Ultra-Fast Free Tier)", "gemini-3.1-flash-lite-preview", "https://generativelanguage.googleapis.com/"),
    GEMINI_PRO("gemini_pro", "Google AI Studio • Gemini 3.1 Pro (Deep Reasoning)", "gemini-3.1-pro-preview", "https://generativelanguage.googleapis.com/"),
    LOCAL_FREE("local_free", "Free Built-In Local Heuristic & Whisper Engine (Zero API Key)", "autoclip-local-v1", "local://autoclip-engine")
}

enum class ExportPlatformPreset(
    val id: String,
    val label: String,
    val aspectRatio: String,
    val resolution: String,
    val subtitleStyle: String
) {
    TIKTOK("tiktok", "TikTok / Douyin 9:16", "9:16", "1080x1920", "Bold Yellow Pop + Active Speaker Crop"),
    SHORTS("shorts", "YouTube Shorts 9:16", "9:16", "1080x1920", "Clean White Box + Dynamic Zoom"),
    REELS("reels", "Instagram Reels 9:16", "9:16", "1080x1920", "Minimal Kinetic + Gradient Backdrop"),
    BILIBILI("bilibili", "Bilibili / RedNote 3:4", "3:4", "1080x1440", "Dual-Line Bilingual + Topic Banner"),
    ORIGINAL("original", "Original Landscape 16:9", "16:9", "1920x1080", "Cinema Bottom Subtitle Burn-in")
}

@Entity(tableName = "autoclip_projects")
data class VideoProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceType: String, // "YOUTUBE", "BILIBILI", "LOCAL_FILE"
    val sourceUrlOrUri: String,
    val categoryId: String,
    val durationSeconds: Int,
    val transcriptSrt: String,
    val currentStage: String = PipelineStage.IDLE.name,
    val stageProgress: Float = 0f,
    val minScoreThreshold: Float = 0.72f,
    val outlineJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "autoclip_segments")
data class ClipSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val segmentIndex: Int,
    val outlineTitle: String,
    val generatedViralTitle: String,
    val coverHookText: String,
    val startTimeStr: String, // e.g. "00:01:15"
    val endTimeStr: String,   // e.g. "00:02:08"
    val durationSec: Int,
    val finalScore: Float,    // 0.0 to 1.0
    val hookScore: Float,
    val emotionScore: Float,
    val infoDensityScore: Float,
    val recommendReason: String,
    val hashtags: String,     // comma-separated
    val transcriptExcerpt: String,
    val srtContent: String,
    val exportPresetId: String = ExportPlatformPreset.TIKTOK.id,
    val isExported: Boolean = false,
    val exportedFilePath: String = ""
)

@Entity(tableName = "autoclip_collections")
data class ClipCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val collectionTitle: String,
    val themeDescription: String,
    val clipIdsCsv: String, // comma-separated segment IDs
    val totalDurationSec: Int,
    val avgViralScore: Float,
    val publishingCopy: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Structured AI output models for Gemini / LLM parsing
@Serializable
data class AiPipelineAnalysisResponse(
    val projectSummary: String = "",
    val outlines: List<AiOutlineNode> = emptyList(),
    val clips: List<AiDetectedClip> = emptyList(),
    val collections: List<AiThematicCollection> = emptyList()
)

@Serializable
data class AiOutlineNode(
    val topicTitle: String = "",
    val startTime: String = "00:00:00",
    val endTime: String = "00:01:00",
    val summary: String = ""
)

@Serializable
data class AiDetectedClip(
    val index: Int = 1,
    val outlineTitle: String = "",
    val viralTitle: String = "",
    val coverHookText: String = "",
    val startTime: String = "00:00:15",
    val endTime: String = "00:01:10",
    val durationSeconds: Int = 55,
    val finalScore: Float = 0.88f,
    val hookScore: Float = 0.90f,
    val emotionScore: Float = 0.85f,
    val infoDensityScore: Float = 0.89f,
    val recommendReason: String = "",
    val hashtags: List<String> = emptyList(),
    val transcriptExcerpt: String = "",
    val srtBlock: String = ""
)

@Serializable
data class AiThematicCollection(
    val collectionTitle: String = "",
    val themeDescription: String = "",
    val clipIndices: List<Int> = emptyList(),
    val publishingCopy: String = ""
)
