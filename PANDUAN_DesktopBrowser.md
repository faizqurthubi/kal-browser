# PANDUAN PROJECT: Desktop Browser (Android WebView) + Auto GitHub Release

> Dokumen ini ditujukan untuk **asisten AI** yang akan mengerjakan project. Baca seluruhnya sebelum menulis kode. Kerjakan sesuai urutan di bagian "Tahapan Kerja". Pemilik project bekerja **dari HP saja** (tanpa Android Studio / PC), jadi semua build harus berjalan di **GitHub Actions** dan APK dirilis otomatis lewat **GitHub Release**.

---

## 1. Tujuan

Aplikasi browser Android sederhana yang membuka URL dengan **tampilan desktop asli**, meskipun layar HP kecil.

Inti fitur: layar HP "dipalsukan" menjadi **layar desktop virtual dengan lebar dan tinggi tertentu**, sehingga website menganggap dirinya dibuka di desktop (bukan mobile). Tampilan **tidak di-stretch**, melainkan **fit to screen palsu** (letterbox: sisi sisa berwarna hitam).

## 2. Fitur Wajib

1. **UI browser sederhana**: address bar, tombol back, forward, reload/stop, progress bar.
2. **Dropdown ukuran layar** (preset lebar × tinggi), minimal:
   - 1024 × 768 (4:3)
   - 1280 × 720 (16:9)
   - 1366 × 768
   - 1440 × 900 (16:10)
   - 1920 × 1080 (16:9)
   - Custom (input lebar & tinggi manual) *(nice to have)*
3. **Tombol rotate** portrait/landscape virtual: menukar W dan H (mis. 1280×720 → 720×1280). Rasio lebar:tinggi ikut berubah, web **tidak** melebar mengikuti layar asli Android.
4. **Letterbox**: kotak WebView memiliki rasio W:H, di-center di dalam container berwarna hitam. Kiri/kanan atau atas/bawah boleh hitam.
5. **Mode desktop sungguhan**:
   - User-Agent desktop (Chrome Windows terbaru).
   - Client Hints / `navigator.userAgentData` dipalsukan sebagai desktop.
   - `navigator.platform`, `maxTouchPoints` dipalsukan.
   - Meta viewport diganti menjadi `width=W` agar media query CSS dan `100vh` memakai ukuran virtual.
6. **Kemampuan browser modern**: JavaScript, DOM storage, cookies (termasuk third-party), HTML5 video, fullscreen video, file download, popup/`window.open`, WebView sistem (Chromium) terbaru.
7. **Ingat pengaturan terakhir** (ukuran, orientasi virtual, URL terakhir) memakai SharedPreferences.

## 3. Fitur Opsional (kerjakan setelah semua wajib selesai)

- Tombol untuk memutar orientasi layar fisik HP (`requestedOrientation`).
- Tab sederhana / bookmark.
- Opsi toggle "Desktop mode" ON/OFF.
- Zoom in/out manual.
- Menu "Buka di browser lain".

---

## 4. Stack & Versi

| Item | Nilai |
| --- | --- |
| Bahasa | Kotlin |
| UI | XML View (bukan Compose, supaya sederhana dan build ringan) |
| minSdk / targetSdk / compileSdk | 24 / 34 / 34 |
| AGP | 8.5.x |
| Kotlin | 1.9.x atau 2.0.x |
| Gradle | 8.7 (diinstal lewat GitHub Actions, **tanpa** gradle wrapper jar agar repo bisa dibuat dari HP) |
| JDK | 17 |
| Dependensi | `androidx.appcompat`, `androidx.constraintlayout`, `com.google.android.material`, `androidx.webkit:webkit:1.11.0` |
| Package name | `com.faiz.desktopbrowser` (ganti bila pemilik minta) |
| Nama app | Desktop Browser |

## 5. Struktur Repo

