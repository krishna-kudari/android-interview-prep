package com.example.interview.datastorelab.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.interview.datastorelab.data.model.AppSettings
import com.example.interview.datastorelab.data.model.Language
import com.example.interview.datastorelab.data.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences DataStore repository for primitive app settings.
 *
 * Interview: Why Preferences DataStore for THIS data, not Typed DataStore?
 *  - Each field is independently observable (e.g., theme-only flow for the AppBar)
 *  - No serialization overhead; DataStore natively handles Boolean/Int/String/Long/Float
 *  - Easier migration path from SharedPreferences (key-by-key mapping)
 *  - Trade-off: no compile-time schema enforcement — anyone can add a wrong key silently
 *
 * Typed DataStore is better when fields are tightly coupled and always read/written together.
 */
@Singleton
class AppSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ── Typed keys — defined as constants, not strings, to prevent typos ──────
    // Preferences.Key<T> is the type-safe replacement for raw string keys in SharedPreferences
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val FONT_SIZE = intPreferencesKey("font_size")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val LAST_OPENED_TS = longPreferencesKey("last_opened_ts")
    }

    // ── Full settings flow ────────────────────────────────────────────────────
    // Maps the raw Preferences (key-value bag) → our typed domain model.
    // Emits on EVERY key change, including changes to unrelated keys.
    val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            // IOException: disk full, permissions changed, etc.
            // Emit empty prefs → mapping produces all defaults → UI stays functional.
            // NEVER rethrow IOException here; let the app degrade gracefully.
            if (throwable is IOException) emit(emptyPreferences())
            else throw throwable  // propagate programming errors (e.g., ClassCastException)
        }
        .map { prefs ->
            AppSettings(
                theme = prefs[Keys.THEME]
                    ?.let { runCatching { Theme.valueOf(it) }.getOrDefault(Theme.SYSTEM) }
                    ?: Theme.SYSTEM,
                language = prefs[Keys.LANGUAGE]
                    ?.let { runCatching { Language.valueOf(it) }.getOrDefault(Language.ENGLISH) }
                    ?: Language.ENGLISH,
                fontSize = (prefs[Keys.FONT_SIZE] ?: 14).coerceIn(10, 28),
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                analyticsEnabled = prefs[Keys.ANALYTICS_ENABLED] ?: false,
                lastOpenedTimestamp = prefs[Keys.LAST_OPENED_TS] ?: 0L
            )
        }

    // ── Granular flows — observe a SINGLE key without re-collecting everything ─
    // Interview: Useful in ViewModel to avoid recomposition when unrelated keys change.
    val themeFlow: Flow<Theme> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            prefs[Keys.THEME]?.let { runCatching { Theme.valueOf(it) }.getOrDefault(Theme.SYSTEM) }
                ?: Theme.SYSTEM
        }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] ?: true }

    // ── Write operations ──────────────────────────────────────────────────────
    // edit {} is the Preferences DataStore equivalent of updateData {}.
    // The lambda runs on Dispatchers.IO; the block is transactional — all mutations
    // inside one edit {} are written atomically (single file rename).

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    suspend fun setLanguage(language: Language) {
        dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }

    suspend fun setFontSize(size: Int) {
        require(size in 10..28) { "Font size $size is outside supported range [10, 28]" }
        dataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE] = size
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ANALYTICS_ENABLED] = enabled
        }
    }

    /**
     * Atomic multi-key update.
     *
     * Interview: ALL mutations inside a single edit {} are written in one atomic operation.
     * If the process dies mid-write, the old values are preserved — no partial state.
     * This is the key advantage over SharedPreferences where three separate putX().apply()
     * calls are three separate non-atomic operations.
     */
    suspend fun applySettingsAtomically(theme: Theme, language: Language, fontSize: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
            prefs[Keys.LANGUAGE] = language.name
            prefs[Keys.FONT_SIZE] = fontSize.coerceIn(10, 28)
            prefs[Keys.LAST_OPENED_TS] = System.currentTimeMillis()
        }
    }

    suspend fun stampLastOpened() {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_OPENED_TS] = System.currentTimeMillis()
        }
    }

    /**
     * Remove a specific key without touching others.
     * Useful for selective resets (e.g., clear only the language pref).
     */
    suspend fun clearLanguagePreference() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.LANGUAGE)
        }
    }

    /** Wipe ALL preference keys. Equivalent to SharedPreferences.clear(). */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
