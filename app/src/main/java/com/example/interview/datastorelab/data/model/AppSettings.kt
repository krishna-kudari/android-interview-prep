package com.example.interview.datastorelab.data.model

/**
 * Domain model for app-wide settings stored in Preferences DataStore.
 * Plain data class — NOT @Serializable because Preferences DataStore stores
 * each field as an individual primitive under a typed key, not as a blob.
 */
data class AppSettings(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.ENGLISH,
    val fontSize: Int = 14,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val lastOpenedTimestamp: Long = 0L
)

enum class Theme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class Language(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("हिंदी", "hi"),
    SPANISH("Español", "es"),
    FRENCH("Français", "fr"),
    JAPANESE("日本語", "ja")
}