```
desktop-browser/
├── .github/workflows/release.yml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/faiz/desktopbrowser/
│       │   ├── MainActivity.kt
│       │   ├── ScreenPreset.kt
│       │   └── DesktopSpoof.kt
│       └── res/
│           ├── layout/activity_main.xml
│           ├── values/strings.xml
│           ├── values/themes.xml
│           └── mipmap-*/ (ikon; boleh pakai adaptive icon sederhana)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 6. Desain Teknis Inti

### 6.1 Layout (activity_main.xml)

Vertikal:

1. **Toolbar atas**: `[◀] [▶] [⟳] [ address bar ........ ] [Go]`
2. **Baris kontrol**: `Spinner ukuran layar` + `ImageButton rotate` + label ukuran aktif (mis. "1280×720").
3. `ProgressBar` horizontal tipis.
4. **Container hitam** (`FrameLayout`, id `viewport_container`, background `#000000`, `layout_weight=1`) berisi `WebView`.

### 6.2 Model ukuran

```kotlin
data class ScreenPreset(val label: String, val w: Int, val h: Int)

val PRESETS = listOf(
    ScreenPreset("1024 × 768", 1024, 768),
    ScreenPreset("1280 × 720", 1280, 720),
    ScreenPreset("1366 × 768", 1366, 768),
    ScreenPreset("1440 × 900", 1440, 900),
    ScreenPreset("1920 × 1080", 1920, 1080),
)
```

State: `currentPreset`, `isPortrait` (Boolean). Ukuran virtual efektif: `vw = if (isPortrait) preset.h else preset.w`, `vh = if (isPortrait) preset.w else preset.h`.

### 6.3 Letterbox (fit to screen palsu)

Hitung ulang **setiap kali**: preset berubah, rotate ditekan, container berubah ukuran (pakai `viewport_container.post {}` dan `OnLayoutChangeListener`), atau rotasi fisik HP.

```kotlin
private fun applyVirtualScreen() {
    val cw = container.width.toFloat()
    val ch = container.height.toFloat()
    if (cw == 0f || ch == 0f) return

    val (vw, vh) = effectiveSize()           // ukuran virtual dalam "CSS px"
    val scale = minOf(cw / vw, ch / vh)

    webView.layoutParams = FrameLayout.LayoutParams(
        (vw * scale).toInt(),
        (vh * scale).toInt(),
        Gravity.CENTER
    )
    webView.requestLayout()
    injectViewport(vw)                        // lihat 6.4
}
```

**Penting (verifikasi di perangkat):** dengan `useWideViewPort = true` + `loadWithOverviewMode = true` + meta viewport `width=vw`, WebView otomatis menskalakan layout selebar `vw` agar muat di lebar view. Jadi **jangan** memanggil `setInitialScale` kecuali metode utama gagal.

**Fallback bila skala tidak pas:** buat WebView berukuran fisik `vw × vh` px, lalu skalakan dengan transformasi:

```kotlin
webView.layoutParams = FrameLayout.LayoutParams(vw, vh, Gravity.CENTER)
webView.pivotX = vw / 2f; webView.pivotY = vh / 2f
webView.scaleX = scale; webView.scaleY = scale
```

(Pakai fallback hanya jika ada bug rendering/touch; uji koordinat sentuh.)

Hasil yang diharapkan:

- HP portrait + 1280×720 → strip lebar, bar hitam atas & bawah.
- HP portrait + virtual portrait (720×1280) → mengisi tinggi, bar hitam kiri & kanan.

### 6.4 Override viewport

Fungsi `injectViewport(vw)` menjalankan JS yang membuat/mengganti `<meta name="viewport" content="width=VW, initial-scale=1">`. Dipanggil di `onPageStarted` dan `onPageFinished`, plus lewat document-start script (6.5) dengan `MutationObserver` agar tidak ditimpa situs.

```js
(function(){
  var W = __W__;
  function fix(){
    var m = document.querySelector('meta[name=viewport]');
    if(!m){ m = document.createElement('meta'); m.name='viewport';
            (document.head||document.documentElement).appendChild(m); }
    var c = 'width='+W+', initial-scale=1';
    if(m.content !== c) m.content = c;
  }
  fix();
  new MutationObserver(fix).observe(document.documentElement,
     {childList:true, subtree:true, attributes:true, attributeFilter:['content']});
})();
```

