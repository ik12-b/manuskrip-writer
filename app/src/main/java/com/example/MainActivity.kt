package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.ManuScribeApp
import com.example.ui.ManuscriptViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ManuscriptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() HARUS dipanggil sebelum super.onCreate() (persyaratan
        // library core-splashscreen), dan sebelum setContent().
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Splash tetap tampil sampai query pertama ke database selesai (viewModel.isReady),
        // bukan cuma splash kosmetik yang hilang di waktu tetap tanpa peduli data siap
        // atau belum. Lambda ini dipanggil berulang oleh sistem sampai return false.
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ManuScribeApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
