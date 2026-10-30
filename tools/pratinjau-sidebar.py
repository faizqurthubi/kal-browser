from PIL import Image, ImageDraw, ImageFont
import glob

# Pratinjau tata letak, bukan tangkapan layar aplikasi. Semua angka disalin dari
# dimens.xml dan SizeGrid supaya proporsi bisa diperiksa sebelum APK dipasang.
S = 3
W, H = 393, 873
img = Image.new("RGB", (W * S, H * S), "#ffffff")
d = ImageDraw.Draw(img)

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; border = "#7C838B"
divider = "#D6D8DB"; signal = "#B4441F"; letterbox = "#000000"

FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
if not glob.glob(FONT):
    FONT = None


def F(px):
    return ImageFont.truetype(FONT, int(px * S)) if FONT else ImageFont.load_default()


def r(x, y, w, h, fill=None, outline=None, width=1, radius=0):
    box = [x * S, y * S, (x + w) * S, (y + h) * S]
    if radius:
        d.rounded_rectangle(box, radius=radius * S, fill=fill, outline=outline, width=max(1, int(width * S)))
    else:
        d.rectangle(box, fill=fill, outline=outline, width=max(1, int(width * S)))


def text(x, y, s, size, fill, anchor="lm"):
    d.text((x * S, y * S), s, font=F(size), fill=fill, anchor=anchor)


PRESETS = [(1024, 768), (1280, 720), (1366, 768), (1440, 900), (1920, 1080)]
SISI_TERPANJANG = 48.0          # size_card_longest
LEBAR_SEL = 63.0                # size_cell_width
TINGGI_SEL = 64.0               # size_cell_height
RUANG_LABEL = TINGGI_SEL - SISI_TERPANJANG
SKALA = SISI_TERPANJANG / max(max(w, h) for w, h in PRESETS)

# Halaman di belakang
r(0, 0, W, H, fill=letterbox)
r(0, 57, W, H - 57, fill=paper)
r((W - 353) / 2, 65, 353, 353 * 1080 / 1920, fill="#EDEEF0")

# Bilah atas: kembali, maju, muat ulang, kotak alamat, hamburger di ujung kanan
r(0, 0, W, 48, fill=paper)
r(0, 48, W, 1, fill=divider)
for i, name in enumerate(["back", "forward", "reload"]):
    cx = 8 + i * 44 + 22
    cy = 24
    if name == "back":
        d.line([(cx + 5, cy - 6), (cx - 5, cy), (cx + 5, cy + 6)], fill=ink, width=int(2 * S), joint="curve")
    elif name == "forward":
        d.line([(cx - 5, cy - 6), (cx + 5, cy), (cx - 5, cy + 6)], fill=ink, width=int(2 * S), joint="curve")
    else:
        d.arc([cx - 7, cy - 7, cx + 7, cy + 7], start=45, end=350, fill=ink, width=int(2 * S))
        d.line([(cx + 7, cy - 9), (cx + 7, cy - 3)], fill=ink, width=int(2 * S))

hamburger_cx = W - 8 - 22
for dy in (-5, 0, 5):
    d.line([(hamburger_cx - 8, 24 + dy), (hamburger_cx + 8, 24 + dy)], fill=ink, width=int(2 * S))

af_x = 8 + 3 * 44 + 4
af_w = (W - 8 - 44) - af_x - 4
r(af_x, 6, af_w, 36, fill=None, outline=divider, width=1, radius=4)
text(af_x + 10, 24, "example.com", 13, muted)

# Scrim menutupi sisa layar di kiri panel
scrim = Image.new("RGBA", img.size, (0, 0, 0, 0))
sd = ImageDraw.Draw(scrim)
sd.rectangle([0, 0, img.size[0], img.size[1]], fill=(0, 0, 0, 102))
img = Image.alpha_composite(img.convert("RGBA"), scrim).convert("RGB")
d = ImageDraw.Draw(img)

# Panel dari kanan
SW = 284
SX = W - SW
r(SX, 0, SW, H, fill=paper)
d.line([(SX * S, 0), (SX * S, H * S)], fill=divider, width=int(1 * S))

PAD = 16
JARAK_BARIS = 10

JARAK_LABEL = 4      # size_label_gap
JARAK_BAGIAN = 24    # size_section_gap
JARAK_JUDUL = 8      # size_heading_gap
TINGGI_JUDUL_BAGIAN = 16

text(SX + PAD, 26, "Ukuran layar", 14, ink)


def gambar_bagian(daftar, y0, aktif_idx, judul=None, slot=8):
    # y0 adalah tepi atas judul bagian. Judul setinggi 16 dp, lalu jarak kecil
    # ke kisi, sesuai size_label_gap.
    if judul:
        text(SX + PAD, y0 + TINGGI_JUDUL_BAGIAN / 2, judul, 12, muted)
        y0 += TINGGI_JUDUL_BAGIAN + JARAK_LABEL
    for i in range(slot):
        baris, kolom = divmod(i, 4)
        sel_x = SX + PAD + kolom * LEBAR_SEL
        sel_y = y0 + baris * (TINGGI_SEL + JARAK_BARIS)
        if i >= len(daftar):
            continue  # slot kosong, tidak menggambar apa pun
        pw, ph = daftar[i]
        w_dp = pw * SKALA
        h_dp = ph * SKALA
        # kotak di tengah ruang di atas label
        cx = sel_x + (LEBAR_SEL - w_dp) / 2
        cy = sel_y + (TINGGI_SEL - RUANG_LABEL - h_dp) / 2
        r(cx, cy, w_dp, h_dp, fill=divider,
          outline=signal if i == aktif_idx else border,
          width=2 if i == aktif_idx else 1, radius=2)
        text(sel_x + LEBAR_SEL / 2, sel_y + TINGGI_SEL - RUANG_LABEL / 2,
             f"{pw} × {ph}", 8, ink if i == aktif_idx else muted, anchor="mm")
    baris_total = (slot + 3) // 4
    return y0 + baris_total * (TINGGI_SEL + JARAK_BARIS)


y = gambar_bagian(PRESETS, 34 + JARAK_JUDUL, 1, judul="Landscape")
gambar_bagian([(h, w) for (w, h) in PRESETS], y + JARAK_BAGIAN, -1, judul="Portrait")

# Tombol info di dasar panel
d.line([(SX * S, (H - 53) * S), (W * S, (H - 53) * S)], fill=divider, width=int(1 * S))
cy = H - 53 + 26
d.ellipse([(SX + PAD) * S, (cy - 9) * S, (SX + PAD + 18) * S, (cy + 9) * S], outline=muted, width=int(1.6 * S))
d.line([(SX + PAD + 9) * S, (cy - 4) * S, (SX + PAD + 9) * S, (cy + 5) * S], fill=muted, width=int(1.6 * S))
d.line([(SX + PAD + 9) * S, (cy - 7) * S, (SX + PAD + 9) * S, (cy - 6) * S], fill=muted, width=int(1.6 * S))
text(SX + PAD + 28, cy, "Info halaman", 14, ink)

img = img.resize((W * 2, H * 2), Image.LANCZOS)
img.save("/home/user/DesktopBrowser/pratinjau-sidebar.png")
print("gambar dibuat", img.size)