Saat ukuran berubah: panggil ulang script lalu `webView.reload()` (agar layout dihitung ulang dari awal).

### 6.5 Pemalsuan perangkat (DesktopSpoof.kt)

1. **User-Agent**:

   ```
   Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36
   ```

   (Gunakan versi Chrome yang masih wajar; sebaiknya ambil nomor versi dari UA WebView asli lalu rakit UA desktop dengan versi yang sama.)
2. **Client Hints** (cek `WebViewFeature.USER_AGENT_METADATA`):

   ```kotlin
   if (WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) {
       val meta = UserAgentMetadata.Builder()
           .setBrandVersionList(listOf(
               UserAgentMetadata.BrandVersion.Builder().setBrand("Chromium").setMajorVersion("130").setFullVersion("130.0.0.0").build(),
               UserAgentMetadata.BrandVersion.Builder().setBrand("Google Chrome").setMajorVersion("130").setFullVersion("130.0.0.0").build()))
           .setPlatform("Windows").setPlatformVersion("10.0.0")
           .setMobile(false).setModel("")
           .setArchitecture("x86").setBitness(64).build()
       WebSettingsCompat.setUserAgentMetadata(webView.settings, meta)
   }
   ```
3. **Document-start JS** (cek `WebViewFeature.DOCUMENT_START_SCRIPT`) memakai `WebViewCompat.addDocumentStartJavaScript(webView, script, setOf("*"))`. Script menimpa:
   - `navigator.platform` → `"Win32"`
   - `navigator.maxTouchPoints` → `0`
   - `navigator.userAgentData.mobile` → `false`
   - `window.ontouchstart` dihapus / `'ontouchstart' in window` dibuat `false` bila memungkinkan
   - Gunakan `Object.defineProperty(Navigator.prototype, ...)`.
   - Gabungkan dengan script viewport 6.4. Karena `W` dinamis, hapus script lama (`ScriptHandler.remove()`) lalu daftarkan ulang saat ukuran berubah.
4. **WebSettings**:

   ```kotlin
   settings.javaScriptEnabled = true
   settings.domStorageEnabled = true
   settings.databaseEnabled = true
   settings.useWideViewPort = true
   settings.loadWithOverviewMode = true
   settings.setSupportZoom(true)
   settings.builtInZoomControls = true
   settings.displayZoomControls = false
   settings.mediaPlaybackRequiresUserGesture = false
   settings.javaScriptCanOpenWindowsAutomatically = true
   settings.setSupportMultipleWindows(false)   // buka popup di WebView yang sama
   settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
   CookieManager.getInstance().setAcceptCookie(true)
   CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
   ```

### 6.6 Perilaku browser

- **Address bar**: bila input berisi spasi atau tidak ada titik → cari lewat `https://www.google.com/search?q=`; bila tanpa skema → tambahkan `https://`.
- **WebViewClient**: `shouldOverrideUrlLoading` tetap memuat di WebView; skema non-http (`intent:`, `mailto:`, `tel:`, `market:`) diteruskan ke `Intent` bila ada handler, jika tidak diabaikan. Update address bar di `onPageStarted`/`doUpdateVisitedHistory`.
- **WebChromeClient**: progress bar, judul, **fullscreen video** (`onShowCustomView` / `onHideCustomView`), `onPermissionRequest` (kamera/mikrofon: minta izin runtime lebih dulu), `onShowFileChooser` untuk input file.
- **Download**: `setDownloadListener` → `DownloadManager`.
- **Back**: `OnBackPressedCallback` → `webView.goBack()` jika bisa, jika tidak keluar.
- **Lifecycle**: `onPause/onResume` memanggil `webView.onPause()/onResume()`; `onDestroy` membersihkan WebView.
- **Konfigurasi**: tambahkan `android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|keyboardHidden"` agar activity tidak recreate saat HP diputar (hitung ulang letterbox di `onConfigurationChanged`/layout listener).
- **Manifest**: izin `INTERNET`, `ACCESS_NETWORK_STATE`; `android:usesCleartextTraffic="true"` (agar situs http tetap bisa dibuka); `android:hardwareAccelerated="true"`.

