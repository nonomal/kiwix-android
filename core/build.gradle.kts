import plugin.KiwixConfigurationPlugin

buildscript {
  repositories {
    google()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
  }

  dependencies {
    classpath(Libs.objectbox_gradle_plugin)
  }
}
plugins {
  `android-library`
}
plugins.apply(KiwixConfigurationPlugin::class)
apply(plugin = "io.objectbox")

android {
  defaultConfig {
    buildConfigField("long", "VERSION_CODE", "".getVersionCode().toString())
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
}

fun shouldUseLocalVersion() = File(projectDir, "libs").exists()

dependencies {
  // use jdk8 java.time backport, as long app < Build.VERSION_CODES.O
  implementation(Libs.threetenabp)

  // Get kiwixlib online if it is not populated locally
  if (!shouldUseLocalVersion()) {
    api(Libs.libkiwix)
  } else {
    implementation("com.getkeepsafe.relinker:relinker:1.4.5")
    api(fileTree(mapOf("include" to "*.aar", "dir" to "libs")))
  }

  // Document File
  implementation(Libs.select_folder_document_file)

  // Square
  implementation(Libs.converter_simplexml) {
    exclude(group = "xpp3", module = "xpp3")
    exclude(group = "stax", module = "stax-api")
    exclude(group = "stax", module = "stax")
  }

  // Leak canary
  debugImplementation(Libs.leakcanary_android)

  implementation(Libs.android_arch_lifecycle_extensions)
  implementation(Libs.objectbox_kotlin)
  implementation(Libs.objectbox_rxjava)
  implementation(Libs.webkit)
  testImplementation(Libs.kotlinx_coroutines_test)
  implementation(Libs.kotlinx_coroutines_android)
  implementation(Libs.kotlinx_coroutines_rx3)
  implementation(Libs.zxing)
}
