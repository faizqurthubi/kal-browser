#!/bin/sh
# Menyiapkan alat verifikasi di /tmp: compiler Kotlin, android.jar, dan jar
# dependensi. Isinya tidak ikut ke APK; ini hanya pengganti Gradle di mesin yang
# tidak punya Android SDK, supaya kode bisa dikompilasi dan diuji sebelum push.
set -e
AKAR=$(cd "$(dirname "$0")/../.." && pwd)
TMP=/tmp/kal-verifikasi
mkdir -p "$TMP/dl" "$TMP/cp" "$TMP/sdk"

if [ ! -x "$TMP/kotlinc/bin/kotlinc" ]; then
  echo "mengunduh compiler Kotlin"
  curl -sL -o "$TMP/dl/kotlin.zip" \
    "https://github.com/JetBrains/kotlin/releases/download/v2.0.21/kotlin-compiler-2.0.21.zip"
  unzip -q -o "$TMP/dl/kotlin.zip" -d "$TMP"
fi

if [ ! -f "$TMP/sdk/android.jar" ]; then
  echo "mengunduh platform Android 34"
  curl -sL -o "$TMP/dl/platform.zip" \
    "https://dl.google.com/android/repository/platform-34-ext12_r01.zip"
  unzip -q -o "$TMP/dl/platform.zip" -d "$TMP/dl"
  cp "$TMP/dl/android-34-ext12/android.jar" "$TMP/sdk/android.jar"
fi

GOOGLE=https://dl.google.com/dl/android/maven2
MAVEN=https://repo1.maven.org/maven2

# AAR: classes.jar diambil dari dalamnya.
for baris in \
  "androidx/core/core/1.13.1" \
  "androidx/core/core-ktx/1.13.1" \
  "androidx/appcompat/appcompat/1.7.0" \
  "androidx/constraintlayout/constraintlayout/2.1.4" \
  "androidx/webkit/webkit/1.11.0" \
  "androidx/activity/activity/1.9.3" \
  "androidx/activity/activity-ktx/1.9.3" \
  "androidx/fragment/fragment/1.8.5" \
  "androidx/savedstate/savedstate/1.2.1" \
  "androidx/lifecycle/lifecycle-viewmodel-ktx/2.8.7" \
  "androidx/lifecycle/lifecycle-viewmodel-android/2.8.7" \
  "androidx/lifecycle/lifecycle-runtime-android/2.8.7" \
  "com/google/android/material/material/1.12.0"; do
  grup=$(dirname "$(dirname "$baris")"); nama=$(basename "$(dirname "$baris")"); versi=$(basename "$baris")
  if [ ! -f "$TMP/cp/$nama.jar" ] || [ "$(stat -c%s "$TMP/cp/$nama.jar")" -lt 2000 ]; then
    curl -sL -o "$TMP/dl/$nama.aar" "$GOOGLE/$grup/$nama/$versi/$nama-$versi.aar"
    unzip -p "$TMP/dl/$nama.aar" classes.jar > "$TMP/cp/$nama.jar" 2>/dev/null || true
  fi
done

# Jar biasa.
for baris in \
  "androidx/lifecycle/lifecycle-common-jvm/2.8.7|$GOOGLE" \
  "androidx/annotation/annotation-jvm/1.8.2|$GOOGLE" \
  "org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.9.0|$MAVEN" \
  "org/jetbrains/kotlinx/kotlinx-coroutines-android/1.9.0|$MAVEN" \
  "org/jetbrains/kotlinx/kotlinx-coroutines-test-jvm/1.9.0|$MAVEN" \
  "com/google/truth/truth/1.4.4|$MAVEN" \
  "com/google/guava/guava/33.0.0-android|$MAVEN" \
  "com/google/guava/failureaccess/1.0.2|$MAVEN" \
  "junit/junit/4.13.2|$MAVEN" \
  "org/hamcrest/hamcrest-core/1.3|$MAVEN"; do
  jalur=${baris%%|*}; dasar=${baris##*|}
  grup=$(dirname "$(dirname "$jalur")"); nama=$(basename "$(dirname "$jalur")"); versi=$(basename "$jalur")
  if [ ! -f "$TMP/cp/$nama.jar" ] || [ "$(stat -c%s "$TMP/cp/$nama.jar")" -lt 2000 ]; then
    curl -sL -o "$TMP/cp/$nama.jar" "$dasar/$grup/$nama/$versi/$nama-$versi.jar"
  fi
done

echo "alat siap di $TMP"
