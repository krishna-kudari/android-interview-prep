import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
}

configurations.configureEach {
    resolutionStrategy {
        // androidx.test.ext:junit:1.3.0 needs 1.2.0; some AGP/Compose edges pin 1.1.0 — unify for androidTest.
        force("androidx.concurrent:concurrent-futures:1.2.0")
    }
}

android {
    namespace = "com.example.interview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.interview"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.interview.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "BASE_URL", "\"https://api.coingecko.com/\"")

    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)

    implementation(libs.bundles.compose.core)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.runtime.livedata)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.bundles.lifecycle)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.bundles.networking)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.bundles.coroutines)
    implementation(libs.androidx.paging.runtime)
    testImplementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.protobuf.javalite)

    implementation(libs.timber)

    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
    // Instrumented Compose + Hilt (explicit mirrors common guides / version catalog samples)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// ── Proto DataStore: generate Java Lite classes from .proto files ─────────────
// protoc reads app/src/main/proto/*.proto and generates Java source in build/generated.
// "lite" option produces MessageLite subclasses — smaller binary, no reflection API,
// suitable for Android where binary size matters.
// Interview: Proto Lite vs Full:
//   Full proto → supports reflection, dynamic messages, JSON interop (com.google.protobuf)
//   Lite proto → no reflection, smaller APK (~10× smaller runtime), used on Android
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobufJava.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
