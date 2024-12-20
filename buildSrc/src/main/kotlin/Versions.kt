import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {

  const val document_file_version: String = "1.0.1"

  const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.9.0"

  const val kotlinx_coroutines_rx3: String = "1.3.9"

  const val androidx_test_espresso: String = "3.5.1"

  const val tracing: String = "1.1.0"

  const val com_squareup_retrofit2: String = "2.11.0"

  const val com_squareup_okhttp3: String = "4.12.0"

  const val org_jetbrains_kotlin: String = "2.0.0"

  const val kotlin_ksp: String = "2.1.0-1.0.29"

  const val androidx_navigation: String = "2.5.3"

  const val navigation_ui_ktx: String = "2.4.1"

  const val com_google_dagger: String = "2.53.1"

  const val androidx_test: String = "1.6.1"

  const val androidx_test_core: String = "1.6.1"

  const val androidx_test_orchestrator: String = "1.5.0"

  const val io_objectbox: String = "4.0.3"

  const val io_mockk: String = "1.13.13"

  const val android_arch_lifecycle_extensions: String = "1.1.1"

  const val com_android_tools_build_gradle: String = "8.4.0"

  const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

  const val com_github_triplet_play_gradle_plugin: String = "3.7.0"

  const val javax_annotation_api: String = "1.3.2"

  const val leakcanary_android: String = "2.13"

  const val constraintlayout: String = "2.1.4"

  const val swipe_refresh_layout: String = "1.1.0"

  const val collection_ktx: String = "1.4.5"

  const val preference_ktx: String = "1.2.1"

  const val junit_jupiter: String = "5.11.0"

  const val assertj_core: String = "3.26.3"

  const val core_testing: String = "2.2.0"

  const val fragment_ktx: String = "1.8.5"

  const val testing_ktx: String = "1.3.0"

  const val threetenabp: String = "1.3.0"

  const val uiautomator: String = "2.3.0"

  const val annotation: String = "1.6.0"

  const val simple_xml: String = "2.7.1"

  const val appcompat: String = "1.7.0"

  const val rxandroid: String = "2.1.1"

  const val core_ktx: String = "1.15.0"

  const val libkiwix: String = "2.2.3"

  const val material: String = "1.8.0"

  const val multidex: String = "2.0.1"

  const val barista: String = "4.3.0"

  const val rxjava: String = "2.2.21"

  const val webkit: String = "1.11.0"

  const val junit: String = "1.1.5"

  const val material_show_case_view: String = "1.3.7"

  const val roomVersion = "2.5.0"

  const val zxing = "3.5.3"

  const val keeper = "0.16.1"

  const val fetch: String = "3.4.1"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
  inline get() =
    id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
