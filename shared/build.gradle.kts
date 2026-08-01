import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kover)
    alias(libs.plugins.mokkery)
}

buildkonfig {
    packageName = "com.blbulyandavbulyan.larm.kmp"
    defaultConfigs {
        val baseUrl = project.findProperty("apiUrl")?.toString() ?: "http://localhost:8080"
        val supabaseUrl = project.findProperty("supabaseUrl")?.toString() ?: ""
        val supabaseAnonKey = project.findProperty("supabaseAnonKey")?.toString() ?: ""

        buildConfigField(FieldSpec.Type.STRING, "API_URL", baseUrl)
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_URL", supabaseUrl)
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_ANON_KEY", supabaseAnonKey)
    }
}

kotlin {

    jvm()

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useMocha {
                        timeout = "30000"
                    }
                }
                filter.excludeTestsMatching("com.blbulyandavbulyan.larm.kmp.ui.*")
                filter.excludeTestsMatching("com.blbulyandavbulyan.larm.kmp.AppTest")
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useMocha {
                        timeout = "30000"
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.turbine)
            implementation(libs.compose.uiTest)
            implementation(libs.ktor.client.mock)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.client.js)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.mp3spi)
        }
    }
}
