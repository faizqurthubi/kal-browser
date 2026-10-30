from PIL import Image, ImageDraw, ImageFont
import glob

# Keadaan TERLIPAT pada tiga varian, skala dp sebenarnya. Yang digambar hanya
# apa yang bisa ditepuk saat bilah alamat sudah menutup.
S = 2
W = 393
MARGIN = 16
PAGE = 84

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; divider = "#D6D8DB"
signal = "#B4441F"; page = "#EDEEF0"; pita_warna = "#FAEDE8"
area = "#B4441F"

# (tinggi pita, menumpang, judul, catatan, tinggi blok)
VARIAN = [
    (10, False, "A. Pita 10 dp tinggal di atas halaman",
     ["Pita tetap di tempatnya, halaman mulai di bawahnya. Garis putus-putus",
      "adalah area sentuh 44 dp yang tidak terlihat, menutupi tepi atas halaman."]),
    (20, False, "B. Pita 20 dp tinggal di atas halaman",
     ["Pita tetap di tempatnya, halaman mulai di bawahnya. Sasaran sentuhnya",
      "dua kali lebih tinggi, dan yang tertutup sama saja: 8 dp tepi halaman."]),
    (14, True, "C. Pegangan 14 dp menumpang di halaman",
     ["Halaman mulai dari tepi atas. 44 dp pertamanya tertutup pegangan, dan",
      "itu termasuk kepala halaman situs. Membuka hanya lewat pegangan ini."]),
]

JUDUL = 30
CATATAN_BARIS = 15
JEDA = 22
TINGGI_BLOK = [JUDUL + PAGE + 46 + JEDA for _ in VARIAN]
TOTAL = 40 + sum(TINGGI_BLOK)

img = Image.new("RGB", (W * S, TOTAL * S), "#FFFFFF")
d = ImageDraw.Draw(img)


def F(px, bold=False):
    path = BOLD if bold else REG
    return ImageFont.truetype(path, int(px * S)) if path else ImageFont.load_default()


def teks(x, y, s, size, fill, anchor="lm", bold=False):
    d.text((x * S, y * S), s, font=F(size, bold), fill=fill, anchor=anchor)


def kotak(x, y, w, h, fill=None, outline=None, width=1, radius=0):
    box = [(x * S), (y * S), ((x + w) * S), ((y + h) * S)]
    if radius:
        d.rounded_rectangle(box, radius=radius * S, fill=fill, outline=outline,
                            width=max(1, int(width * S)))
    else:
        d.rectangle(box, fill=fill, outline=outline, width=max(1, int(width * S)))


def putus_putus(x, y, w, h):
    """Garis putus-putus untuk area sentuh yang tidak terlihat."""
    langkah = 4
    for xx in range(int(x), int(x + w), langkah * 2):
        d.line([(xx * S), (y * S), (min(xx + langkah, x + w) * S), (y * S)],
               fill=area, width=int(1 * S))
        d.line([(xx * S), ((y + h) * S), (min(xx + langkah, x + w) * S), ((y + h) * S)],
               fill=area, width=int(1 * S))
    for yy in range(int(y), int(y + h), langkah * 2):
        d.line([(x * S), (yy * S), (x * S), (min(yy + langkah, y + h) * S)],
               fill=area, width=int(1 * S))
        d.line([((x + w) * S), (yy * S), ((x + w) * S), (min(yy + langkah, y + h) * S)],
               fill=area, width=int(1 * S))


def panah_atas(cx, cy, ukuran=5):
    d.line([((cx - ukuran) * S), ((cy + ukuran / 2) * S), (cx * S), ((cy - ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))
    d.line([((cx + ukuran) * S), ((cy + ukuran / 2) * S), (cx * S), ((cy - ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))


teks(MARGIN, 20, "Keadaan terlipat: apa yang bisa ditepuk", 13, ink, bold=True)

y = 40
for (pita, menumpang, judul, baris), tinggi_blok in zip(VARIAN, TINGGI_BLOK):
    teks(MARGIN, y + 6, judul, 12, ink, bold=True)
    y0 = y + JUDUL

    # Halaman selalu digambar lebih dulu.
    kotak(0, y0, W, PAGE, fill=page)
    # Tiruan isi halaman bagian atas, supaya kelihatan apa yang tertutup.
    kotak(MARGIN, y0 + 8, W - 2 * MARGIN, 22, fill="#DDE0E3")
    teks(MARGIN + 8, y0 + 19, "kepala halaman situs", 8.5, muted)
    teks(MARGIN, y0 + 52, "halaman web", 9, muted)

    if menumpang:
        # Pegangan menumpang: tidak memakai tinggi, tapi menutupi halaman.
        kotak(W / 2 - 40, y0 + 4, 80, pita, fill=pita_warna, outline=signal, radius=pita / 2)
        panah_atas(W / 2, y0 + 4 + pita / 2, ukuran=4)
        # Area sentuh 44 dp berpusat di pegangan, jadi separuhnya masuk ke halaman.
        putus_putus(W / 2 - 22, y0 - 8, 44, 44)
    else:
        kotak(0, y0, W, pita, fill=pita_warna)
        d.line([(0, y0 * S), (W * S, y0 * S)], fill=divider, width=int(1 * S))
        d.line([(0, (y0 + pita) * S), (W * S, (y0 + pita) * S)], fill=divider, width=int(1 * S))
        panah_atas(W / 2, y0 + pita / 2)
        putus_putus(0, y0 - 8, W, 44)

    for i, isi in enumerate(baris):
        teks(MARGIN, y0 + PAGE + 12 + i * CATATAN_BARIS, isi, 9.5, muted)

    y += tinggi_blok

img.save("/home/user/DesktopBrowser/usul-pita-terlipat.png")
print("gambar dibuat", img.size)
