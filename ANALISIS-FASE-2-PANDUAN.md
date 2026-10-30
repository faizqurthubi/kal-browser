# Analisis PANDUAN FASE 2

Tanggal: 2026-10-29
Dibandingkan dengan: `v1.10.2`
Status: analisis, belum dikerjakan

## Ringkasan

Dokumen Fase 2 tegas soal batas kewenangannya: ia menang untuk urusan toolbar, dan Fase 1 menang untuk
selebihnya. Saya setuju dengan pembagian itu. Yang perlu dicatat lebih dulu: dokumen ini membalik
**enam** keputusan yang sudah kita ambil dan kerjakan di `v1.10.0` sampai `v1.10.2`, dan **satu**
bagiannya bertabrakan dengan instruksi yang kamu berikan setelah itu. Semuanya bisa dikerjakan, tapi
jangan sampai kamu terkejut seperti waktu bilah status.

## Enam hal yang berbalik

| # | Di `v1.10.2` | Menurut dokumen Fase 2 | Catatan |
|---|---|---|---|
| 1 | Tombol tukar hanya di menu | Tukar **kembali ke bilah**, di baris kedua (tegak) dan baris utama (mendatar) | Ini membatalkan pilihanmu "hanya di menu", dan saya setuju: dengan chip ukuran, tukar jadi satu gerakan |
| 2 | Ukuran aktif hanya di menu | **Chip ukuran selalu terlihat**, misalnya `1920×1080 · mendatar ▾` | Menutup keberatan yang saya sampaikan waktu itu |
| 3 | Bilah menyembunyikan diri saat digulir, **dua orientasi** | Hanya **mendatar**, memakai **timer 3 detik**, bukan gulir | Di tegak bilah selalu terlihat. Mekanisme gulir yang sekarang harus dibuang, kalau tidak dua mekanisme saling berebut |
| 4 | Tombol 48 dp, padding 8, ripple borderless | Tombol **44 dp**, padding 10, ripple **bounded** oval | Bedanya bukan selera: ripple borderless yang sekarang memang tergambar keluar batas |
| 5 | Bilah satu baris 49 dp | Tegak **dua baris, 100 dp** | Menambah 51 dp chrome di tegak. Rinciannya di bawah |
| 6 | Kolom URL selalu menampilkan alamat penuh | Tidak fokus: **domain saja**, gembok dan `https://` bersyarat | Perlu keputusan teknis, lihat bagian risiko nomor 4 |

## Yang bertabrakan dengan instruksimu sendiri

Kamu sebelumnya menegur saya karena menyembunyikan **bilah status** yang berisi jam dan baterai, dan
saya sudah mengembalikannya di `v1.10.1`.

Dokumen Fase 2 bagian 4 menulis, untuk mode mendatar: `Padding atas bilah 0 dp (status bar
disembunyikan)`, dan bagian 7 menyebut zona geser dibuat agar "tidak bentrok dengan gestur status bar
sistem (mode imersif)".

Artinya di mendatar, **bilah status disembunyikan lagi**. Saya tidak akan memutuskan ini sendiri.
Tiga kemungkinan:

| Pilihan | Tinggi bilah mendatar | Akibat |
|---|---|---|
| A. Ikut dokumen: bilah status hilang di mendatar | 46 dp | Tinggi maksimal, jam dan baterai hilang saat mendatar |
| B. Bilah status tetap terlihat, padding atas = tinggi bilah status | 46 + 24 = 70 dp | Jam terlihat, tinggi berkurang 24 dp |
| C. Bilah status tetap, dan bilah alamat menumpang di bawahnya | 46 dp | jam terlihat, tapi bilah menutupi 24 dp lebih banyak halaman |

## Angka: apa artinya bagi tinggi halaman

Angka dari HP-mu, layar 873 × 393 dp.

| Keadaan | Area kerja | Chrome | Area halaman | Perubahan |
|---|---|---|---|---|
| `v1.10.2` tegak, bilah 49 dp | 825 dp | 49 | 776 dp | pembanding |
| Fase 2 tegak, dua baris 100 dp + jarak 6 | 825 dp | 106 | **719 dp** | turun 57 dp |
| `v1.10.2` mendatar, bilah 49 dp | 345 dp | 49 | 296 dp | pembanding |
| Fase 2 mendatar, bilah menumpang | 345 dp | 0 | **345 dp** | naik 49 dp |

Jadi: **mendatar menang besar, tegak kalah 57 dp**. Ini konsisten dengan alasan di dokumen, bahwa pita
hitam tegak sangat tinggi sehingga dua baris tidak mengorbankan apa pun. Saya setuju untuk tegak,
asalkan angka 57 dp ini kamu tahu.

