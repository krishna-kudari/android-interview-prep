package com.example.interview.datastorelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interview.datastorelab.data.AppSettingsStore
import com.example.interview.datastorelab.data.SessionStore
import com.example.interview.datastorelab.data.UserProfileStore
import com.example.interview.datastorelab.data.model.Address
import com.example.interview.datastorelab.data.model.AppSettings
import com.example.interview.datastorelab.data.model.ContentPrefs
import com.example.interview.datastorelab.data.model.Language
import com.example.interview.datastorelab.data.model.SessionData
import com.example.interview.datastorelab.data.model.Theme
import com.example.interview.datastorelab.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class DataStoreLabUiState(
    val settings: AppSettings = AppSettings(),
    val userProfile: UserProfile = UserProfile(),
    val session: SessionData = SessionData(),
    val snackMessage: String = ""
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * ViewModel that bridges three DataStore repositories to the Compose UI.
 *
 * Key patterns demonstrated:
 *
 * 1. combine() — merges emissions from multiple independent Flows into one.
 *    Emits a new value whenever ANY of the three DataStores writes something new.
 *    Internally uses a latest-value cache per Flow (like combineLatest in RxJava).
 *
 * 2. stateIn(WhileSubscribed(5_000)) — converts a cold Flow to a hot StateFlow.
 *    The upstream coroutine stays active for 5 seconds after the last collector
 *    drops (e.g., Activity goes to background). This avoids re-reading from disk
 *    on quick orientation changes while still freeing resources during long pauses.
 *
 * 3. viewModelScope.launch — suspending DataStore write operations are launched in the
 *    ViewModel scope. If the user taps quickly, launches are sequential per store
 *    (DataStore serializes writes internally), so no data races.
 */
@HiltViewModel
class DataStoreLabViewModel @Inject constructor(
    private val appSettingsStore: AppSettingsStore,
    private val userProfileStore: UserProfileStore,
    private val sessionStore: SessionStore
) : ViewModel() {

    // ── Internal event state ──────────────────────────────────────────────────
    private val _snack = MutableStateFlow("")

    // ── Combined UI state ─────────────────────────────────────────────────────
    // combine() starts collecting ALL three flows simultaneously.
    // The lambda is re-invoked whenever any one of them emits.
    // Interview: combine() vs. zip():
    //   zip() waits for BOTH streams to emit before producing a pair.
    //   combine() uses the LATEST value from each stream — exactly what we want here.
    val uiState: StateFlow<DataStoreLabUiState> = combine(
        appSettingsStore.appSettingsFlow,
        userProfileStore.userProfileFlow,
        sessionStore.sessionFlow,
        _snack
    ) { settings, profile, session, snack ->
        DataStoreLabUiState(
            settings = settings,
            userProfile = profile,
            session = session,
            snackMessage = snack
        )
    }
        .catch { emit(DataStoreLabUiState(snackMessage = "Error: ${it.message}")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataStoreLabUiState()
        )

    // ── Granular StateFlows — for targeted observation in sub-composables ──────
    // These avoid re-collecting the full uiState when a child composable only
    // cares about one piece of data. Each is backed by its own DataStore Flow.
    val theme: StateFlow<Theme> = appSettingsStore.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Theme.SYSTEM)

    val isLoggedIn: StateFlow<Boolean> = sessionStore.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val badgeCount: StateFlow<Int> = userProfileStore.badgeCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── App Settings operations ───────────────────────────────────────────────

    fun setTheme(theme: Theme) = viewModelScope.launch {
        appSettingsStore.setTheme(theme)
        _snack.value = "Theme set to ${theme.displayName}"
    }

    fun setLanguage(language: Language) = viewModelScope.launch {
        appSettingsStore.setLanguage(language)
        _snack.value = "Language set to ${language.displayName}"
    }

    fun setFontSize(size: Int) = viewModelScope.launch {
        appSettingsStore.setFontSize(size)
        _snack.value = "Font size set to ${size}sp"
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        appSettingsStore.setNotificationsEnabled(enabled)
    }

    fun setAnalyticsEnabled(enabled: Boolean) = viewModelScope.launch {
        appSettingsStore.setAnalyticsEnabled(enabled)
    }

    /** Demonstrates atomic multi-key write — all three keys in one disk operation. */
    fun applySettingsAtomically(theme: Theme, language: Language, fontSize: Int) =
        viewModelScope.launch {
            appSettingsStore.applySettingsAtomically(theme, language, fontSize)
            _snack.value = "Settings applied atomically ✓"
        }

    fun clearSettings() = viewModelScope.launch {
        appSettingsStore.clearAll()
        _snack.value = "Settings cleared (defaults restored)"
    }

    // ── User Profile operations ───────────────────────────────────────────────

    fun saveFullProfile(profile: UserProfile) = viewModelScope.launch {
        userProfileStore.saveProfile(profile)
        _snack.value = "Full profile saved"
    }

    fun updateDisplayName(name: String) = viewModelScope.launch {
        userProfileStore.updateDisplayName(name)
        _snack.value = "Display name updated"
    }

    fun updateEmail(email: String) = viewModelScope.launch {
        userProfileStore.updateEmail(email)
    }

    fun updateBio(bio: String) = viewModelScope.launch {
        userProfileStore.updateBio(bio)
    }

    fun updateAddress(address: Address) = viewModelScope.launch {
        userProfileStore.updateAddress(address)
        _snack.value = "Address updated"
    }

    fun addBadge(badge: String) = viewModelScope.launch {
        userProfileStore.addBadge(badge)
        _snack.value = "Badge '$badge' added"
    }

    fun removeBadge(badge: String) = viewModelScope.launch {
        userProfileStore.removeBadge(badge)
        _snack.value = "Badge '$badge' removed"
    }

    fun incrementStat(key: String) = viewModelScope.launch {
        userProfileStore.incrementStat(key)
        _snack.value = "Stat '$key' incremented"
    }

    fun updateContentPrefs(prefs: ContentPrefs) = viewModelScope.launch {
        userProfileStore.updateContentPrefs(prefs)
        _snack.value = "Content preferences updated"
    }

    fun clearProfile() = viewModelScope.launch {
        userProfileStore.clearProfile()
        _snack.value = "Profile cleared"
    }

    // ── Session operations ────────────────────────────────────────────────────

    fun login(userId: String, roles: List<String> = listOf("USER", "PREMIUM")) =
        viewModelScope.launch {
            sessionStore.login(userId, roles)
            _snack.value = "Logged in as $userId"
        }

    fun refreshToken() = viewModelScope.launch {
        sessionStore.refreshToken()
        _snack.value = "Token refreshed"
    }

    fun addRole(role: String) = viewModelScope.launch {
        sessionStore.addRole(role)
        _snack.value = "Role '$role' added"
    }

    fun logout() = viewModelScope.launch {
        sessionStore.logout()
        _snack.value = "Logged out — session cleared atomically"
    }

    // ── Global reset ──────────────────────────────────────────────────────────

    fun resetEverything() = viewModelScope.launch {
        appSettingsStore.clearAll()
        userProfileStore.clearProfile()
        sessionStore.logout()
        _snack.value = "All DataStores reset to defaults"
    }

    fun clearSnack() {
        _snack.update { "" }
    }
}
