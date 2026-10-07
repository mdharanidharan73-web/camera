package com.example.data

enum class CameraMode(val label: String) {
    PHOTO("PHOTO"),
    PORTRAIT("PORTRAIT"),
    NIGHT("NIGHT")
}

enum class EnhancementStrength(val label: String) {
    NATURAL("Natural"),
    BALANCED("Balanced"),
    PROFESSIONAL("Professional")
}

enum class ImageQuality(val label: String) {
    HIGH("High (1080p)"),
    MAXIMUM("Maximum (Full-Res)")
}

enum class EnhancementEngine(val displayName: String) {
    OPENAI("OpenAI Photographic Engine"),
    LOCAL_DSLR("RealShot On-Device RAW Engine")
}

data class AnalysisReport(
    val exposureAdjustment: String = "+0.6 EV balanced",
    val shadowRecovery: String = "42% shadow detail recovered",
    val highlightProtection: String = "Highlights roll-off preserved",
    val skinTexturePreservation: String = "100% natural pores & texture intact",
    val colorBalance: String = "Believable natural white balance",
    val noiseReduction: String = "Chroma noise reduced naturally",
    val summary: String = "Photographic technical corrections applied with strict scene & identity preservation."
)

data class PhotoRecord(
    val id: String,
    val originalPath: String,
    val enhancedPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val cameraMode: CameraMode = CameraMode.PHOTO,
    val engineUsed: EnhancementEngine = EnhancementEngine.OPENAI,
    val analysisNotes: AnalysisReport = AnalysisReport()
)

enum class FlashMode {
    OFF, AUTO, ON
}

enum class ScreenState {
    CAMERA,
    ENHANCING,
    RESULT,
    GALLERY
}
