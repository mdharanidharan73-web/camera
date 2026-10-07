package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("realshot_prefs", Context.MODE_PRIVATE)

    var aiEnhancementEnabled: Boolean
        get() = prefs.getBoolean(KEY_AI_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AI_ENABLED, value).apply()

    var autoSaveToGallery: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply()

    var saveOriginalBackup: Boolean
        get() = prefs.getBoolean(KEY_SAVE_BACKUP, true)
        set(value) = prefs.edit().putBoolean(KEY_SAVE_BACKUP, value).apply()

    var enhancementStrength: EnhancementStrength
        get() {
            val name = prefs.getString(KEY_STRENGTH, EnhancementStrength.BALANCED.name)
            return try {
                EnhancementStrength.valueOf(name ?: EnhancementStrength.BALANCED.name)
            } catch (e: Exception) {
                EnhancementStrength.BALANCED
            }
        }
        set(value) = prefs.edit().putString(KEY_STRENGTH, value.name).apply()

    var imageQuality: ImageQuality
        get() {
            val name = prefs.getString(KEY_QUALITY, ImageQuality.HIGH.name)
            return try {
                ImageQuality.valueOf(name ?: ImageQuality.HIGH.name)
            } catch (e: Exception) {
                ImageQuality.HIGH
            }
        }
        set(value) = prefs.edit().putString(KEY_QUALITY, value.name).apply()

    var openAiModel: String
        get() {
            val saved = prefs.getString(KEY_OPENAI_MODEL, "")
            if (!saved.isNullOrBlank()) return saved
            return try {
                val config = BuildConfig.OPENAI_IMAGE_MODEL
                if (config.isNotBlank()) config else "dall-e-2"
            } catch (e: Exception) {
                "dall-e-2"
            }
        }
        set(value) = prefs.edit().putString(KEY_OPENAI_MODEL, value.trim()).apply()

    var customOpenAiKey: String
        get() = prefs.getString(KEY_CUSTOM_OPENAI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_OPENAI_KEY, value.trim()).apply()

    var backendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, "https://realshot-ai-backend.onrender.com") ?: "https://realshot-ai-backend.onrender.com"
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value.trim()).apply()

    fun getEffectiveOpenAiKey(): String {
        val custom = customOpenAiKey
        if (custom.isNotBlank()) return custom
        return try {
            val configKey = BuildConfig.OPENAI_API_KEY
            if (configKey != "MY_OPENAI_API_KEY" && configKey.isNotBlank()) {
                configKey
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun isOpenAiKeyConfigured(): Boolean {
        return getEffectiveOpenAiKey().isNotBlank()
    }

    fun getMaskedApiKey(): String {
        val key = getEffectiveOpenAiKey()
        if (key.isBlank()) return "Not Configured"
        if (key.length <= 10) return "••••••••"
        return "${key.take(7)}••••••••${key.takeLast(4)}"
    }

    companion object {
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_SAVE_BACKUP = "save_backup"
        private const val KEY_STRENGTH = "enhancement_strength"
        private const val KEY_QUALITY = "image_quality"
        private const val KEY_OPENAI_MODEL = "openai_model"
        private const val KEY_CUSTOM_OPENAI_KEY = "custom_openai_key"
        private const val KEY_BACKEND_URL = "backend_url"
    }
}
