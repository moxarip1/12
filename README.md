# AutoClip Studio (Android Port of `zhouxiaoka/AutoClip`)

An AI-powered video highlight clipper, 9:16 vertical reframing studio, burned-in SRT subtitle generator, and thematic collection manager modeled after [`zhouxiaoka/AutoClip`](https://github.com/zhouxiaoka/AutoClip).

## Core 6-Stage AI Pipeline
1. **Step 1: Structural Outline (`step1_outline.py`)** — Splits video SRT transcripts into semantic chunks and extracts macro topic boundaries.
2. **Step 2: Timeline Segmentation (`step2_timeline.py`)** — Maps topic boundaries to exact millisecond timestamps.
3. **Step 3: Multi-Dimensional Viral Scoring (`step3_scoring.py`)** — Scores candidate segments (0.00 – 1.00) on 3-Second Hook Tension, Emotional Resonance, and Standalone Information Density.
4. **Step 4: Viral Title & Hook Copywriting (`step4_title.py`)** — Generates punchy short-form titles, 9:16 thumbnail cover hooks, and platform hashtags.
5. **Step 5: Smart Thematic Collections (`step5_clustering.py`)** — Clusters top-scoring clips into multi-segment compilations with ready-to-publish social copy.
6. **Step 6: 9:16 Speaker Reframe & Subtitle Burn-In (`step6_video.py`)** — Configures 9:16 vertical speaker tracking, burned-in SRT subtitles, and local FFmpeg export scripts for TikTok, YouTube Shorts, Instagram Reels, and Bilibili.

## Continuous `/debug/debug.apk` Build Sync
- Every debug build (`assembleDebug` / `packageDebug`) automatically runs the `syncDebugApkToRoot` Gradle task in `app/build.gradle.kts`, copying the latest compiled APK directly to `/debug/debug.apk` alongside `/debug/build-manifest.json`.
- Any future code or dependency update automatically updates `/debug/debug.apk`.
