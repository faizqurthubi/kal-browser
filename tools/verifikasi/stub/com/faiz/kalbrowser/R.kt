package com.faiz.kalbrowser

// Tiruan R untuk kompilasi di luar Gradle. Isinya harus memuat setiap id yang
// dirujuk kode; kalau tidak, kompilasi lokal gagal walau Gradle lolos.
object R {
    object drawable {
        const val ic_back = 1; const val ic_forward = 2; const val ic_reload = 3
        const val ic_stop = 4; const val ic_arrow_right = 6
        const val ic_menu = 9; const val ic_info = 10; const val ic_fold_arrow = 13
        const val bg_address_bar = 14; const val bg_size_cell = 11; const val bg_size_card = 12
    }
    object color {
        const val letterbox = 6; const val paper = 7; const val size_label = 8
        const val ink = 1; const val ink_muted = 2
    }
    object dimen {
        const val sidebar_width = 40; const val toolbar_height = 41; const val field_height = 42
        const val size_card_longest = 43; const val size_cell_width = 44
        const val size_cell_height = 45
    }
    object string {
        const val app_name = 10; const val address_bar_hint = 11; const val action_back = 12
        const val action_forward = 13; const val action_reload = 14; const val action_stop = 15
        const val empty_hint = 20
        const val diagnostics_title = 21; const val action_close = 22
        const val download_started = 23; const val download_failed = 24
        const val download_unsupported = 25; const val download_denied = 26
        const val download_fallback_name = 27; const val upload_unavailable = 28
        const val action_go = 29; const val action_sidebar = 30
        const val sidebar_sizes = 31; const val sidebar_landscape = 32
        const val sidebar_portrait = 33; const val sidebar_diagnostics = 37
        const val action_copy = 34; const val diagnostics_copied = 35; const val diagnostics_copy_failed = 36
        const val action_fold_bar = 38; const val action_unfold_bar = 39
    }
}
