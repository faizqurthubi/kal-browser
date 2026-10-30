#!/bin/sh
# Mengompilasi seluruh kode aplikasi dan seluruh test, lalu menjalankannya.
# Dipakai sebagai pengganti Gradle di mesin tanpa Android SDK. Kegagalan di sini
# berarti kode tidak akan lolos di GitHub Actions.
set -e
AKAR=$(cd "$(dirname "$0")/../.." && pwd)
TMP=/tmp/kal-verifikasi
STUB="$AKAR/tools/verifikasi/stub"
CP="$TMP/sdk/android.jar:$(ls "$TMP"/cp/*.jar | tr '\n' ':')"

echo "== kompilasi aplikasi"
rm -rf "$TMP/kelas-app" && mkdir -p "$TMP/kelas-app"
"$TMP/kotlinc/bin/kotlinc" -nowarn -cp "$CP" -d "$TMP/kelas-app" \
  $(find "$AKAR/app/src/main/java" "$STUB" -name "*.kt")

echo "== kompilasi test"
rm -rf "$TMP/kelas-test" && mkdir -p "$TMP/kelas-test"
"$TMP/kotlinc/bin/kotlinc" -nowarn -cp "$TMP/kelas-app:$CP" -d "$TMP/kelas-test" \
  $(find "$AKAR/app/src/test" -name "*.kt")

echo "== jalankan test"
KELAS=$(cd "$TMP/kelas-test" && find . -name "*Test.class" \
  | sed 's|^\./||; s|/|.|g; s|\.class$||' | tr '\n' ' ')
java -cp "$TMP/kelas-test:$TMP/kelas-app:$TMP/kotlinc/lib/kotlin-stdlib.jar:$CP" \
  org.junit.runner.JUnitCore $KELAS
