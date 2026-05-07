package com.example.interview.datastorelab.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.interview.datastorelab.data.model.UserProfile
import com.example.interview.datastorelab.data.serializer.SessionProtoSerializer
import com.example.interview.datastorelab.data.serializer.UserProfileSerializer
import com.example.interview.datastorelab.proto.SessionProto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Hilt module that provides all DataStore instances as application-scoped singletons.
 *
 * Interview: Why provide DataStore via Hilt rather than using the `by dataStore()` delegate?
 *
 * The `by dataStore()` top-level delegate creates a DataStore that is keyed to the
 * Context instance passed to it. With DI, Hilt provides the ApplicationContext which
 * is truly a single instance — so the DataStore created here is guaranteed to be a
 * singleton. Mixing both patterns risks creating two DataStore instances for the same
 * file, which causes data races (DataStore explicitly throws IllegalStateException
 * when two instances point to the same file in the same process).
 *
 * Rule: One DataStore per file per process.
 *
 * Factory methods used:
 *  - PreferenceDataStoreFactory.create() — for Preferences DataStore (primitives)
 *  - DataStoreFactory.create()           — for Typed DataStore (serialized objects)
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    // ── 1. Preferences DataStore ──────────────────────────────────────────────
    // File: /data/data/<pkg>/files/datastore/app_settings.preferences_pb
    //
    // corruptionHandler: if the preferences file is corrupt (e.g., truncated write
    // before DataStore existed), replace with emptyPreferences() — all defaults apply.
    //
    // migrations: SharedPreferencesMigration copies keys from the legacy SharedPreferences
    // file on the FIRST time this DataStore is opened. After migration it's a no-op.
    // The legacy SharedPreferences file is deleted once migration succeeds.
    //
    // Interview: migration runs synchronously on first DataStore access.
    // If the process dies mid-migration, it re-runs on next access (idempotent).
    @Provides
    @Singleton
    fun provideAppSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() }
        ),
        migrations = listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "legacy_app_settings"
            )
        ),
        produceFile = { context.preferencesDataStoreFile("app_settings") }
    )

    // ── 2. Typed DataStore — UserProfile ──────────────────────────────────────
    // File: /data/data/<pkg>/files/datastore/user_profile.json
    //
    // corruptionHandler: if UserProfileSerializer.readFrom() throws CorruptionException,
    // the handler is invoked: it returns UserProfile() (all defaults) and DataStore
    // overwrites the corrupt file with the default value. The user loses their profile
    // data but the app doesn't crash permanently — graceful degradation.
    //
    // Interview: Without corruptionHandler, a corrupt file would throw an exception on
    // every read forever, making the DataStore permanently unusable.
    @Provides
    @Singleton
    fun provideUserProfileDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserProfile> = DataStoreFactory.create(
        serializer = UserProfileSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { UserProfile() }
        ),
        produceFile = { File(context.filesDir, "datastore/user_profile.json") }
    )

    // ── 3. Proto DataStore — Session ──────────────────────────────────────────
    // File: /data/data/<pkg>/files/datastore/session.pb  (binary protobuf)
    //
    // Changed from JSON (SessionSerializer) → Proto (SessionProtoSerializer).
    // The file format changes from UTF-8 JSON text to binary TLV (tag-length-value).
    //
    // Why proto here vs Gson for UserProfile?
    //  - Session data is security-sensitive: smaller, opaque binary is harder to tamper
    //    with on a rooted device compared to human-readable JSON
    //  - Auth tokens, roles, expiry timestamps benefit from protobuf's compact encoding
    //  - Proto schema (session.proto) serves as the canonical contract for the data shape
    //  - Field numbers in proto guarantee safe schema evolution (add field 9, 10... safely)
    //
    // corruptionHandler: if the binary is corrupt (e.g., partial write), SessionProto
    // .getDefaultInstance() is returned → all zero-defaults → user is effectively logged out.
    // This is the correct fail-safe for auth state: corrupt = treat as logged out.
    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context
    ): DataStore<SessionProto> = DataStoreFactory.create(
        serializer = SessionProtoSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { SessionProto.getDefaultInstance() }
        ),
        produceFile = { File(context.filesDir, "datastore/session.pb") }
    )
}
