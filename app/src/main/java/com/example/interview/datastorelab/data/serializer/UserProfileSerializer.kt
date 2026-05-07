package com.example.interview.datastorelab.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.interview.datastorelab.data.model.Address
import com.example.interview.datastorelab.data.model.ContentPrefs
import com.example.interview.datastorelab.data.model.UserProfile
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Gson-based Serializer<UserProfile> for Typed DataStore.
 *
 * Interview: Gson vs kotlinx.serialization for Typed DataStore
 *
 * | Aspect                   | kotlinx.serialization       | Gson                        |
 * |--------------------------|-----------------------------|-----------------------------|
 * | Kotlin default values    | ✅ Respected via constructor | ⚠️ Requires InstanceCreator  |
 * | Annotation              | @Serializable required      | No annotation needed         |
 * | Null safety              | ✅ Compile-time            | ⚠️ Runtime null risk         |
 * | Schema evolution         | ignoreUnknownKeys = true    | lenient() mode              |
 * | Code generation          | Optional (slower reflection)| Reflection always           |
 * | Build dependency         | Plugin + runtime            | Just the Gson jar           |
 *
 * ─── The Kotlin Default Value Problem with Gson ───────────────────────────────
 *
 * Gson does NOT call Kotlin constructors. It uses `Unsafe.allocateInstance()` to
 * create an object without calling ANY constructor — bypassing all default parameter
 * values in your data class. This means if a JSON field is missing, that field becomes
 * null in memory (even if the Kotlin type is non-nullable), leading to NPEs at runtime.
 *
 * Solution used here: InstanceCreator<T>
 * We tell Gson: "when you need to create a UserProfile, start with UserProfile()".
 * Gson then sets ONLY the fields present in the JSON over this pre-initialized instance.
 * Fields missing from JSON retain their Kotlin default values. ✅
 *
 * Alternative solutions:
 *  1. Make all fields nullable (breaks domain model cleanliness)
 *  2. Post-process: apply defaults with result.copy(name = result.name ?: "")
 *  3. Custom TypeAdapter per class (most control, most verbose)
 *  4. Use kotlinx.serialization instead (the cleanest Kotlin solution)
 */
object UserProfileSerializer : Serializer<UserProfile> {

    override val defaultValue: UserProfile = UserProfile()

    private val gson = GsonBuilder()
        // InstanceCreator: Gson will call this lambda to allocate a new instance
        // instead of using Unsafe.allocateInstance(). The instance starts with all
        // Kotlin constructor defaults applied — fields present in JSON then overwrite them.
        .registerTypeAdapter(
            UserProfile::class.java,
            InstanceCreator<UserProfile> { UserProfile() }
        )
        .registerTypeAdapter(
            Address::class.java,
            InstanceCreator<Address> { Address() }
        )
        .registerTypeAdapter(
            ContentPrefs::class.java,
            InstanceCreator<ContentPrefs> { ContentPrefs() }
        )
        // serializeNulls: write null fields explicitly to JSON.
        // Without this, Gson silently omits null fields — on read they'd fall back
        // to whatever InstanceCreator provides, which is correct here, but being
        // explicit avoids subtle bugs when InstanceCreator is changed later.
        .serializeNulls()
        // disableHtmlEscaping: prevents < > & = from being Unicode-escaped in the JSON.
        // Matters if user fields contain HTML (bio text, addresses, etc.).
        .disableHtmlEscaping()
        .create()

    override suspend fun readFrom(input: InputStream): UserProfile {
        val json = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return try {
            // fromJson() returns null if the JSON string is "null" — guard with ?: defaultValue.
            gson.fromJson(json, UserProfile::class.java) ?: UserProfile()
        } catch (e: JsonSyntaxException) {
            // Malformed JSON → signal DataStore to replace file with defaultValue.
            throw CorruptionException("Cannot parse UserProfile JSON with Gson", e)
        }
    }

    override suspend fun writeTo(t: UserProfile, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.bufferedWriter(Charsets.UTF_8).use { writer ->
                // toJson(obj, writer) streams JSON directly to the Writer without building
                // an intermediate String in memory — better for large objects.
                gson.toJson(t, writer)
            }
        }
    }
}
