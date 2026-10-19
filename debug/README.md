# AutoClip Studio — Continuous `/debug/debug.apk` Release Folder

This `/debug` directory always holds the latest compiled Android package (`debug.apk`) with all cumulative and future updates automatically synced on every build.

- **APK File:** `/debug/debug.apk`
- **Size:** 17.77 MB (18643028 bytes)
- **Version:** `1.5.0-autoclip-gemini-free`
- **AI Engine:** Google AI Studio Gemini API (`gemini-3.5-flash`, `gemini-3.1-flash-lite-preview`, `gemini-3.1-pro-preview`) + Free Built-In Local Engine
- **Last Auto-Synced (Epoch MS):** `1792408277994`
- **Continuous Update Hook:** Configured in `app/build.gradle.kts` (`syncDebugApkToRoot`) to overwrite `/debug/debug.apk` automatically after every `assembleDebug`, `packageDebug`, or future code update.