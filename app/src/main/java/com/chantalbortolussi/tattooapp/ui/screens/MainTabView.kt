package com.chantalbortolussi.tattooapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chantalbortolussi.tattooapp.ui.theme.GoldAccent
import com.chantalbortolussi.tattooapp.ui.theme.CharcoalBackground
import com.chantalbortolussi.tattooapp.ui.theme.CardBackground
import com.chantalbortolussi.tattooapp.ui.theme.TextMuted

// Helper structure to represent Tab details
private data class TabItem(
    val title: String,
    val icon: ImageVector,
    val iconResId: Int? = null // For custom drawables
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabView() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val context = LocalContext.current

    val tabs = listOf(
        TabItem("Galleria", Icons.Default.Home),
        TabItem("Prova AR", Icons.Default.PlayArrow), // Will look like a try icon
        TabItem("Prenota", Icons.Default.DateRange),
        TabItem("Diario", Icons.Default.List)
    )

    // Pulsing animation for floating WhatsApp button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalBackground,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                color = if (selectedTab == index) GoldAccent else TextMuted
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) GoldAccent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CardBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CharcoalBackground)
                .padding(innerPadding)
        ) {
            // Screen content switcher
            when (selectedTab) {
                0 -> HomeView()
                1 -> TryOnView()
                2 -> BookingView()
                3 -> DiaryView()
            }

            // Floating WhatsApp Button: overlay on all screens except the Camera AR view
            if (selectedTab != 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 24.dp)
                        .scale(pulseScale)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366)) // Original WhatsApp Green
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/393245462726")
                            )
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Custom Draw standard telephone/chat icon as a placeholder of WhatsApp bubble
                    Icon(
                        imageVector = Icons.Default.DateRange, // Simple bubble-phone placeholder
                        contentDescription = "WhatsApp Chantal",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
