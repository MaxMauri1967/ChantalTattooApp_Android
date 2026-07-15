package com.chantalbortolussi.tattooapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.chantalbortolussi.tattooapp.ui.screens.MainTabView
import com.chantalbortolussi.tattooapp.ui.theme.ChantalTattooAppTheme

class MainActivity : ComponentActivity() {

    // Request permissions for Android 13+ (Post Notifications)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission response received, app handles state dynamically
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic permission requesting for local notifications on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ChantalTattooAppTheme {
                MainTabView()
            }
        }
    }
}