---

## 7. Build Gradle (ringkas)

`app/build.gradle.kts` harus:

- `namespace = "com.faiz.desktopbrowser"`, `applicationId` sama.
- `versionCode` dan `versionName` bisa di-override dari environment (`VERSION_CODE`, `VERSION_NAME`) yang diisi workflow dari tag, contoh:

  ```kotlin
  versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
  versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
  ```
- `signingConfigs.release` membaca dari environment variable, hanya bila tersedia:

  ```kotlin
  signingConfigs {
      create("release") {
          val ks = System.getenv("KEYSTORE_PATH")
          if (ks != null && file(ks).exists()) {
              storeFile = file(ks)
              storePassword = System.getenv("KEYSTORE_PASSWORD")
              keyAlias = System.getenv("KEY_ALIAS")
              keyPassword = System.getenv("KEY_PASSWORD")
          }
      }
  }
  buildTypes {
      release {
          isMinifyEnabled = false          // jaga sederhana
          signingConfig = signingConfigs.getByName("release")
      }
  }
  ```
- Dependensi sesuai tabel stack. Tidak perlu gradle wrapper (workflow memakai `gradle/actions/setup-gradle` dengan `gradle-version`).

---

## 8. GitHub Actions: Auto Release

Buat `.github/workflows/release.yml`. **Pemicu**: push tag berformat `v*` (mis. `v1.0.0`). Hasil: APK ter-sign terlampir di GitHub Release.

```yaml
name: Build & Release APK

on:
  push:
    tags: ['v*']
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.7'

      - name: Tentukan versi dari tag
        run: |
          TAG="${GITHUB_REF_NAME:-v0.0.0}"
          echo "VERSION_NAME=${TAG#v}" >> $GITHUB_ENV
          echo "VERSION_CODE=${GITHUB_RUN_NUMBER}" >> $GITHUB_ENV

      - name: Siapkan keystore (jika secret tersedia)
        env:
          KEYSTORE_B64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          if [ -n "$KEYSTORE_B64" ]; then
            echo "$KEYSTORE_B64" | base64 -d > "$RUNNER_TEMP/release.jks"
            echo "KEYSTORE_PATH=$RUNNER_TEMP/release.jks" >> $GITHUB_ENV
            echo "SIGNED=true" >> $GITHUB_ENV
          else
            echo "SIGNED=false" >> $GITHUB_ENV
          fi

      - name: Build APK
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          if [ "$SIGNED" = "true" ]; then
            gradle assembleRelease --no-daemon
          else
            gradle assembleDebug --no-daemon
          fi

      - name: Rapikan nama file APK
        run: |
          mkdir -p out
          if [ "$SIGNED" = "true" ]; then
            cp app/build/outputs/apk/release/*.apk "out/DesktopBrowser-${VERSION_NAME}.apk"
          else
            cp app/build/outputs/apk/debug/*.apk "out/DesktopBrowser-${VERSION_NAME}-debug.apk"
          fi

      - name: Buat GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: out/*.apk
          generate_release_notes: true
```

**Catatan untuk AI:**

- Bila secret keystore belum ada, workflow tetap harus sukses dan menghasilkan APK **debug** (agar pemilik bisa langsung mencoba).
- Gunakan `workflow_dispatch` hanya untuk uji build; rilis resmi lewat tag.
- Pastikan `.gitignore` memuat `build/`, `.gradle/`, `local.properties`, `*.jks`, `*.keystore`.

## 9. Panduan untuk Pemilik (taruh di README.md)

Tulis README singkat berbahasa Indonesia yang menjelaskan, **semuanya bisa dilakukan dari HP**:

