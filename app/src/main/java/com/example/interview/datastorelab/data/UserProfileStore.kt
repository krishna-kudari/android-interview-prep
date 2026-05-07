package com.example.interview.datastorelab.data

import androidx.datastore.core.DataStore
import com.example.interview.datastorelab.data.model.Address
import com.example.interview.datastorelab.data.model.ContentPrefs
import com.example.interview.datastorelab.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed DataStore<UserProfile> repository for complex nested user profile data.
 *
 * Interview: How does Typed DataStore differ from Preferences DataStore?
 *
 * | Aspect              | Preferences DataStore        | Typed DataStore              |
 * |---------------------|------------------------------|------------------------------|
 * | Storage unit        | Individual keyed primitives  | One serialized blob (file)   |
 * | Schema              | Keys defined at runtime      | Compile-time type (class)    |
 * | Complex objects     | Manual flatten/reconstruct   | Native via Serializer<T>     |
 * | Partial update      | edit { prefs[key] = value }  | updateData { current.copy()} |
 * | Migration           | SharedPreferencesMigration   | Custom DataMigration<T>      |
 * | Corruption recovery | ReplaceFileCorruptionHandler | Same, wraps defaultValue     |
 *
 * Interview: atomic write internals for Typed DataStore:
 *   updateData { transform } reads current T → applies transform → calls Serializer.writeTo()
 *   into a TEMP file → fsync → atomic rename to real file. Either old or new; never partial.
 */
@Singleton
class UserProfileStore @Inject constructor(
    private val dataStore: DataStore<UserProfile>
) {

    // ── Read ──────────────────────────────────────────────────────────────────
    // For Typed DataStore, IOException is already handled by ReplaceFileCorruptionHandler.
    // CorruptionException triggers the handler → defaultValue returned.
    // Any other exception propagates. The .catch here is an extra safety net for callers.
    val userProfileFlow: Flow<UserProfile> = dataStore.data
        .catch { emit(UserProfile()) }

    // Derived flows — transform the full object Flow into smaller, specific Flows.
    // Downstream collectors only recompose when THIS value actually changes,
    // because Compose's collectAsStateWithLifecycle uses distinctUntilChanged internally.
    val displayNameFlow: Flow<String> = userProfileFlow.map { it.displayName }
    val badgesFlow: Flow<List<String>> = userProfileFlow.map { it.badges }
    val badgeCountFlow: Flow<Int> = userProfileFlow.map { it.badges.size }
    val statsFlow: Flow<Map<String, Int>> = userProfileFlow.map { it.stats }
    val addressFlow: Flow<Address> = userProfileFlow.map { it.address }
    val contentPrefsFlow: Flow<ContentPrefs> = userProfileFlow.map { it.contentPrefs }

    // ── Writes ────────────────────────────────────────────────────────────────

    /** Replace the entire profile in one atomic write. */
    suspend fun saveProfile(profile: UserProfile) {
        dataStore.updateData { profile }
    }

    /**
     * Partial update via copy() — only one field changes; others stay intact.
     *
     * Interview: updateData {} follows a read-modify-write pattern:
     *   1. DataStore reads current value (from cache if available, else disk)
     *   2. Runs the lambda: current → new (your transform)
     *   3. Writes the new value atomically
     * The lambda may be retried if concurrent writes conflict — keep it pure/side-effect-free.
     */
    suspend fun updateDisplayName(name: String) {
        dataStore.updateData { current -> current.copy(displayName = name) }
    }

    suspend fun updateEmail(email: String) {
        dataStore.updateData { current -> current.copy(email = email) }
    }

    suspend fun updateBio(bio: String) {
        dataStore.updateData { current -> current.copy(bio = bio) }
    }

    suspend fun updateAddress(address: Address) {
        dataStore.updateData { current -> current.copy(address = address) }
    }

    suspend fun updateContentPrefs(prefs: ContentPrefs) {
        dataStore.updateData { current -> current.copy(contentPrefs = prefs) }
    }

    /**
     * Append a badge — demonstrates read-modify-write on a List field.
     * distinct() prevents duplicates without needing a separate check.
     */
    suspend fun addBadge(badge: String) {
        dataStore.updateData { current ->
            current.copy(badges = (current.badges + badge).distinct())
        }
    }

    suspend fun removeBadge(badge: String) {
        dataStore.updateData { current ->
            current.copy(badges = current.badges - badge)
        }
    }

    /**
     * Increment a stat counter — demonstrates read-modify-write on a Map<String, Int>.
     * Interview: Maps, Lists, nested objects — all handled natively by the typed DataStore.
     */
    suspend fun incrementStat(key: String, by: Int = 1) {
        dataStore.updateData { current ->
            val updatedStats = current.stats.toMutableMap()
            updatedStats[key] = (updatedStats[key] ?: 0) + by
            current.copy(stats = updatedStats)
        }
    }

    suspend fun setStat(key: String, value: Int) {
        dataStore.updateData { current ->
            current.copy(stats = current.stats + (key to value))
        }
    }

    /** Toggle a per-topic content weight. */
    suspend fun setTopicWeight(topic: String, weight: Float) {
        dataStore.updateData { current ->
            val updated = current.contentPrefs.topicWeights.toMutableMap()
            updated[topic] = weight
            current.copy(contentPrefs = current.contentPrefs.copy(topicWeights = updated))
        }
    }

    /** Reset profile to defaultValue. */
    suspend fun clearProfile() {
        dataStore.updateData { UserProfile() }
    }
}
