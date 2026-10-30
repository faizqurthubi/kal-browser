from PIL import Image, ImageDraw, ImageFont
import glob

# Gambar penjelasan, bukan tangkapan layar aplikasi. Isinya memperlihatkan apa
# yang tersimpan di ponsel saat daftar ukuran berubah, dan kenapa menyimpan
# angka lebih aman daripada menyimpan nomor urut.

S = 2
W, H = 460, 1000
img = Image.new("RGB", (W * S, H * S), "#FBFBF9")
d = ImageDraw.Draw(img)

ink = "#17191C"; muted = "#5A5F66"; border = "#7C838B"
divider = "#D6D8DB"; signal = "#B4441F"; paper = "#FFFFFF"

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None


def F(px, bold=False):
    path = BOLD if bold else REG
    return ImageFont.truetype(path, int(px * S)) if path else ImageFont.load_default()


def teks(x, y, s, size, fill, anchor="lm", bold=False):
    d.text((x * S, y * S), s, font=F(size, bold), fill=fill, anchor=anchor)


def kotak(x, y, w, h, fill=None, outline=None, width=1, radius=6):
    d.rounded_rectangle([x * S, y * S, (x + w) * S, (y + h) * S],
                        radius=radius * S, fill=fill, outline=outline,
                        width=max(1, int(width * S)))


LAMA = ["1024 × 768", "1280 × 720", "1366 × 768", "1440 × 900", "1920 × 1080"]
BARU = LAMA[:4] + ["1600 × 900", "1680 × 1050", "1920 × 1080", "2560 × 1440"]

teks(24, 26, "Kenapa nomor urut berbahaya, angka tidak", 15, ink, bold=True)


def daftar(x, y, judul, isi, tandai, catatan, warna_tandai):
    teks(x, y, judul, 11, muted, bold=True)
    y += 18
    for i, ukuran in enumerate(isi):
        baris_y = y + i * 26
        aktif = tandai.get(i)
        fill = paper
        outline = divider
        width = 1
        if aktif == "salah":
            fill = "#FBEFEA"; outline = signal; width = 1.6
        elif aktif == "benar":
            fill = "#F2F4F6"; outline = ink; width = 1.6
        kotak(x, baris_y, 150, 22, fill=fill, outline=outline, width=width, radius=4)
        teks(x + 10, baris_y + 11, f"indeks {i}", 9, muted)
        teks(x + 62, baris_y + 11, ukuran, 10, ink, bold=bool(aktif))
        if aktif == "salah":
            teks(x + 160, baris_y + 11, "dipakai aplikasi", 9, signal, bold=True)
        elif aktif == "benar":
            teks(x + 160, baris_y + 11, "yang tadi dipilih", 9, ink, bold=True)
    y += len(isi) * 26 + 6
    for i, baris in enumerate(catatan):
        teks(x, y + i * 13, baris, 9.5, warna_tandai if i == 0 else muted)
    return y + len(catatan) * 13


y = 52
y = daftar(
    24, y, "HARI INI, DI PONSEL ANDA", LAMA, {4: "benar"},
    ["Yang disimpan di ponsel: indeks 4.", "Ukuran yang Anda lihat: 1920 × 1080."],
    ink,
) + 22

y = daftar(
    24, y, "VERSI BERIKUTNYA MENAMBAH TIGA UKURAN", BARU,
    {4: "salah", 6: "benar"},
    ["Indeks 4 sekarang berarti 1600 × 900.",
     "Aplikasi membuka ukuran yang tidak pernah Anda pilih,",
     "tanpa pesan apa pun."],
    signal,
) + 22

y = daftar(
    24, y, "KALAU YANG DISIMPAN ANGKANYA", BARU, {6: "benar"},
    ["Yang disimpan: lebar 1920, tinggi 1080.",
     "Daftar berubah sebanyak apa pun, ukurannya tetap sama."],
    ink,
)

# Keterangan dasar, diletakkan setelah tinggi isi diketahui.
d.line([24 * S, (y + 6) * S, (W - 24) * S, (y + 6) * S], fill=divider, width=int(1 * S))
teks(24, y + 20, "Angka di gambar ini contoh. Hari ini daftarnya masih lima ukuran.", 9.5, muted)

img = img.crop((0, 0, W * S, (y + 34) * S))
img.save("/home/user/DesktopBrowser/bukti-indeks-tersimpan.png")
print("gambar dibuat", img.size)
