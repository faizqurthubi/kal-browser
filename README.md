# Kal Browser

Browser Android yang membuka situs dalam tata letak desktop. Layar ponsel diperlakukan sebagai layar virtual berukuran tetap, dan sisa ruang di luar kotak halaman dibiarkan hitam, bukan diregangkan.

Semua build berjalan di GitHub Actions. Tidak perlu Android Studio atau komputer.

## Status

Aplikasi masih dibangun bertahap. Yang sudah ada di repo ini:

| Bagian | Status |
| --- | --- |
| Kerangka project, tema, ikon | Selesai |
| Address bar, tombol kembali, maju, muat ulang, hentikan | Selesai |
| Aturan alamat: pencarian, tambah `https://` | Selesai |
| Bilah progres dan keadaan kosong | Selesai |
| Build dan rilis lewat GitHub Actions | Selesai |
| Mode desktop: User-Agent, Client Hints, platform, maxTouchPoints | Selesai |
| Ukuran layar virtual dan meta viewport `width=W` | Selesai |
| Tombol tukar lebar dan tinggi | Selesai |
| Letterbox dan orientasi virtual | Selesai |
| Fullscreen video, unduh berkas, unggah berkas | Selesai |
| Simpan pengaturan terakhir | Belum |

Artinya APK di Release sekarang sudah melaporkan diri sebagai Chrome di Windows, memaksa halaman memakai lebar layar virtual pilihanmu, dan mengurungnya dalam kotak sesuai rasio virtual dengan tepi hitam. Riwayat pekerjaan per tahap ada di `PANDUAN_DesktopBrowser.md` bagian 10.

## Unduh dan pasang

1. Buka halaman [Releases](../../releases).
2. Pada rilis terbaru, unduh berkas `.apk`.
3. Buka berkasnya di ponsel, lalu izinkan pemasangan dari sumber tidak dikenal bila diminta.

Nomor versi mengikuti nama tag. Nomor build diambil dari nomor run GitHub Actions, jadi selalu naik.

### Soal tanda tangan APK

Selama keystore belum dipasang sebagai secret, APK dibangun dengan tanda tangan debug. Runner GitHub membuat keystore debug baru di setiap build, jadi tanda tangannya berubah dan APK baru tidak bisa dipasang menimpa APK lama. Hapus aplikasi lama dulu, atau siapkan keystore tetap dengan cara di bawah.

## Membuat rilis baru dari ponsel

1. Buka repo ini di browser ponsel.
2. Masuk ke tab **Releases**, lalu **Draft a new release**.
3. Isi tag baru, misalnya `v1.0.1`, pilih **Create new tag on publish**.
4. Tekan **Publish release**.

Workflow `Rilis APK` langsung berjalan, membangun APK, dan menempelkannya ke rilis yang baru dibuat. Selesai dalam beberapa menit. Muat ulang halaman rilis untuk melihat berkasnya.

Menekan tombol **Run workflow** pada workflow `Rilis APK` hanya membangun APK untuk diuji. Hasilnya muncul di bagian Artifacts pada halaman run, bukan sebagai Release.

## Keystore tetap, supaya update tidak perlu hapus aplikasi

Bagian ini opsional. Tanpa keystore, aplikasi tetap bisa dipakai, hanya perlu dihapus dulu setiap kali memasang versi baru.

Cara membuatnya dari ponsel memakai Termux:

```bash
pkg install openjdk-17
keytool -genkeypair -v -keystore kal-browser.jks -alias kalbrowser \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 kal-browser.jks > kal-browser.jks.txt
cat kal-browser.jks.txt
```

Baris terakhir menghasilkan teks base64 yang panjang. Salin seluruh teks itu.

Lalu buka **Settings**, **Secrets and variables**, **Actions**, dan tambahkan empat secret:

| Nama secret | Isi |
| --- | --- |
| `KEYSTORE_BASE64` | Teks base64 dari langkah di atas |
| `KEYSTORE_PASSWORD` | Kata sandi keystore yang kamu isi saat `keytool` |
| `KEY_ALIAS` | `kalbrowser` |
| `KEY_PASSWORD` | Kata sandi kunci, biasanya sama dengan kata sandi keystore |

Setelah keempat secret ada, workflow otomatis memakai `assembleRelease` dan menandatangani APK dengan keystore itu.

Simpan berkas `kal-browser.jks` di tempat aman di luar ponsel. Kalau berkas itu hilang, update tidak bisa lagi menimpa versi yang sudah terpasang, dan pengguna harus menghapus aplikasi lebih dulu.

## Build tanpa rilis

Workflow `Build APK` berjalan pada setiap push ke branch mana pun dan pada tombol **Run workflow**. Isinya menjalankan unit test, membangun APK debug, lalu mengunggahnya sebagai artifact.

