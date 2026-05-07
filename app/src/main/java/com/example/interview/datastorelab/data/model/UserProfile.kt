package com.example.interview.datastorelab.data.model

/**
 * Complex nested domain model stored in a Typed DataStore<UserProfile>.
 *
 * Serialization: Gson (via GsonUserProfileSerializer / UserProfileSerializer)
 * Wire format: JSON text  →  user_profile.json on device
 *
 * No @Serializable needed — Gson works with plain Kotlin data classes via reflection.
 * The serializer registers an InstanceCreator<UserProfile> to preserve Kotlin default values.
 *
 * Interview: Why no @Serializable here but we had it before?
 *   @Serializable is a kotlinx.serialization annotation that triggers compile-time code
 *   generation for the serializer. Gson uses runtime reflection — no annotations needed.
 *   Trade-off: kotlinx generates faster code at compile time; Gson is slower (reflection)
 *   but requires zero annotations on the model class.
 */
data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val badges: List<String> = emptyList(),
    val stats: Map<String, Int> = emptyMap(),
    val address: Address = Address(),
    val contentPrefs: ContentPrefs = ContentPrefs()
)

data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "India",
    val pinCode: String = ""
)

data class ContentPrefs(
    val favoriteCategories: List<String> = emptyList(),
    val emailUpdates: Boolean = true,
    val pushNotifications: Boolean = true,
    val weeklyDigest: Boolean = false,
    val topicWeights: Map<String, Float> = emptyMap()
)
