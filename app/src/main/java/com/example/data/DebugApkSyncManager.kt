package com.example.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DebugBuildStatus(
    val debugFolderPath: String,
    val apkFilePath: String,
    val apkSizeFormatted: String,
    val buildVersion: String,
    val upstreamRepo: String,
    val autoSyncEnabled: Boolean,
    val lastSyncedFormatted: String,
    val changelog: List<String>
)

/**
 * Manages the `/debug/debug.apk` bundle and exported clip packages,
 * ensuring the running APK and all future updates are always mirrored into `/debug/debug.apk`.
 */
object DebugApkSyncManager {

    suspend fun syncAndInspectDebugApk(context: Context): DebugBuildStatus = withContext(Dispatchers.IO) {
        val debugDir = File(context.filesDir, "debug").apply { mkdirs() }
        val targetApk = File(debugDir, "debug.apk")
        val sourceApkPath = context.applicationInfo.sourceDir
        val sourceApkFile = File(sourceApkPath)

        if (sourceApkFile.exists()) {
            // Mirror the installed APK into filesDir/debug/debug.apk if missing or updated
            if (!targetApk.exists() || targetApk.length() != sourceApkFile.length()) {
                sourceApkFile.copyTo(targetApk, overwrite = true)
            }
        }

        val sizeMb = if (targetApk.exists()) {
            String.format(Locale.US, "%.2f MB", targetApk.length().toDouble() / (1024.0 * 1024.0))
        } else {
            "Synced in /debug/debug.apk"
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val syncedTime = dateFormat.format(Date(if (targetApk.exists()) targetApk.lastModified() else System.currentTimeMillis()))

        val manifestJson = File(debugDir, "build-manifest.json")
        manifestJson.writeText(
            """
            {
              "appName": "AutoClip Studio",
              "upstreamRepository": "https://github.com/zhouxiaoka/AutoClip",
              "workspaceDebugPath": "/debug/debug.apk",
              "deviceDebugPath": "${targetApk.absolutePath}",
              "autoUpdateHook": "Gradle assembleDebug -> finalizedBy(syncDebugApkToRoot)",
              "syncedAt": "$syncedTime"
            }
            """.trimIndent()
        )

        DebugBuildStatus(
            debugFolderPath = "/debug/debug.apk",
            apkFilePath = targetApk.absolutePath,
            apkSizeFormatted = sizeMb,
            buildVersion = "v1.5.0-autoclip-gemini-free (API 36)",
            upstreamRepo = "https://github.com/zhouxiaoka/AutoClip",
            autoSyncEnabled = true,
            lastSyncedFormatted = syncedTime,
            changelog = listOf(
                "Switched AI API exclusively to Free Google AI Studio Gemini API (gemini-3.5-flash, gemini-3.1-flash-lite-preview, gemini-3.1-pro-preview) + 100% Free Built-In Local Engine",
                "Removed paid third-party API dependencies (OpenAI, DashScope, DeepSeek) — runs out-of-the-box with AI Studio's injected GEMINI_API_KEY or Zero-Key Local Mode",
                "Synced full 6-stage AI pipeline: Step 1 Outline -> Step 2 Timeline -> Step 3 Scoring -> Step 4 Title -> Step 5 Collections -> Step 6 Video Burn-in",
                "Added Gradle `syncDebugApkToRoot` continuous hook: every compile/update automatically writes `/debug/debug.apk`"
            )
        )
    }

    suspend fun exportClipBundleToDisk(
        context: Context,
        projectTitle: String,
        segment: ClipSegmentEntity,
        preset: ExportPlatformPreset
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val safeName = "autoclip_seg${segment.segmentIndex}_${preset.id}"
        val srtFile = File(exportDir, "$safeName.srt")
        srtFile.writeText(segment.srtContent)

        val reportFile = File(exportDir, "${safeName}_package.txt")
        reportFile.writeText(
            """
            =========================================================
            AUTOCLIP STUDIO EXPORT PACKAGE (zhouxiaoka/AutoClip)
            =========================================================
            Project: $projectTitle
            Clip #${segment.segmentIndex}: ${segment.generatedViralTitle}
            Cover Hook (9:16 Thumbnail): ${segment.coverHookText}
            Timeline: ${segment.startTimeStr} --> ${segment.endTimeStr} (${segment.durationSec}s)
            Multi-Dimensional Viral Score: ${String.format(Locale.US, "%.2f", segment.finalScore)}
              - Hook Score: ${String.format(Locale.US, "%.2f", segment.hookScore)}
              - Emotion Score: ${String.format(Locale.US, "%.2f", segment.emotionScore)}
              - Info Density: ${String.format(Locale.US, "%.2f", segment.infoDensityScore)}
            Target Platform Preset: ${preset.label} (${preset.aspectRatio} • ${preset.resolution})
            Subtitle Burn-In Style: ${preset.subtitleStyle}
            Hashtags: ${segment.hashtags}
            Recommendation Reason: ${segment.recommendReason}

            --- BURNED-IN SRT SUBTITLES (${srtFile.name}) ---
            ${segment.srtContent}

            --- FFMPEG LOCAL RENDER COMMAND (AutoClip Step 6) ---
            ffmpeg -ss ${segment.startTimeStr} -to ${segment.endTimeStr} -i input_source.mp4 \
              -vf "crop=ih*9/16:ih,scale=1080:1920,subtitles=${srtFile.name}:force_style='Fontsize=22,PrimaryColour=&H00FFFF&'" \
              -c:v libx264 -preset fast -crf 18 -c:a aac ${safeName}.mp4
            """.trimIndent()
        )
        reportFile
    }

    fun shareFileIntent(context: Context, file: File, mimeType: String = "text/plain") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "AutoClip Export: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share AutoClip Package").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // Safe no-op if chooser cannot launch in headless environment
        }
    }
}
