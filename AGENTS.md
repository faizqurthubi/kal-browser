# Kal Browser

Aplikasi Android satu modul: pembungkus WebView dengan mode desktop.

## Urutan baca sebelum menyentuh kode

1. `PANDUAN_DesktopBrowser.md` (spesifikasi, menang atas sumber lain bila bentrok)
2. `DESIGN.md` (arah desain dan alasannya)
3. `.agents/antislop.md` (filter) dan skill terkait di `.agents/skills/`

## Bentuk kode

- Satu modul `app`, tanpa multi-module, tanpa Hilt, tanpa flavor.
- XML View dan WebView, bukan Compose.
- Layer UI, domain, data. Arus data satu arah: ViewModel + StateFlow, state UI satu data class.
- Pengaturan lewat repository + DataStore, tidak pernah langsung di Activity.
- Versi dependensi di `gradle/libs.versions.toml`.
- Test memakai test double, bukan mocking.
- Komentar kode berbahasa Indonesia, hanya untuk alasan (mengapa), bukan mengulang kode.

<!-- antislop:start -->
## antislop
For UI, copy, people, mobile layout, or code comments work, read `.agents/antislop.md` (core) and then the skill for the task:
- UI / visual: `.agents/skills/antislop-ui/SKILL.md`
- Copy & text: `.agents/skills/antislop-copywriting/SKILL.md`
- People: `.agents/skills/antislop-human/SKILL.md`
- Mobile / responsive: `.agents/skills/antislop-layoutmobile/SKILL.md`
- Code comments: `.agents/skills/antislop-code/SKILL.md`

Before starting, follow the core's "Two Usage Modes" section in strict order: explicit session instruction first, then global preference, then ask. A session instruction always wins. For a resolved mode, say `antislop active: <mode> (session override).` or `antislop active: <mode> (global preference).` once before presenting findings or making edits, using the actual mode and source. Acknowledging the user's request without naming the source does not replace this notice.
Only an explicit choice of antislop during or after selects a session mode. A request to review, audit, or avoid file edits does not select a mode; read the global preference in that case. Another skill's mode does not select antislop's mode.
If the mode is unresolved, ask during/after and end the response; wait for the answer before any UI review, planning, or concept. For read-only tasks, put the active-mode notice only at the start of the final answer, never in progress messages. For editing tasks, announce before the first edit and omit it from the final answer.
<!-- antislop:end -->