## Memeriksa lebar virtual

Kalau sebuah situs masih tampil dengan tata letak mobile, ada dua cara memeriksanya.

Cara pertama, tempel `kal:info` ke kolom alamat. Muncul kotak berisi angka yang benar-benar dilihat
halaman. Yang perlu dibaca:

- `lebar halaman` harus sama dengan ukuran yang dipilih di dropdown. Kalau angkanya selebar layar
  ponsel, berarti ada yang menolak lebar virtual.
- `beda` adalah selisih antara lebar dokumen dan lebar halaman. Kalau angkanya 0, seluruh halaman pas
  di layar dan tidak bisa digeser ke samping. Kalau lebih dari 0, ada bagian yang lebih lebar dari
  layar, dan baris `elemen terlebar` menyebut elemen mana.
- `geser horizontal` menunjukkan seberapa jauh halaman sudah digeser. Halaman yang tidak bisa digeser
  ke samping selalu menampilkan 0.

Cara kedua, tempel alamat berikut untuk halaman uji kecil:


Untuk memastikan halaman benar-benar melihat lebar layar virtual, tempel alamat berikut ke kolom
alamat. Halaman kecil ini menampilkan lebar, tinggi, dan skala yang dilihat situs.

```
data:text/html,<h1 id=o></h1><script>document.getElementById('o').textContent='lebar '+innerWidth+' tinggi '+innerHeight+' skala '+visualViewport.scale</script>
```

Angka `lebar` harus sama dengan ukuran yang dipilih di dropdown. Kalau angkanya selebar layar ponsel
(misalnya 393), berarti ada situs atau WebView yang menolak meta viewport.

## Unduh dan unggah

Berkas yang diunduh dari situs diserahkan ke pengunduh sistem, jadi muncul di aplikasi Unduhan dan
tetap berjalan walau aplikasi ditutup. Berkas disimpan di folder `Download` HP. Situs yang berkasnya
butuh sesi tetap bisa diunduh karena cookie ikut dikirim.

Menekan tombol unggah di situs membuka pemilih berkas Android. Kalau situs membatasi jenis berkas,
pemilihnya hanya menampilkan jenis itu. Beberapa situs mengizinkan lebih dari satu berkas sekaligus,
dan itu juga didukung.

Video bisa diputar penuh dengan tombol fullscreen di pemutarnya. Bilah status dan toolbar ikut
tersembunyi, dan tombol kembali menutup video lebih dulu baru mundur ke halaman sebelumnya.

## Kenapa ukuran besar memakai jalur berbeda

Chromium menolak mengecilkan halaman di bawah 25%. Pada layar 393 dp, lebar virtual 1920 px butuh
skala 20.5%, dan skala itu tidak pernah tercapai. Akibatnya halaman tersangkut di 25%, hanya sekitar
1571 px yang terlihat, dan sisa 349 px bisa digeser ke samping.

Untuk ukuran seperti itu, WebView diletakkan seukuran layar virtual lalu diperkecil dengan matriks
View, yang tidak punya batas 25%. Ukuran yang masih sanggup ditangani WebView sendiri tetap memakai
jalur biasa, karena hasil teksnya lebih tajam. Peralihan ini otomatis.

Pada HP dengan layar 393 dp, batasnya jatuh di antara 1366 dan 1440 pada dropdown ukuran.

## Batasan

- Mesin render memakai Android System WebView bawaan ponsel. Perbarui **Android System WebView** atau Chrome dari Play Store supaya situs modern tampil benar.
- Sebagian situs tetap bisa mendeteksi bahwa halaman dibuka di WebView walaupun User-Agent sudah dipalsukan. Itu batas mesin render, bukan kesalahan aplikasi.
- Aplikasi belum menyimpan alamat terakhir dan ukuran layar, jadi pengaturan kembali ke 1280 × 720 setiap aplikasi dibuka. Penyimpanan masuk tahap berikutnya.

## Isi repo

```
app/src/main/java/com/faiz/kalbrowser/
  MainActivity.kt          Activity, WebView, penyambung ke ViewModel
  ScreenPreset.kt          Daftar ukuran layar virtual
  ui/                      State dan ViewModel
  domain/                  Aturan alamat
app/src/main/res/          Layout, tema, ikon
.github/workflows/         build.yml untuk uji, release.yml untuk rilis
DESIGN.md                  Arah desain dan alasannya
PANDUAN_DesktopBrowser.md  Spesifikasi dan urutan tahap
AGENTS.md                  Aturan kerja untuk agen
.agents/                   Filter anti-slop yang dipakai saat membangun
```

## Menjalankan test

Test berjalan otomatis di workflow. Untuk menjalankannya sendiri dibutuhkan JDK 17 dan Gradle 8.7:

```bash
gradle testDebugUnitTest
```