Yang penting dan sudah benar di dokumen: **ukuran virtual tidak berubah**. Lebar dan tinggi CSS tetap
mengikuti preset. Yang berubah hanya skala tampil dan ukuran kotak. Ini sudah sesuai arsitektur
sekarang, karena `applyVirtualScreen` memisahkan ukuran virtual dari ukuran kotak.

Satu efek samping yang perlu disebut: di tegak, kotak yang mengecil bisa membuat preset 1440 dan 1920
berpindah dari jalur biasa ke jalur transformasi, karena skalanya melewati batas 25 persen Chromium.
Itu tidak melanggar checklist, karena `innerWidth` dan `innerHeight` tetap sama, tapi tajam teksnya
sedikit berbeda. Ini sudah terjadi sekarang dan bukan hal baru.

## Risiko teknis dan cara saya menanganinya

### 1. Dua mekanisme sembunyi tidak boleh hidup bersama

`ChromeVisibility` sekarang memakai gulir. Dokumen meminta timer dan hanya di mendatar. Saya akan
**membuang** mekanisme gulir sepenuhnya, bukan menumpuknya. Kalau keduanya ada, bilah bisa hilang saat
kamu sedang mengetik lalu muncul sendiri, dan tidak ada cara memprediksi hasilnya.

Penggantinya, `IdleHide`, dengan aturan yang bisa diuji: sembunyikan bila mendatar, bilah terlihat,
tidak fokus, tidak ada menu terbuka, dan sudah lewat 3 detik sejak sentuhan terakhir.

### 2. PopupMenu tidak bisa subjudul

Dokumen meminta judul 12 sp warna sekunder, radio 44 dp, dan "Tukar lebar dan tinggi" dengan ikon
**dan subteks** (`Sekarang mendatar · 1920×1080`). `PopupMenu` Android tidak mendukung subteks sama
sekali, dan gaya judulnya terbatas.

Karena itu chip dan menu ⋮ harus memakai **satu komponen sendiri**: `PopupWindow` dengan tata letak
custom. Ini lebih banyak kode daripada menu sekarang, tapi justru itu yang membuat "satu komponen, dua
pintu masuk" benar-benar terjadi, bukan dua menu yang mirip.

### 3. Kolom URL: domain saja tanpa kehilangan alamat penuh

Ini bagian yang paling mudah salah. Kalau saya menulis domain saja ke dalam kolom saat tidak fokus,
maka alamat penuh hilang dari kolom, dan menekan tombol kembali ke halaman itu akan memuat domain saja
tanpa jalur halaman.

Usulan saya: kolom tetap menyimpan alamat penuh, dan tampilan tidak fokus memakai lapisan terpisah
berisi domain dan gembok, seperti yang biasa dilakukan browser. Saat kolom disentuh, lapisan itu
menyingkir dan kolom menampilkan alamat penuh dengan seluruh teks terpilih. Dengan cara ini alamat
penuh tidak pernah hilang, dan gembok bisa muncul tanpa memotong domain.

Untuk aturan "sembunyikan `https://` dulu, lalu gembok", lebarnya diukur dengan `Paint.measureText`
terhadap lebar kolom yang tersedia, bukan diperkirakan dari jumlah huruf.

### 4. Geseran dari zona atas

Potongan kode di dokumen sudah benar arahnya, dan saya akan memakai pola yang sama: `OnTouchListener`
di WebView, bukan lapisan transparan di atasnya, supaya ketukan pada header situs tidak mati.

Dua detail yang saya perbaiki dari contoh itu:

- `ACTION_CANCEL` hanya dikirim bila WebView memang sudah menerima `ACTION_DOWN`. Kalau tidak, WebView
  menerima pembatalan untuk gestur yang tidak pernah dimulai.
- Setelah menelan gestur, posisi jari harus dilacak sampai `UP` atau `CANCEL`, dan bilah tidak boleh
  disembunyikan timer selama gestur berjalan.

### 5. Pegangan dan petunjuk sekali

Pegangan 32 × 4 dp adalah satu `View` kecil di lapisan atas, muncul 2 detik setelah bilah hilang, dan
bisa diketuk. Petunjuk sekali butuh satu kunci baru di penyimpanan. Saya akan menaruhnya sebagai kunci
terpisah, bukan menambah kolom di `BrowserSettings`, karena `BrowserSettings` sengaja hanya berisi
pilihan yang mengubah tampilan halaman.

## Urutan kerja yang saya pakai

Delapan tahap di dokumen saya ikuti apa adanya, hanya saya tambahkan verifikasi di tiap tahap:

