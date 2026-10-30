from PIL import Image, ImageDraw, ImageFont
import glob

# Perbandingan tinggi pita lipat pada skala dp sebenarnya. Ikon bilah sengaja
# tidak digambar, karena yang dibandingkan hanya tingginya dan sasaran sentuh.
S = 2
W = 393
MARGIN = 16
BAR = 48
PAGE = 40

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; divider = "#D6D8DB"
signal = "#B4441F"; page = "#EDEEF0"; pita_warna = "#FAEDE8"

OPTIONS = [
    (10, "A. Usul Anda: pita 10 dp, sekitar 1/5 bilah", [
        "Bilah 48 + pita 10 = 58 dp. Tinggi pitanya hanya 23% dari 44 dp.",
        "Saat terlipat 10 dp tetap dipakai, jadi halaman tidak pernah penuh.",
    ]),
    (20, "B. Pita 20 dp, sekitar 2/5 bilah", [
        "Bilah 48 + pita 20 = 68 dp. Dua kali lebih mudah ditepuk daripada 10 dp.",
        "Saat terlipat 20 dp tetap dipakai. Ini yang saya sarankan.",
    ]),
    (0, "C. Tanpa pita, pegangan 14 dp menumpang", [
        "Tidak ada tinggi tambahan, dan halaman penuh saat terlipat.",
        "Tapi saat terlipat tidak ada bidang yang bisa ditepuk, hanya bisa digeser.",
    ]),
]

JUDUL_BLOK = 20
CATATAN_BARIS = 15
JEDA = 20
TINGGI_BLOK = [
    JUDUL_BLOK + BAR + (p if p else 14) + PAGE + 8 + 2 * CATATAN_BARIS + JEDA
    for p, _, _ in OPTIONS
]
TOTAL = 44 + sum(TINGGI_BLOK)

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


def panah(cx, cy, ukuran=5):
    d.line([((cx - ukuran) * S), ((cy - ukuran / 2) * S), (cx * S), ((cy + ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))
    d.line([((cx + ukuran) * S), ((cy - ukuran / 2) * S), (cx * S), ((cy + ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))


teks(MARGIN, 22, "Tiga tinggi pita lipat, skala sebenarnya", 14, ink, bold=True)

y = 44
for (pita, judul, baris), tinggi_blok in zip(OPTIONS, TINGGI_BLOK):
    teks(MARGIN, y + 10, judul, 12, ink, bold=True)
    y_bar = y + JUDUL_BLOK

    # Bilah alamat
    kotak(0, y_bar, W, BAR, fill=paper)
    d.line([(0, y_bar * S), (W * S, y_bar * S)], fill=divider, width=int(1 * S))
    d.line([(0, (y_bar + BAR) * S), (W * S, (y_bar + BAR) * S)], fill=divider, width=int(1 * S))
    teks(MARGIN, y_bar + BAR / 2, "bilah alamat 48 dp", 10, muted)

    if pita > 0:
        y_pita = y_bar + BAR
        kotak(0, y_pita, W, pita, fill=pita_warna)
        # Tanda ukur tiap 10 dp, supaya tinggi pita bisa dibaca langsung.
        for i in range(1, pita // 10 + 1):
            yy = y_pita + i * 10
            d.line([(MARGIN * S), (yy * S), ((W - MARGIN) * S), (yy * S)],
                   fill=divider, width=int(1 * S))
        panah(W / 2, y_pita + pita / 2)
        teks(W - MARGIN, y_pita + pita / 2, f"pita {pita} dp", 9.5, signal, anchor="rm")
        y_halaman = y_pita + pita
    else:
        y_halaman = y_bar + BAR

    # Halaman
    kotak(0, y_halaman, W, PAGE, fill=page)
    teks(MARGIN, y_halaman + PAGE / 2, "halaman web", 9.5, muted)

    if pita == 0:
        # Pegangan digambar di atas halaman karena ia menumpang, bukan
        # menambah tinggi.
        kotak(W / 2 - 40, y_halaman, 80, 14, fill=pita_warna, outline=signal, radius=7)
        panah(W / 2, y_halaman + 7, ukuran=4)
        teks(W - MARGIN - 4, y_halaman + 7, "14 dp", 9.5, signal, anchor="rm")

    for i, isi in enumerate(baris):
        teks(MARGIN, y_halaman + PAGE + 12 + i * CATATAN_BARIS, isi, 9.5, muted)

    y += tinggi_blok

img.save("/home/user/DesktopBrowser/usul-pita-lipat.png")
print("gambar dibuat", img.size)
