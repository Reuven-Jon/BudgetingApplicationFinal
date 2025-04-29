plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.sample.budgetingapplicationfinal"
    compileSdk = 35

    buildFeatures {
        viewBinding = true    // <— add this
        compose = true

        composeOptions {
            // match this to the Compose version you’ll use below
            kotlinCompilerExtensionVersion = "1.4.8"

            defaultConfig {
                applicationId = "com.sample.budgetingapplicationfinal"
                minSdk = 32
                targetSdk = 35
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            kotlinOptions {
                jvmTarget = "11"
            }

            buildFeatures {
                viewBinding = true    // we’re using XML + ViewBinding
            }
        }

        dependencies {
            // Core
            implementation("androidx.core:core-ktx:1.10.1")
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("androidx.recyclerview:recyclerview:1.3.0")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

            // Material Components & CardView
            implementation("com.google.android.material:material:1.9.0")
            implementation("androidx.cardview:cardview:1.0.0")

            // Testing
            testImplementation("junit:junit:4.13.2")
            androidTestImplementation("androidx.test.ext:junit:1.1.5")
            androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

            // BOM to keep versions in sync
            implementation(platform("androidx.compose:compose-bom:2024.04.00"))

// Core UI & graphics
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.ui:ui-graphics")

// Material3 components
            implementation("androidx.compose.material3:material3")

// Preview tooling
            implementation("androidx.compose.ui:ui-tooling-preview")
            debugImplementation("androidx.compose.ui:ui-tooling")
        }
    }
}