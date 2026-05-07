package com.example.interview.datastorelab.data.model

import com.example.interview.datastorelab.proto.SessionProto

/**
 * Domain model for auth session.
 *
 * Source of truth on disk: session.proto binary (SessionProto message)
 * This class is the Kotlin-friendly representation used above the data layer.
 *
 * Pattern: Proto in data layer → domain class in presentation layer
 *
 * Interview: Why keep a separate domain class instead of using SessionProto directly in UI?
 *
 *  1. Proto-generated classes are Java, not idiomatic Kotlin:
 *       - No copy(), componentN(), destructuring
 *       - Builder pattern for writes (sessionProto.toBuilder().setUserId(...).build())
 *       - Not null-safe in Kotlin (all string fields return "" but compiler doesn't know)
 *
 *  2. Decoupling: if you swap proto for Room or a remote source later,
 *     the ViewModel/UI code is unchanged — only the mapping layer changes.
 *
 *  3. Computed properties: isTokenExpired, remainingSessionMinutes are NOT stored on disk.
 *     They live only in this domain class, derived at read time. Proto messages should
 *     store raw data; business logic belongs in the domain layer.
 */
data class SessionData(
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val authToken: String = "",
    val refreshToken: String = "",
    val expiresAtMs: Long = 0L,
    val roles: List<String> = emptyList(),
    val deviceId: String = "",
    val loginTimestampMs: Long = 0L,
    val deviceInfo: DeviceInfo = DeviceInfo()
) {
    val isTokenExpired: Boolean
        get() = isLoggedIn && System.currentTimeMillis() > expiresAtMs

    val remainingSessionMinutes: Long
        get() = if (!isLoggedIn) 0L
        else maxOf(0L, (expiresAtMs - System.currentTimeMillis()) / 60_000)
}

/**
 * Nested domain model matching the DeviceInfo nested message in session.proto.
 * Demonstrates proto nested messages ↔ Kotlin nested data classes.
 */
data class DeviceInfo(
    val manufacturer: String = "",
    val model: String = "",
    val sdkVersion: Int = 0,
    val appVersion: String = ""
)

// ── Proto ↔ Domain mappers (live in the data layer alongside SessionData) ──────

/**
 * SessionProto → SessionData
 * Called inside SessionStore to convert proto binary into domain objects for the UI.
 */
fun SessionProto.toDomain(): SessionData = SessionData(
    isLoggedIn = isLoggedIn,
    userId = userId,
    authToken = authToken,
    refreshToken = refreshToken,
    expiresAtMs = expiresAtMs,
    roles = rolesList,               // proto repeated field → List<String>
    deviceId = deviceId,
    loginTimestampMs = loginTimestampMs,
    deviceInfo = if (hasDeviceInfo()) deviceInfo.toDomain() else DeviceInfo()
)

fun com.example.interview.datastorelab.proto.DeviceInfo.toDomain(): DeviceInfo = DeviceInfo(
    manufacturer = manufacturer,
    model = model,
    sdkVersion = sdkVersion,
    appVersion = appVersion
)

/**
 * DeviceInfo domain → proto DeviceInfo builder.
 * Called in SessionStore when constructing the proto message to persist.
 */
fun DeviceInfo.toProto(): com.example.interview.datastorelab.proto.DeviceInfo =
    com.example.interview.datastorelab.proto.DeviceInfo.newBuilder()
        .setManufacturer(manufacturer)
        .setModel(model)
        .setSdkVersion(sdkVersion)
        .setAppVersion(appVersion)
        .build()
