package com.faiz.kalbrowser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.faiz.kalbrowser.databinding.ActivityMainBinding
import com.faiz.kalbrowser.desktop.DesktopMode
import com.faiz.kalbrowser.ui.BrowserCommand
import com.faiz.kalbrowser.ui.BrowserUiState
import com.faiz.kalbrowser.ui.BrowserViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: BrowserViewModel by viewModels()
    private lateinit var desktopMode: DesktopMode

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
    }

    private fun setupWebView() {
        // Semua penyetelan WebSettings dan penyamaran desktop ada di DesktopMode,
        // supaya Activity tinggal mengurus tampilan dan navigasi.
        desktopMode = DesktopMode(binding.webView).also { it.apply() }

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean = forwardNonHttpScheme(request.url)

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                viewModel.onPageStarted(url.orEmpty())
                syncHistory()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                viewModel.onPageFinished(url.orEmpty())
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
                    }
                    syncHistory()
                }
            }
        }
    }

    private fun syncHistory() {
        viewModel.onHistoryChanged(binding.webView.canGoBack(), binding.webView.canGoForward())
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.addressBar.windowToken, 0)
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