| Tahap | Isi | Berkas utama | Cara saya verifikasi |
|---|---|---|---|
| 1 | Dimensi per orientasi, style 44 dp, ripple bounded, warna ikon | `values/dimens.xml`, `values-land/dimens.xml`, `styles.xml`, `drawable/ripple_44.xml` | Pemeriksa sumber daya: nol referensi menggantung |
| 2 | Bilah tegak dua baris, mendatar satu baris, forward `GONE` | `layout/activity_main.xml` | Kompilasi dengan stub binding, cek XML sah |
| 3 | Chip ukuran dan tombol tukar | `layout`, `MainActivity` | Chip dan menu memanggil satu fungsi yang sama |
| 4 | Kolom URL: domain, gembok, `https://` bersyarat | baru: `domain/AddressDisplay.kt` + test | Test murni: urutan sembunyi, batas lebar |
| 5 | Popup ukuran sendiri | baru: `ui/ScreenSizePopup.kt` | Dua pintu masuk memakai komponen yang sama |
| 6 | Padding, jarak, inset | `values`, `values-land`, `MainActivity` | Angka 8/4/6 dan 0/2/0 dibaca dari sumber daya |
| 7 | Auto-hide mendatar | baru: `domain/IdleHide.kt` + test, `MainActivity` | Test: 3 detik, fokus, menu terbuka |
| 8 | Audit anti-slop dan Delivery Gate | `anti-slop/audit-002-*.md` | Laporan PASS/FAIL dengan bukti |

Perkiraan yang jujur: tahap 4 dan 7 adalah yang paling rawan, karena keduanya menyentuh perilaku yang
hanya bisa dibuktikan di HP. Sisanya soal tata letak dan bisa saya periksa sampai ke angkanya.

## Checklist yang tidak bisa saya buktikan sendiri

Empat butir dari bagian 9 hanya bisa dijawab dari HP-mu:

1. Ripple tidak terpotong dan tidak melewati batas tombol.
2. Geseran di zona 12 sampai 52 dp memunculkan bilah tanpa menggeser halaman dan tanpa memunculkan
   bilah status sistem.
3. Ketukan pada header situs di area atas tetap sampai ke situs saat bilah tersembunyi.
4. Pegangan muncul dan bisa diketuk.

Tiga butir lain bisa saya ukur sendiri: ukuran virtual identik, angka padding, dan ikon seragam.

## Yang perlu kamu putuskan sebelum saya mulai

1. **Bilah status di mendatar**: ikut dokumen (hilang), atau tetap terlihat seperti `v1.10.2`.
   Ini yang paling penting, karena terakhir kamu menegur saya soal ini.
2. **Auto-hide di tegak**: dokumen bilang tidak ada. Berarti fitur sembunyi saat menggulir yang sekarang
   hilang sepenuhnya di tegak. Setuju?
3. **Baris kedua di tegak**: 100 dp chrome, halaman turun 57 dp. Setuju, atau mau chip ukuran dipindah
   ke bilah utama dan tombol tukar saja yang di baris kedua?


## Keputusan pemilik terhadap panduan Fase 2

Jawaban diberikan sebagai enam angka terhadap tabel "enam hal yang berbalik", bukan terhadap tiga
pertanyaan di akhir dokumen ini. Dibaca begitu, artinya:

| # | Panduan Fase 2 | Keputusan | Yang dikerjakan |
|---|---|---|---|
| 1 | Tukar kembali ke bilah | **Ditolak**, tetap hanya di menu | tidak ada perubahan |
| 2 | Chip ukuran selalu terlihat | **Ditolak**, tetap hanya di menu | tidak ada perubahan |
| 3 | Sembunyi hanya mendatar, pakai timer 3 detik | **Ditolak**, tetap sembunyi saat digulir di dua orientasi | tidak ada perubahan |
| 4 | Tombol 44 dp, ripple berbatas | **44 dp disetujui**, ripple **tetap tanpa batas** | `icon_button_size` 48 ke 44, padding 8 ke 10 |
| 5 | Tegak dua baris 100 dp | **Ditolak**, tetap satu baris 48 dp | tidak ada perubahan |
| 6 | Domain saja, gembok dan https bersyarat | "tergantung lebar layar" | **belum dikerjakan**, menunggu penegasan |

Akibatnya Fase 2 menyusut menjadi satu perubahan kecil. Hitungannya: chrome tetap 49 dp di dua
orientasi, ukuran virtual tidak tersentuh, dan mekanisme sembunyi saat gulir tetap dipakai.

Satu hal yang tidak terjawab dan tidak saya putuskan sendiri: apakah bilah status sistem ikut
disembunyikan di mode mendatar seperti tertulis di panduan bagian 4. Karena pemilik pernah menegur
saya soal ini, keadaan sekarang dipertahankan: bilah status tetap terlihat.
