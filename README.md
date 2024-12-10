<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Kiwix_logo_v3_glow.png/512px-Kiwix_logo_v3_glow.png" align="right" height='250' />
<a href="https://play.google.com/store/apps/details?id=org.kiwix.kiwixmobile" target="_blank" align="left">
  <img src="https://play.google.com/intl/en/badges/images/badge_new.png" alt="Get it on Google Play" height="30" />
</a>
<a href="https://apt.izzysoft.de/fdroid/index/apk/org.kiwix.kiwixmobile" target="_blank" align="left">
  <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid2.png" alt="Get it on IzzyOnDroid" height="29" />
</a>

# Kiwix Android

Kiwix is an offline reader for Web content. One of its main purposes
is to make [Wikipedia](https://www.wikipedia.org/) available
offline. This is achieved by reading the content of a file in the
[ZIM](https://openzim.org) format, a highly compressed open format
with additional metadata.

This is the version for Android, with [support versions ranging from 7.1
to 13](https://github.com/kiwix/kiwix-android/blob/main/buildSrc/src/main/kotlin/Config.kt).

**Important Note**: Starting from Android 11, the ZIM file picker
feature has been restricted in the [Play Store
variant](https://play.google.com/store/apps/details?id=org.kiwix.kiwixmobile)
due to Play Store policies. This means that users running Android 11
and above will not be able to load ZIM files from internal/external
storage directly within the app if they have downloaded Kiwix from the
Google Play Store. This restriction is in place to comply with the
Play Store policies. The Play Store variant of Kiwix does not require
the `MANAGE_EXTERNAL_STORAGE` permission anymore, which is necessary
to scan storage and access ZIM files at arbitrary locations.
Therefore, the storage scanning & file picking functionalities are not
available in this variant anymore. For already downloaded ZIM files, You can copy
them to the `Android/media/org.kiwix.kiwixmobile/` folder, and the application will read them.
Before uninstalling the application, please ensure that you move all your ZIM files
from this folder, as they will be automatically deleted when the application is uninstalled
or if the application data is cleared.  To use the full version of Kiwix
and benefit of the ZIM file picker feature, you can download it
directly from the [official
repository](https://download.kiwix.org/release/kiwix-android/) or use
[IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/org.kiwix.kiwixmobile). We understand that this
restriction may cause inconvenience, but it is necessary to comply
with the Play Store policies and ensure a smooth user experience.  We
recommend using the official version of the app available on our
website to access the complete set of features.

Possible paths for play store version which supports for the scanning/reading ZIM files.

| Storage path                                              | Viewable outside kiwix(in File manager) | Could be scanned by Kiwix |
|-----------------------------------------------------------|-----------------------------------------|---------------------------|
| storage/0/Android/media/org.kiwix.kiwixmobile/            | Yes                                     | Yes                       |
| storage/0/Android/data/org.kiwix.kiwixmobile/             | No                                      | Yes                       |
| storage/sdcard-name/Android/media/org.kiwix.kiwixmobile/  | Yes                                     | Yes                       |
| storage/sdcard-name/Android/data/org.kiwix.kiwixmobile/   | No                                      | Yes                       |

Kiwix Android is written in [Kotlin](https://kotlinlang.org/)

[![Build Status](https://github.com/kiwix/kiwix-android/workflows/CI/badge.svg?query=branch%3Amain+workflow%3ANightly)](https://github.com/kiwix/kiwix-android/actions?query=workflow%3ACI+branch%3Amain)
[![Nightly](https://github.com/kiwix/kiwix-android/actions/workflows/nightly.yml/badge.svg)](https://github.com/kiwix/kiwix-android/actions/workflows/nightly.yml)
[![codecov](https://codecov.io/gh/kiwix/kiwix-android/branch/main/graph/badge.svg)](https://codecov.io/gh/kiwix/kiwix-android)
[![CodeFactor](https://www.codefactor.io/repository/github/kiwix/kiwix-android/badge)](https://www.codefactor.io/repository/github/kiwix/kiwix-android)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Public Chat](https://img.shields.io/badge/public-chat-green)](https://chat.kiwix.org)
[![Slack](https://img.shields.io/badge/Slack-chat-E01E5A)](https://kiwixoffline.slack.com)

## Build instructions

To build Kiwix Android, clone [this
repository](https://github.com/kiwix/kiwix-android) and import (not
open) the project with [Android
Studio](https://developer.android.com/studio).

If you prefer to build without Android Studio you must first set up
the Android SDK and then run the command: `./gradlew build ` from the
root directory of the project. The project requires `Java 17` to run,
Therefore set the `Gradle JDK` to `Java 17`.

Kiwix Android is a multi-module project, in 99% of scenarios you will
want to build the `app` module in the `debug` configuration. If you
are interested in our custom apps, they have their own repo
[kiwix-android-custom](https://github.com/kiwix/kiwix-android-custom).

## Libraries Used

- [Libkiwix](https://github.com/kiwix/java-libkiwix) - Kotlin/Java binding for the core Kiwix
  library
- [Dagger 2](https://github.com/google/dagger) - A fast dependency injector for Android and Java
- [Retrofit](https://square.github.io/retrofit/) - Retrofit turns your REST API into a Java
  interface
- [OkHttp](https://github.com/square/okhttp) - An HTTP+SPDY client for Android and Java applications
- [Butterknife](https://jakewharton.github.io/butterknife/) - View "injection" library for Android
- [Mockito](https://github.com/mockito/mockito) - Most popular Mocking framework for unit tests
  written in Java
- [RxJava](https://github.com/ReactiveX/RxJava) - Reactive Extensions for the JVM – a library for
  composing asynchronous and event-based programs using observable sequences for the Java VM.
- [ObjectBox](https://github.com/objectbox/objectbox-java) - Reactive NoSQL Database
- [MockK](https://github.com/mockk/mockk) - Kotlin mocking library that allows mocking of final
  classes by default.
- [JUnit5](https://github.com/junit-team/junit5/) - The next generation of JUnit
- [AssertJ](https://github.com/joel-costigliola/assertj-core) - Fluent assertions for test code
- [ZXing](https://github.com/zxing/zxing) - Barcode scanning library for Java, Android

## Contributing

Before contributing be sure to check out the
[CONTRIBUTION](https://github.com/kiwix/kiwix-android/blob/main/CONTRIBUTING.md)
guidelines.

We currently have a series of automated Unit & Integration
tests. These can be run locally and are also run when submitting a
pull request.

## Communication

Available communication channels:

* [Email](mailto:contact+android@kiwix.org)
* [Slack](https://kiwixoffline.slack.com): #android
  channel [Get an invite](https://join.slack.com/t/kiwixoffline/shared_invite/zt-19s7tsi68-xlgHdmDr5c6MJ7uFmJuBkg)

For more information, please refer to
[https://wiki.kiwix.org/wiki/Communication](https://wiki.kiwix.org/wiki/Communication).

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0) or later, see
[COPYING](COPYING) for more details.