1. **Cara rilis**: buat tag dari halaman GitHub → *Releases* → *Draft a new release* → isi tag baru `v1.0.0` → *Publish*. Workflow berjalan otomatis dan APK muncul di halaman Release.
2. **Cara install**: buka halaman Release di HP, unduh APK, izinkan "install dari sumber tidak dikenal".
3. **(Opsional) Signing tetap** agar update bisa menimpa versi lama tanpa uninstall: buat keystore sekali (bisa lewat workflow `workflow_dispatch` terpisah yang menjalankan `keytool` dan mencetak base64, atau lewat Termux), lalu simpan di *Settings → Secrets and variables → Actions*: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. **Simpan cadangan keystore**: bila hilang, update tidak bisa menimpa versi lama.
4. APK debug (tanpa secret) memakai tanda tangan debug yang berubah-ubah per runner. Jelaskan bahwa ini hanya untuk uji coba.

---

## 10. Tahapan Kerja (urutan wajib)

1. **Scaffold project**: struktur repo, Gradle, manifest, tema, ikon, `.gitignore`. Pastikan build lokal-logis benar (tidak ada referensi resource yang hilang).
2. **WebView dasar + address bar + navigasi** (6.6).
3. **Mode desktop**: UA, WebSettings, Client Hints, document-start script (6.5).
4. **Dropdown ukuran + override viewport** (6.2, 6.4).
5. **Letterbox + rotate** (6.3).
6. **Fullscreen video, download, file chooser, permission, back handling**.
7. **SharedPreferences** untuk menyimpan pengaturan.
8. **GitHub Actions release** (bagian 8) dan README (bagian 9).
9. Review akhir dengan checklist bagian 11.

Setelah tiap tahap, ringkas apa yang selesai dan file mana yang berubah. Jangan lanjut menambah fitur opsional sebelum semua fitur wajib lulus checklist.

## 11. Checklist Penerimaan

- [ ] Workflow berjalan sukses pada push tag `v*` dan melampirkan APK ke Release.
- [ ] Aplikasi terbuka, address bar memuat URL dan pencarian.
- [ ] Situs yang punya layout mobile/desktop berbeda (uji: `https://www.whatismybrowser.com`, `https://responsively.app`, YouTube, Wikipedia, GitHub) menampilkan **layout desktop**.
- [ ] Situs pendeteksi UA melaporkan Windows + Chrome, bukan Android/mobile; `navigator.userAgentData.mobile === false`.
- [ ] Mengganti ukuran di dropdown mengubah lebar CSS yang dilihat halaman (cek dengan `window.innerWidth` ≈ lebar virtual).
- [ ] Tombol rotate menukar W/H; rasio kotak berubah; **tidak ada stretch**; sisa area hitam.
- [ ] `100vh` di halaman sesuai tinggi virtual (tidak terpotong / tidak terlalu tinggi).
- [ ] Sentuhan/klik akurat (koordinat tap sesuai elemen yang disentuh) pada semua ukuran.
- [ ] Video HTML5 bisa diputar dan fullscreen berfungsi.
- [ ] Download file dan upload file berfungsi.
- [ ] Back/forward/reload dan tombol back sistem berfungsi.
- [ ] Memutar HP (fisik) tidak merusak letterbox dan tidak mereset halaman.
- [ ] Pengaturan ukuran dan orientasi virtual tetap tersimpan setelah app ditutup.
- [ ] Tidak ada crash saat WebView feature tidak didukung (semua `WebViewFeature` dicek dulu).

## 12. Catatan & Batasan Penting

- Mesin render = WebView sistem Android (Chromium). Kemampuan HTML/CSS/JS mengikuti versi WebView terpasang; sarankan pemilik memperbarui **Android System WebView** / Chrome dari Play Store.
- Sebagian situs sangat ketat dan masih bisa mendeteksi WebView atau mobile lewat sinyal lain; itu bukan bug aplikasi.
- Jangan menyimpan password/keystore di repo. Semua rahasia lewat GitHub Secrets.
- Jaga kode tetap sederhana dan terkomentari (bahasa Indonesia untuk komentar), karena pemilik sedang belajar coding sambil membangun.