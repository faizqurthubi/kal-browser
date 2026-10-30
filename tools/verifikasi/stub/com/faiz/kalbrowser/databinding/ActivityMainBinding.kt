package com.faiz.kalbrowser.databinding

import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

// Tiruan binding untuk kompilasi di luar Gradle. Setiap id di activity_main.xml
// harus punya field di sini, karena kode Activity mengaksesnya lewat nama field.
class ActivityMainBinding private constructor(
    val root: FrameLayout, val browserContent: LinearLayout, val chromeBar: LinearLayout, val toolbar: View,
    val buttonBack: ImageButton, val buttonForward: ImageButton, val buttonReload: ImageButton,
    val addressField: FrameLayout, val addressBar: EditText, val buttonGo: ImageButton,
    val buttonSidebar: ImageButton, val pageProgress: ProgressBar,
    val foldHandle: FrameLayout, val foldArrow: ImageView,
    val viewportWrap: FrameLayout, val viewportContainer: FrameLayout,
    val webView: WebView, val emptyHint: TextView,
    val sidebarScrim: View, val sidebar: LinearLayout, val gridLandscape: GridLayout,
    val gridPortrait: GridLayout, val sidebarInfo: LinearLayout,
    val fullscreenContainer: FrameLayout,
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityMainBinding = throw UnsupportedOperationException()
    }
}
