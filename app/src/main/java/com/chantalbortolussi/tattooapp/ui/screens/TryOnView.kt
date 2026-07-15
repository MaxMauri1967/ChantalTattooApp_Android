package com.chantalbortolussi.tattooapp.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.chantalbortolussi.tattooapp.ui.components.TattooShapes
import com.chantalbortolussi.tattooapp.ui.theme.GoldAccent
import com.chantalbortolussi.tattooapp.ui.theme.CharcoalBackground
import com.chantalbortolussi.tattooapp.ui.theme.CardBackground
import com.chantalbortolussi.tattooapp.ui.theme.TextMuted
import com.chantalbortolussi.tattooapp.ui.theme.TextPrimary
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun TryOnView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera Permissions State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Tattoo Transformation States
    var selectedShapeIndex by remember { mutableStateOf(0) } // 0: Bussola, 1: Loto, 2: Luna, 3: Farfalla, 4: Ornamento
    val shapes = listOf("Bussola", "Loto", "Luna", "Farfalla", "Ornamento")

    var scale by remember { mutableStateOf(1.0f) }
    var rotation by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(0.8f) }
    var isFlipped by remember { mutableStateOf(false) }
    var isGoldColor by remember { mutableStateOf(true) }

    // Offset position for dragging
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Custom Background Photo states
    var uploadedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadedImageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                uploadedBitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Toast.makeText(context, "Errore caricamento foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dimensions of the rendering Canvas area
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // CameraX helper variables
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    // Reset all adjustments
    fun resetTransformations() {
        scale = 1.0f
        rotation = 0f
        opacity = 0.8f
        isFlipped = false
        isGoldColor = true
        offsetX = 0f
        offsetY = 0f
    }

    // Custom capture compositing and saving to public MediaStore gallery
    fun captureAndSaveComposite() {
        // Trigger haptic buzz shutter feedback
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(100L)
        } catch (e: Exception) {
            // Haptics unavailable, ignore
        }

        // 1. Resolve Background frame as Bitmap
        val baseBitmap: Bitmap = if (uploadedBitmap != null) {
            uploadedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            // Capture current frame from live Camera preview stream!
            previewView?.bitmap?.copy(Bitmap.Config.ARGB_8888, true)
                ?: Bitmap.createBitmap(canvasSize.width.coerceAtLeast(100), canvasSize.height.coerceAtLeast(100), Bitmap.Config.ARGB_8888)
        }

        // 2. Create Canvas wrapping base Bitmap to overlay the path
        val compositeBitmap = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
        val androidCanvas = AndroidCanvas(compositeBitmap)
        androidCanvas.drawBitmap(baseBitmap, 0f, 0f, null)

        // Calculate scaling ratio if canvas display size differs from capture bitmap sizes
        val ratioX = baseBitmap.width.toFloat() / canvasSize.width.toFloat().coerceAtLeast(1f)
        val ratioY = baseBitmap.height.toFloat() / canvasSize.height.toFloat().coerceAtLeast(1f)

        // Setup draw shape path
        val shapeWidth = 220.dp.value * 3f // default bounding box size
        val shapeHeight = 220.dp.value * 3f
        
        val path = when (selectedShapeIndex) {
            0 -> TattooShapes.getCompassPath(shapeWidth, shapeHeight)
            1 -> TattooShapes.getLotusPath(shapeWidth, shapeHeight)
            2 -> TattooShapes.getMoonPath(shapeWidth, shapeHeight)
            3 -> TattooShapes.getButterflyPath(shapeWidth, shapeHeight)
            else -> TattooShapes.getOrnamentoGeometricoPath(shapeWidth, shapeHeight)
        }

        // Paint configuration
        val paint = AndroidPaint().apply {
            color = if (isGoldColor) android.graphics.Color.parseColor("#BFA37E") else android.graphics.Color.BLACK
            style = AndroidPaint.Style.STROKE
            strokeWidth = 3.dp.value * ratioX.coerceAtLeast(1f)
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            alpha = (opacity * 255).toInt()
        }

        // Apply same transformations to Android matrix
        val matrix = AndroidMatrix().apply {
            // Apply scale
            postScale(
                if (isFlipped) -scale else scale,
                scale,
                shapeWidth / 2f,
                shapeHeight / 2f
            )
            // Apply rotation
            postRotate(rotation, shapeWidth / 2f, shapeHeight / 2f)
            // Translate to center + user drag offsets
            val centerX = (baseBitmap.width / 2f) - (shapeWidth / 2f) * ratioX
            val centerY = (baseBitmap.height / 2f) - (shapeHeight / 2f) * ratioY
            postTranslate(
                centerX + offsetX * ratioX,
                centerY + offsetY * ratioY
            )
        }

        val androidPath = path.asAndroidPath()
        androidPath.transform(matrix)
        androidCanvas.drawPath(androidPath, paint)

        // 3. Save Bitmap to Android Public Pictures Gallery via MediaStore
        val filename = "ChantalTattoo_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ChantalTattoo")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (imageUri != null) {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
                if (outputStream != null) {
                    compositeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }

                Toast.makeText(context, "Fotomontaggio salvato in Galleria!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Errore nel salvataggio dell'immagine.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        // CAMERA / UPLOADED BACKGROUND BOX
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
        ) {
            if (uploadedImageUri != null) {
                // PHOTO BACKGROUND VIEW
                AsyncImage(
                    model = uploadedImageUri,
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (hasCameraPermission) {
                // CAMERA BACKGROUND VIEW (CameraX bound)
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                            } catch (exc: Exception) {
                                // Camera binding failed
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // FALLBACK NO PERMISSION SCREEN
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "La fotocamera non è abilitata.", color = TextMuted)
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("Carica una foto", color = Color.Black)
                        }
                    }
                }
            }

            // TRANSPARENT DRAGGABLE DRAW OVERLAY CANVAS
            if (canvasSize != IntSize.Zero) {
                val shapeWidth = 220.dp.value * 3f
                val shapeHeight = 220.dp.value * 3f
                val path = when (selectedShapeIndex) {
                    0 -> TattooShapes.getCompassPath(shapeWidth, shapeHeight)
                    1 -> TattooShapes.getLotusPath(shapeWidth, shapeHeight)
                    2 -> TattooShapes.getMoonPath(shapeWidth, shapeHeight)
                    3 -> TattooShapes.getButterflyPath(shapeWidth, shapeHeight)
                    else -> TattooShapes.getOrnamentoGeometricoPath(shapeWidth, shapeHeight)
                }

                val currentPathColor = if (isGoldColor) GoldAccent else Color.Black

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    val canvasCenterX = size.width / 2f
                    val canvasCenterY = size.height / 2f

                    withTransform({
                        // Apply drag offset translation
                        translate(offsetX, offsetY)
                        // Translate coordinate origin to center of bounding box to rotate/scale symmetrically
                        translate(canvasCenterX - shapeWidth / 2f, canvasCenterY - shapeHeight / 2f)
                        // Apply rotation
                        rotate(rotation, pivot = androidx.compose.ui.geometry.Offset(shapeWidth / 2f, shapeHeight / 2f))
                        // Apply scale (with flip mapping)
                        scale(
                            scaleX = if (isFlipped) -scale else scale,
                            scaleY = scale,
                            pivot = androidx.compose.ui.geometry.Offset(shapeWidth / 2f, shapeHeight / 2f)
                        )
                    }) {
                        drawPath(
                            path = path,
                            color = currentPathColor,
                            alpha = opacity,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }

        // FLOATING ACTION OVERLAY CONTROLS (Top right and Bottom slider container)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP CONTROLS (Camera toggle / Reset / Gallery upload)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo upload picker
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Carica Foto", tint = GoldAccent)
                }

                // Reset transformations
                IconButton(
                    onClick = { resetTransformations() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = GoldAccent)
                }

                // Clear loaded background image, fall back to Camera feed
                if (uploadedImageUri != null) {
                    IconButton(
                        onClick = {
                            uploadedImageUri = null
                            uploadedBitmap = null
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Usa Camera", tint = GoldAccent)
                    }
                }
            }

            // BOTTOM CONTROL PALETTE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tattoo Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shapes.forEachIndexed { index, shape ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedShapeIndex == index) GoldAccent else CardBackground)
                                .clickable { selectedShapeIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedShapeIndex == index) Color.Black else TextMuted
                            )
                        }
                    }
                }

                // 2. Adjustments Slider container (Collapsable or simple Scroll list)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Scale Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "SCALA", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 0.3f..2.5f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                        )
                    }

                    // Rotation Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "ROTAZIONE", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        Slider(
                            value = rotation,
                            onValueChange = { rotation = it },
                            valueRange = 0f..360f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                        )
                    }

                    // Opacity Slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "OPACITÀ", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.1f..1.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                        )
                    }
                }

                // 3. Shutter, Flip, and Color Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mirror/Flip Toggle Button
                    IconButton(
                        onClick = { isFlipped = !isFlipped },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // Flip representation
                            contentDescription = "Specchia",
                            tint = if (isFlipped) GoldAccent else TextMuted
                        )
                    }

                    // CAMERA CAPTURE/SHUTTER BUTTON
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { captureAndSaveComposite() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.Black, CircleShape)
                                .background(Color.White)
                        )
                    }

                    // Color selection (Gold vs Black) Toggle
                    IconButton(
                        onClick = { isGoldColor = !isGoldColor },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite, // Color dot representation
                            contentDescription = "Colore",
                            tint = if (isGoldColor) GoldAccent else Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
