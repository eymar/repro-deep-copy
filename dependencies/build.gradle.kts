import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

dependencies {
    kotlinCompilerPluginClasspath(project(":repro:plugin-repackaged"))
    kotlinNativeCompilerPluginClasspath(project(":repro:plugin-repackaged"))
}

kotlin {
//    js(IR) {
//        browser()
//    }
    jvm("desktop")
//    mingwX64()
//    linuxX64()
//    macosArm64()
//    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
                implementation("org.jetbrains.kotlinx:atomicfu:0.20.0")
            }
        }
        val jvmMain by creating {
            dependsOn(commonMain)
        }
        val desktopMain by getting {
            dependsOn(jvmMain)
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0-Beta")
            }
        }
        val jvmTest by creating {}
        val desktopTest by getting {
            dependsOn(jvmTest)
        }
//        val jsNativeMain by creating {
//            dependsOn(commonMain)
//        }
//        val jsMain by getting {
//            dependsOn(jsNativeMain)
//        }
    }
}

tasks.withType<KotlinJsCompile>().forEach {
    it.kotlinOptions.freeCompilerArgs += "-Xklib-enable-signature-clash-checks=false"
}
