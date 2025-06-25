plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.publish)
}

group = "io.github.3-bodycoding"
version = "1.0.0"

kotlin {
    jvmToolchain(21)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    js { browser() }
    wasmJs { browser() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

}

android {
    namespace = "com.wzq.singletouchbox"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "singletouchbox", version.toString())

    pom {
        name.set("SingleTouchBox")
        description.set("One-Finger Drag & Drop Rotate & Zoom Component for Compose Multiplatform.")
        inceptionYear.set("2025")
        url.set("https://github.com/3-BodyCoding/single-touch-box/")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("3-BodyCoding")
                name.set("ZhongQian Wang")
                email.set("hxxywzq@hotmail.com")
            }
        }
        scm {
            url.set("https://github.com/3-BodyCoding/single-touch-box/")
            connection.set("scm:git:git://github.com/3-BodyCoding/single-touch-box.git")
            developerConnection.set("scm:git:ssh://git@github.com/3-BodyCoding/single-touch-box.git")
        }
    }
}

