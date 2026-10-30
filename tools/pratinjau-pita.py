from PIL import Image, ImageDraw, ImageFont
import glob

# Pratinjau dua keadaan pita 14 dp yang ikut bergerak bersama bilah.
# Skala dp sebenarnya untuk lebar 393 dp.
S = 3
W = 393
PITA = 14
BAR = 48
HALAMAN = 250
MARGIN = 16

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; divider = "#D6D8DB"
signal = "#B4441F"; page = "#EDEEF0"; kepala = "#DDE0E3"

JUDUL = 30
CATATAN = 34
JEDA = 22
TINGGI_PANEL = JUDUL + PITA + BAR + HALAMAN + CATATAN + JEDA
TOTAL = 44 + 2 * TINGGI_PANEL

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


def panah(cx, cy, ke_atas, ukuran=5):
    tanda = -1 if ke_atas else 1
    d.line([((cx - ukuran) * S), ((cy - tanda * ukuran / 2) * S), (cx * S), ((cy + tanda * ukuran / 2) * S)],
           fill=muted, width=int(1.6 * S))
    d.line([((cx + ukuran) * S), ((cy - tanda * ukuran / 2) * S), (cx * S), ((cy + tanda * ukuran / 2) * S)],
           fill=muted, width=int(1.6 * S))


def pita(y, ke_atas):
    kotak(0, y, W, PITA, fill=paper)
    d.line([(0, y * S), (W * S, y * S)], fill=divider, width=int(1 * S))
    d.line([(0, (y + PITA) * S), (W * S, (y + PITA) * S)], fill=divider, width=int(1 * S))
    panah(W / 2, y + PITA / 2, ke_atas)
    teks(W - MARGIN, y + PITA / 2, "pita 14 dp", 8, signal, anchor="rm")


def bilah(y):
    kotak(0, y, W, BAR, fill=paper)
    # Ikon kiri
    for i, nama in enumerate(["back", "forward", "reload"]):
        cx = 8 + i * 44 + 22
        cy = y + BAR / 2
        # Koordinat garis harus dikali S, kalau tidak panahnya menggambar di
        # sepertiga posisi yang seharusnya.
        if nama == "back":
            d.line([((cx + 5) * S, (cy - 6) * S), ((cx - 5) * S, cy * S), ((cx + 5) * S, (cy + 6) * S)],
                   fill=ink, width=int(2 * S), joint="curve")
        elif nama == "forward":
            d.line([((cx - 5) * S, (cy - 6) * S), ((cx + 5) * S, cy * S), ((cx - 5) * S, (cy + 6) * S)],
                   fill=ink, width=int(2 * S), joint="curve")
        else:
            d.arc([(cx - 7) * S, (cy - 7) * S, (cx + 7) * S, (cy + 7) * S], start=45, end=350,
                  fill=ink, width=int(2 * S))
            d.line([((cx + 7) * S, (cy - 9) * S), ((cx + 7) * S, (cy - 3) * S)],
                   fill=ink, width=int(2 * S))
    hx = W - 8 - 22
    hy = y + BAR / 2
    for dy in (-5, 0, 5):
        d.line([((hx - 8) * S, (hy + dy) * S), ((hx + 8) * S, (hy + dy) * S)],
           fill=ink, width=int(2 * S))
    ax = 8 + 3 * 44 + 4
    aw = (W - 8 - 44) - ax - 4
    kotak(ax, y + 6, aw, BAR - 12, fill=None, outline=divider, radius=4)
    teks(ax + 10, y + BAR / 2, "example.com", 12, muted)


def halaman(y, tinggi):
    kotak(0, y, W, tinggi, fill=page)
    kotak(MARGIN, y + 10, W - 2 * MARGIN, 24, fill=kepala)
    teks(MARGIN + 8, y + 22, "kepala halaman situs", 8.5, muted)
    for i in range(3):
        kotak(MARGIN, y + 46 + i * 16, W - 2 * MARGIN - i * 40, 8, fill="#E1E4E7")


teks(MARGIN, 20, "Pita 14 dp ikut bergerak bersama bilah", 13, ink, bold=True)

y = 44

# Terbuka
teks(MARGIN, y + 9, "terbuka: bilah 48 + pita 14 = 62 dp", 11, ink, bold=True)
y0 = y + JUDUL
bilah(y0)
pita(y0 + BAR, ke_atas=True)
halaman(y0 + BAR + PITA, HALAMAN)
teks(MARGIN, y0 + BAR + PITA + HALAMAN + 12,
     "Halaman mulai tepat di bawah pita, jadi pitanya punya jalur sendiri.", 9.5, muted)
teks(MARGIN, y0 + BAR + PITA + HALAMAN + 26,
     "Titik ketuk panah ada di 48 hingga 62 dp dari tepi atas.", 9.5, muted)
y += TINGGI_PANEL

# Terlipat
teks(MARGIN, y + 9, "terlipat: bilah pergi, pita berhenti di tepi atas", 11, ink, bold=True)
y0 = y + JUDUL
pita(y0, ke_atas=False)
halaman(y0 + PITA, HALAMAN + BAR)
kotak(0, y0, W, PITA, fill=None, outline=None)
teks(MARGIN, y0 + PITA + HALAMAN + BAR + 12,
     "Bilah pergi, halaman bertambah 48 dp, dan pita menumpang di atasnya.", 9.5, muted)
teks(MARGIN, y0 + PITA + HALAMAN + BAR + 26,
     "Titik ketuk panah pindah ke 0 hingga 14 dp, dan tetap di tengah layar.", 9.5, muted)

img.save("/home/user/DesktopBrowser/pratinjau-pita.png")
print("gambar dibuat", img.size)
