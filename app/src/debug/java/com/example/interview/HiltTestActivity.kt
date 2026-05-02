package com.example.interview

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Debug-only host activity for Compose instrumented tests with Hilt.
 * Declared in [debug/AndroidManifest.xml]; lives under `src/debug` so it ships in the app
 * under test (not only the androidTest APK). See Medium / official Hilt testing guides.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
