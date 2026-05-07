package com.example.interview.datastorelab.data

import android.os.Build
import androidx.datastore.core.DataStore
import com.example.interview.datastorelab.data.model.DeviceInfo
import com.example.interview.datastorelab.data.model.SessionData
import com.example.interview.datastorelab.data.model.toDomain
import com.example.interview.datastorelab.data.model.toProto
import com.example.interview.datastorelab.proto.SessionProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proto DataStore repository for auth session.
 *
 * Key change from the JSON version:
 *   Before: DataStore<SessionData>  (kotlinx.serialization JSON)
 *   Now:    DataStore<SessionProto> (protobuf binary)
 *
 * The store internally works with SessionProto (the generated Java class).
 * All flows exposed to the ViewModel are mapped to SessionData (Kotlin domain class).
 * This keeps proto concerns inside the data layer — ViewModel/UI never see SessionProto.
 *
 * Interview: Proto DataStore write internals
 *   updateData { currentProto -> newProto }
 *     1. DataStore reads current SessionProto from disk (or cache)
 *     2. Your lambda returns the new SessionProto (via proto builder pattern)
 *     3. SessionProtoSerializer.writeTo() encodes it as binary TLV to a TEMP file
 *     4. Atomic rename: TEMP → session.pb
 *   If the process dies in step 3, step 4 never happens → old proto remains intact.
 *
 * Interview: Proto builder pattern (immutable messages)
 *   Proto messages are immutable. To "modify" one:
 *     currentProto.toBuilder()        // creates a mutable Builder copy
 *       .setAuthToken("new_token")    // mutate fields
 *       .build()                      // produce a new immutable SessionProto
 *   Contrast with Kotlin: current.copy(authToken = "new_token")
 */
@Singleton
class SessionStore @Inject constructor(
    private val dataStore: DataStore<SessionProto>
) {

    // ── Flows ─────────────────────────────────────────────────────────────────
    // Map SessionProto → SessionData for every emission.
    // The mapping is cheap (pure field copying) and called lazily on collection.

    val sessionFlow: Flow<SessionData> = dataStore.data
        .catch { emit(SessionProto.getDefaultInstance()) }
        .map { proto -> proto.toDomain() }

    val isLoggedInFlow: Flow<Boolean> = dataStore.data
        .catch { emit(SessionProto.getDefaultInstance()) }
        .map { it.isLoggedIn }

    val isTokenExpiredFlow: Flow<Boolean> = sessionFlow.map { it.isTokenExpired }
    val userIdFlow: Flow<String> = dataStore.data.catch { emit(SessionProto.getDefaultInstance()) }.map { it.userId }
    val rolesFlow: Flow<List<String>> = dataStore.data.catch { emit(SessionProto.getDefaultInstance()) }.map { it.rolesList }

    // ── Writes ────────────────────────────────────────────────────────────────

    /**
     * Login: build a complete SessionProto from scratch.
     *
     * Proto Builder pattern:
     *   SessionProto.newBuilder()         ← static factory, creates empty Builder
     *     .setIsLoggedIn(true)            ← fluent setter for each field
     *     .addRoles("USER")               ← addX() for repeated fields
     *     .setDeviceInfo(DeviceInfo(...)) ← nested message
     *     .build()                        ← produces immutable SessionProto
     *
     * Interview: Note addRoles() vs setRoles(). Proto repeated fields use:
     *   - addX(value)    — append one element
     *   - addAllX(list)  — append all elements from a collection
     *   - clearX()       — remove all elements
     */
    suspend fun login(userId: String, roles: List<String> = listOf("USER")) {
        dataStore.updateData {
            val deviceInfoProto = DeviceInfo(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                sdkVersion = Build.VERSION.SDK_INT,
                appVersion = "1.0"
            ).toProto()

            SessionProto.newBuilder()
                .setIsLoggedIn(true)
                .setUserId(userId)
                .setAuthToken(generateToken("tok"))
                .setRefreshToken(generateToken("ref"))
                .setExpiresAtMs(System.currentTimeMillis() + 60 * 60 * 1000L)
                .addAllRoles(roles)
                .setDeviceId(UUID.randomUUID().toString().take(12))
                .setLoginTimestampMs(System.currentTimeMillis())
                .setDeviceInfo(deviceInfoProto)
                .build()
        }
    }

    /**
     * Token refresh — partial update via proto builder's toBuilder().
     *
     * Proto toBuilder(): copies all fields from the existing message into a new Builder.
     * You then overwrite only the fields you care about. Identical to Kotlin's copy().
     *
     * Interview: this is the proto equivalent of:
     *   current.copy(authToken = newToken, expiresAtMs = newExpiry)
     */
    suspend fun refreshToken() {
        dataStore.updateData { currentProto ->
            if (!currentProto.isLoggedIn) return@updateData currentProto
            currentProto.toBuilder()
                .setAuthToken(generateToken("tok"))
                .setExpiresAtMs(System.currentTimeMillis() + 60 * 60 * 1000L)
                .build()
        }
    }

    /**
     * Add a role — demonstrates modifying a 'repeated' field.
     *
     * clearRoles() + addAllRoles() is the idiomatic way to replace a repeated field.
     * There is no "setRoles(list)" on a builder — you must clear then re-add.
     */
    suspend fun addRole(role: String) {
        dataStore.updateData { currentProto ->
            val updatedRoles = (currentProto.rolesList + role).distinct()
            currentProto.toBuilder()
                .clearRoles()
                .addAllRoles(updatedRoles)
                .build()
        }
    }

    /**
     * Logout: replace with the empty default instance.
     *
     * getDefaultInstance() == all proto3 zero-defaults (false, 0L, "", empty lists).
     * Single atomic write clears all session state — no partial logout possible.
     */
    suspend fun logout() {
        dataStore.updateData { SessionProto.getDefaultInstance() }
    }

    private fun generateToken(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(20)}"
}
