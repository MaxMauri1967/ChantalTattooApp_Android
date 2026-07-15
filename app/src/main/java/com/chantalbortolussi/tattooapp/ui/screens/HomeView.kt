package com.chantalbortolussi.tattooapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chantalbortolussi.tattooapp.model.Gallery
import com.chantalbortolussi.tattooapp.model.GalleryImage
import com.chantalbortolussi.tattooapp.network.NetworkClient
import com.chantalbortolussi.tattooapp.ui.theme.GoldAccent
import com.chantalbortolussi.tattooapp.ui.theme.CharcoalBackground
import com.chantalbortolussi.tattooapp.ui.theme.CardBackground
import com.chantalbortolussi.tattooapp.ui.theme.TextMuted
import com.chantalbortolussi.tattooapp.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun HomeView() {
    var galleries by remember { mutableStateOf<List<Gallery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Navigation/Lightbox States
    var selectedGallery by remember { mutableStateOf<Gallery?>(null) }
    var lightboxImageIndex by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch Galleries from backend API
    fun loadGalleries() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = NetworkClient.apiService.getGalleries()
                if (response.isSuccessful && response.body()?.success == true) {
                    galleries = response.body()?.galleries ?: emptyList()
                } else {
                    errorMessage = "Errore nel caricamento delle gallerie"
                }
            } catch (e: Exception) {
                errorMessage = "Errore di rete: Controlla la tua connessione."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGalleries()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        if (selectedGallery == null) {
            // MAIN FEED VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp)
            ) {
                // Header (Pixel-perfect copy of Chantal brand style)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "CHANTAL BORTOLUSSI",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "STUDIO TATUAGGI",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“...come una seconda pelle.”",
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = GoldAccent.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Divider(
                        color = GoldAccent.copy(alpha = 0.3f),
                        modifier = Modifier.width(50.dp),
                        thickness = 1.dp
                    )
                }

                // API Status handling
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { loadGalleries() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("RIPROVA", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // GALLERIES GRID
                    if (galleries.isNotEmpty()) {
                        // Section Header
                        Text(
                            text = "I MIEI LAVORI",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )

                        val firstGallery = galleries.first()
                        val remainingGalleries = galleries.drop(1)

                        // 1. Hero Card (Full-width for first gallery, e.g. Fine Line)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedGallery = firstGallery },
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val coverUrl = firstGallery.images.firstOrNull()?.url
                                if (coverUrl != null) {
                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = firstGallery.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Vignette Gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                )
                                // Category Title
                                Text(
                                    text = firstGallery.title.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 18.sp,
                                    color = TextPrimary,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                )
                            }
                        }

                        // 2. 2x2 Grid for other galleries
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            val chunked = remainingGalleries.chunked(2)
                            for (row in chunked) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (gallery in row) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(130.dp)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(CardBackground)
                                                .clickable { selectedGallery = gallery }
                                        ) {
                                            val coverUrl = gallery.images.firstOrNull()?.url
                                            if (coverUrl != null) {
                                                AsyncImage(
                                                    model = coverUrl,
                                                    contentDescription = gallery.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                        )
                                                    )
                                            )
                                            Text(
                                                text = gallery.title.uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                    if (row.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // DETAILED SUBCATEGORY VIEW (3-column grid)
            val currentGallery = selectedGallery!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CharcoalBackground)
            ) {
                // Category Header navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedGallery = null }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Indietro",
                            tint = GoldAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentGallery.title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                }

                if (currentGallery.images.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessuna foto disponibile in questa galleria.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(currentGallery.images.size) { index ->
                            val image = currentGallery.images[index]
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackground)
                                    .clickable { lightboxImageIndex = index }
                            ) {
                                AsyncImage(
                                    model = image.url,
                                    contentDescription = image.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // FULLSCREEN PAGE-VIEW LIGHTBOX OVERLAY
        AnimatedVisibility(
            visible = lightboxImageIndex != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val currentGallery = selectedGallery
            if (currentGallery != null && lightboxImageIndex != null) {
                LightboxOverlay(
                    images = currentGallery.images,
                    startIndex = lightboxImageIndex!!,
                    onClose = { lightboxImageIndex = null }
                )
            }
        }
    }
}

// Lightbox component rendering a horizontal swipable page viewer
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LightboxOverlay(
    images: List<GalleryImage>,
    startIndex: Int,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { images.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Horizontal swipable images
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = images[page].url,
                    contentDescription = images[page].name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Top Actions (Close button & image index)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 30.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Chiudi",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Image Label
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .padding(30.dp)
                .align(Alignment.BottomCenter)
        ) {
            val label = images[pagerState.currentPage].name
            if (label.isNotEmpty()) {
                Text(
                    text = label.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
