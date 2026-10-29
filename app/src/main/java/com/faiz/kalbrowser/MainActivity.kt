package com.faiz.kalbrowser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.faiz.kalbrowser.PRESETS
import com.faiz.kalbrowser.databinding.ActivityMainBinding
import com.faiz.kalbrowser.desktop.DesktopMode
import com.faiz.kalbrowser.desktop.DesktopSpoof
import com.faiz.kalbrowser.ui.BrowserCommand
import com.faiz.kalbrowser.ui.BrowserUiState
import com.faiz.kalbrowser.ui.BrowserViewModel
import com.faiz.kalbrowser.ui.Letterbox
import com.faiz.kalbrowser.ui.ViewportFit
import org.json.JSONTokener
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: BrowserViewModel by viewModels()
    private lateinit var desktopMode: DesktopMode
    private var updatingSpinnerFromState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WebView tidak ikut didaur ulang saat orientasi berubah karena
        // android:configChanges sudah menahannya, jadi penyiapan di sini hanya
        // berjalan sekali untuk setiap pembuatan activity.
        setupWebView()

        setupControls()
        observeUiState()
        observeCommands()

        // Ukuran container baru diketahui setelah layout berjalan, dan berubah
        // lagi setiap kali keyboard muncul atau perangkat diputar.
        binding.viewportContainer.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val berubah = (right - left) != (oldRight - oldLeft) || (bottom - top) != (oldBottom - oldTop)
            if (berubah) applyVirtualScreen()
        }
        binding.viewportContainer.post { applyVirtualScreen() }
    }

    private fun setupWebView() {
        // Semua penyetelan WebSettings dan penyamaran desktop ada di DesktopMode,
        // supaya Activity tinggal mengurus tampilan dan navigasi.
        desktopMode = DesktopMode(binding.webView)
        desktopMode.apply(viewModel.uiState.value.viewportWidth)

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean = forwardNonHttpScheme(request.url)

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                viewModel.onPageStarted(url.orEmpty())
                desktopMode.injectViewportNow()
                syncHistory()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                viewModel.onPageFinished(url.orEmpty())
                desktopMode.injectViewportNow()
                syncHistory()
            }

            // Alamat di kolom tidak selalu berubah pasangan dengan onPageStarted,
            // misalnya saat situs menulis riwayat lewat history.pushState.
            override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                viewModel.onUrlChanged(url.orEmpty())
                syncHistory()
            }
        }

        // Latar WebView dibuat senada letterbox supaya halaman yang belum termuat
        // tidak tampil sebagai kotak putih di tengah layar hitam.
        binding.webView.setBackgroundColor(getColor(R.color.letterbox))

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                viewModel.onProgressChanged(newProgress)
            }
        }
    }

    /** true berarti skema sudah ditangani di luar WebView dan tidak boleh dimuat di sini. */
    private fun forwardNonHttpScheme(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase()
        if (scheme == "http" || scheme == "https") return false

        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (e: ActivityNotFoundException) {
            // Skema tanpa aplikasi penerima dibiarkan, bukan ditampilkan sebagai
            // halaman error, karena pengguna memang tidak bisa berbuat apa pun.
            true
        }
    }

    private fun setupControls() {
        binding.buttonBack.setOnClickListener { viewModel.goBack() }
        binding.buttonForward.setOnClickListener { viewModel.goForward() }
        binding.buttonReload.setOnClickListener { viewModel.reloadOrStop() }
        binding.buttonOpen.setOnClickListener { submitAddress() }

        val presets = ArrayAdapter(this, R.layout.item_screen_size, PRESETS.map { it.label })
        presets.setDropDownViewResource(R.layout.item_screen_size)
        binding.screenSizeSpinner.adapter = presets
        binding.screenSizeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    // Perubahan dari render state bukan pilihan pengguna, dan
                    // memprosesnya akan memicu muat ulang berulang.
                    if (updatingSpinnerFromState) return
                    viewModel.selectPreset(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        binding.buttonRotate.setOnClickListener { viewModel.rotate() }

        binding.addressBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submitAddress()
                true
            } else {
                false
            }
        }

        // Tombol kembali sistem mengikuti riwayat halaman lebih dulu, baru
        // menutup aplikasi, sama seperti browser lain.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    private fun submitAddress() {
        val input = binding.addressBar.text?.toString().orEmpty()
        // Fokus dilepas sebelum memuat supaya kolom alamat mau menampilkan alamat
        // hasil normalisasi, termasuk saat yang diketik adalah kata pencarian.
        binding.addressBar.clearFocus()
        hideKeyboard()
        viewModel.submitAddress(input)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: BrowserUiState) {
        // Kolom alamat hanya ditulis ulang saat tidak sedang difokuskan, kalau
        // tidak kursor ketik akan melompat ke ujung setiap kali progres bergerak.
        if (!binding.addressBar.hasFocus() && binding.addressBar.text.toString() != state.address) {
            binding.addressBar.setText(state.address)
        }

        binding.buttonBack.isEnabled = state.canGoBack
        binding.buttonForward.isEnabled = state.canGoForward

        binding.buttonReload.setImageResource(
            if (state.isLoading) R.drawable.ic_stop else R.drawable.ic_reload,
        )
        binding.buttonReload.contentDescription = getString(
            if (state.isLoading) R.string.action_stop else R.string.action_reload,
        )

        if (binding.screenSizeSpinner.selectedItemPosition != state.selectedPresetIndex) {
            updatingSpinnerFromState = true
            binding.screenSizeSpinner.setSelection(state.selectedPresetIndex)
            updatingSpinnerFromState = false
        }
        binding.screenSizeLabel.text = state.effectiveSizeLabel
        applyVirtualScreen(state.viewportWidth, state.viewportHeight)

        binding.pageProgress.progress = state.progress
        binding.pageProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Keadaan kosong hanya muncul saat belum ada halaman sama sekali, bukan
        // saat halaman sedang dimuat atau gagal.
        binding.emptyHint.visibility = if (state.address.isEmpty() && !state.isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun observeCommands() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.commands.collect { command ->
                    when (command) {
                        is BrowserCommand.LoadUrl -> binding.webView.loadUrl(command.url)
                        BrowserCommand.GoBack -> binding.webView.goBack()
                        BrowserCommand.GoForward -> binding.webView.goForward()
                        BrowserCommand.Reload -> binding.webView.reload()
                        BrowserCommand.Stop -> binding.webView.stopLoading()
                        BrowserCommand.CollectDiagnostics -> showDiagnostics()
                        is BrowserCommand.ApplyViewport -> {
                            desktopMode.setViewportWidth(command.width)
                            // Memuat ulang hanya kalau memang ada halaman. WebView
                            // kosong yang dimuat ulang hanya membuat layar berkedip.
                            if (binding.webView.url != null) binding.webView.reload()
                        }
                    }
                    syncHistory()
                }
            }
        }
    }

    private fun applyVirtualScreen() {
        val state = viewModel.uiState.value
        applyVirtualScreen(state.viewportWidth, state.viewportHeight)
    }

    /**
     * Mengunci kotak WebView ke rasio layar virtual lalu memusatkannya. Sisa
     * ruang dibiarkan hitam sesuai latar container, bukan diregangkan.
     *
     * Ada dua cara mencapai ukuran kotak itu. Selama WebView masih sanggup
     * mengecilkan halaman sendiri, biarkan dia yang mengatur. Kalau skala yang
     * dibutuhkan lebih kecil dari batas Chromium, WebView diletakkan seukuran
     * layar virtual lalu dikecilkan dengan matriks View. Tanpa jalur kedua,
     * halaman 1920 px tersangkut di skala 25% dan bisa digeser ke samping.
     */
    private fun applyVirtualScreen(virtualWidth: Int, virtualHeight: Int) {
        val box = Letterbox.boxFor(
            containerWidth = binding.viewportContainer.width,
            containerHeight = binding.viewportContainer.height,
            virtualWidth = virtualWidth,
            virtualHeight = virtualHeight,
        ) ?: return

        val density = resources.displayMetrics.density
        val mode = ViewportFit.modeFor(box.width, density, virtualWidth)

        if (mode == ViewportFit.Mode.Native) {
            val current = binding.webView.layoutParams
            val sudahPas = current.width == box.width &&
                current.height == box.height &&
                binding.webView.scaleX == 1f &&
                binding.webView.scaleY == 1f
            if (sudahPas) return

            // Skala dari jalur transformasi harus dibersihkan, kalau tidak
            // ukuran barunya mengecil dua kali.
            binding.webView.scaleX = 1f
            binding.webView.scaleY = 1f
            binding.webView.layoutParams = FrameLayout.LayoutParams(
                box.width,
                box.height,
                Gravity.CENTER,
            )
            return
        }

        val current = binding.webView.layoutParams
        val skala = ViewportFit.transformScale(box.width, virtualWidth)
        val sudahPas = current.width == virtualWidth &&
            current.height == virtualHeight &&
            binding.webView.scaleX == skala
        if (sudahPas) return

        // Titik pivot di tengah menjaga kotak tetap terpusat setelah diperkecil,
        // karena layout menempatkan kotak virtual di tengah container.
        binding.webView.pivotX = virtualWidth / 2f
        binding.webView.pivotY = virtualHeight / 2f
        binding.webView.scaleX = skala
        binding.webView.scaleY = skala
        binding.webView.layoutParams = FrameLayout.LayoutParams(
            virtualWidth,
            virtualHeight,
            Gravity.CENTER,
        )
    }

    /**
     * Membaca angka dari halaman yang sedang terbuka lalu menampilkannya apa
     * adanya. Nilai dikembalikan WebView sebagai string JSON, jadi tanda kutipnya
     * dilepas dulu sebelum ditampilkan.
     */
    private fun showDiagnostics() {
        binding.webView.evaluateJavascript(DesktopSpoof.diagnosticsScript()) { hasil ->
            val teks = runCatching {
                JSONTokener(hasil).nextValue() as? String
            }.getOrNull() ?: hasil

            AlertDialog.Builder(this)
                .setTitle(R.string.diagnostics_title)
                .setMessage(teks)
                .setPositiveButton(R.string.action_close, null)
                .show()
        }
    }

    private fun syncHistory() {
        viewModel.onHistoryChanged(binding.webView.canGoBack(), binding.webView.canGoForward())
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.addressBar.windowToken, 0)
    }

    // Activity tidak dibuat ulang saat perangkat diputar, jadi kotak harus
    // dihitung ulang di sini. post dipakai karena ukuran container belum final
    // pada saat callback ini berjalan.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.viewportContainer.post { applyVirtualScreen() }
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        // WebView harus dilepas dari induknya sebelum destroy, kalau tidak
        // proses render tetap hidup dan meninggalkan kebocoran memori.
        binding.viewportContainer.removeView(binding.webView)
        binding.webView.destroy()
        super.onDestroy()
    }
}
