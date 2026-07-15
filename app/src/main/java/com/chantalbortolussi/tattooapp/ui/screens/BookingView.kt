package com.chantalbortolussi.tattooapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chantalbortolussi.tattooapp.network.BookingRequest
import com.chantalbortolussi.tattooapp.network.NetworkClient
import com.chantalbortolussi.tattooapp.ui.theme.GoldAccent
import com.chantalbortolussi.tattooapp.ui.theme.CharcoalBackground
import com.chantalbortolussi.tattooapp.ui.theme.CardBackground
import com.chantalbortolussi.tattooapp.ui.theme.TextMuted
import com.chantalbortolussi.tattooapp.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingView() {
    // Form States
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Fine Line") }
    var description by remember { mutableStateOf("") }

    // Dropdown state
    var isStyleMenuExpanded by remember { mutableStateOf(false) }
    val styles = listOf("Fine Line", "Ornamental", "Realistic", "Dotwork & Mandala", "Micro Realism", "Altro")

    // UI Feedback States
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Email Validation Regex
    val emailPattern = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}".toRegex()
    val isEmailValid = emailPattern.matches(email)
    
    // Total Validation rule
    val isValid = name.trim().isNotEmpty() && isEmailValid && description.trim().isNotEmpty()

    fun resetForm() {
        name = ""
        email = ""
        phone = ""
        selectedStyle = "Fine Line"
        description = ""
    }

    fun submitBooking() {
        if (!isValid) return
        focusManager.clearFocus()
        coroutineScope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                val request = BookingRequest(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    style = selectedStyle,
                    description = description.trim()
                )
                
                val response = NetworkClient.apiService.submitBooking(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    submitSuccess = true
                    resetForm()
                } else {
                    val serverError = response.body()?.error ?: "Errore sconosciuto del server"
                    errorMessage = "Errore: $serverError"
                }
            } catch (e: Exception) {
                errorMessage = "Errore di connessione. Controlla la tua rete e riprova."
            } finally {
                isSubmitting = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        // Decorative background gold glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 12.dp)
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
                    text = "RICHIESTA APPUNTAMENTO",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp
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

            Text(
                text = "Compila il modulo per inviare la tua idea a Chantal. Ti risponderà via email o telefono per concordare i dettagli, la data e il preventivo.",
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // FORM CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Name Field
                CustomInputField(
                    label = "NOME COMPLETO *",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Inserisci il tuo nome e cognome",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Email Field
                CustomInputField(
                    label = "INDIRIZZO EMAIL *",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "La tua email migliore",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    isError = email.isNotEmpty() && !isEmailValid
                )

                // Phone Field
                CustomInputField(
                    label = "TELEFONO",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Il tuo numero (opzionale)",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                // Dropdown Style Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "STILE PREFERITO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .border(1.dp, GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { isStyleMenuExpanded = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star, // Style brush icon placeholder
                                    contentDescription = "Stile",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = selectedStyle,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            Icon(
                                imageVector = if (isStyleMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Scegli",
                                tint = GoldAccent
                            )
                        }

                        DropdownMenu(
                            expanded = isStyleMenuExpanded,
                            onDismissRequest = { isStyleMenuExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .background(CardBackground)
                                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        ) {
                            styles.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style, color = TextPrimary) },
                                    onClick = {
                                        selectedStyle = style
                                        isStyleMenuExpanded = false
                                    },
                                    trailingIcon = {
                                        if (selectedStyle == style) {
                                            Icon(Icons.Default.Check, contentDescription = "Selezionato", tint = GoldAccent)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Description Text Area Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LA TUA IDEA DI TATUAGGIO *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                text = "Descrivi l'idea, la zona del corpo, la dimensione indicativa (es. 10cm) ed eventuali dettagli importanti...",
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = GoldAccent.copy(alpha = 0.15f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = false,
                        maxLines = 8
                    )
                }

                // Error Message block
                if (errorMessage != null) {
                    Row(
                        modifier = Modifier.padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Errore",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }

                // Submit Button
                Button(
                    onClick = { submitBooking() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(top = 10.dp),
                    enabled = isValid && !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        disabledContainerColor = GoldAccent.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 10.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "INVIANDO...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        } else {
                            Text(
                                text = "INVIA RICHIESTA",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }

        // SUCCESS SCREEN OVERLAY
        AnimatedVisibility(
            visible = submitSuccess,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            SuccessOverlay(onDismiss = { submitSuccess = false })
        }
    }
}

// Reusable elegant form input field
@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = if (isError) Color.Red else GoldAccent,
                unfocusedBorderColor = if (isError) Color.Red else GoldAccent.copy(alpha = 0.15f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError
        )
    }
}

// Success full-screen Overlay card
@Composable
fun SuccessOverlay(onDismiss: () -> Void) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(35.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackground)
                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            // Animated Gold Checkmark ring
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(2.dp, GoldAccent.copy(alpha = 0.3f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(4.dp, GoldAccent, CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = GoldAccent,
                    modifier = Modifier.size(38.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "RICHIESTA INVIATA",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Chantal ha ricevuto la tua richiesta.",
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Chantal esaminerà la tua idea di tatuaggio e ti risponderà via email o telefono per concordare data, dettagli artistici e preventivo.\n\nGrazie per la tua fiducia!",
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
                    .padding(top = 15.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text(
                    text = "CONTINUA",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
