package com.chantalbortolussi.tattooapp.ui.screens

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chantalbortolussi.tattooapp.receiver.AftercareReceiver
import com.chantalbortolussi.tattooapp.ui.theme.GoldAccent
import com.chantalbortolussi.tattooapp.ui.theme.CharcoalBackground
import com.chantalbortolussi.tattooapp.ui.theme.CardBackground
import com.chantalbortolussi.tattooapp.ui.theme.TextMuted
import com.chantalbortolussi.tattooapp.ui.theme.TextPrimary
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun DiaryView() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("chantal_diary_prefs", Context.MODE_PRIVATE) }

    // AppStorage equivalents using Compose mutable states backed by SharedPreferences
    var hasActiveDiary by remember { mutableStateOf(sharedPrefs.getBoolean("hasActiveDiary", false)) }
    var tattooName by remember { mutableStateOf(sharedPrefs.getString("tattooName", "") ?: "") }
    var tattooStartDate by remember { mutableStateOf(sharedPrefs.getLong("tattooStartDate", 0L)) }
    var notificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("notificationsEnabled", false)) }
    
    // Set of completed tasks stored as comma separated string "Day_TaskIndex" (e.g. "3_1,5_2")
    var completedTasksSet by remember {
        mutableStateOf(
            sharedPrefs.getString("completedTasks", "")
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toSet() ?: emptySet()
        )
    }

    // Temp form states
    var inputName by remember { mutableStateOf("") }
    var calendar = remember { Calendar.getInstance() }
    var inputDateMillis by remember { mutableStateOf(calendar.timeInMillis) }
    var inputDateText by remember { mutableStateOf("Oggi") }

    fun updatePrefs(block: SharedPreferences.Editor.() -> Unit) {
        val editor = sharedPrefs.edit()
        editor.block()
        editor.apply()
    }

    fun startDiary() {
        if (inputName.trim().isEmpty()) return
        tattooName = inputName.trim()
        tattooStartDate = inputDateMillis
        hasActiveDiary = true
        completedTasksSet = emptySet()

        updatePrefs {
            putBoolean("hasActiveDiary", true)
            putString("tattooName", tattooName)
            putLong("tattooStartDate", tattooStartDate)
            putString("completedTasks", "")
        }
    }

    fun stopDiary() {
        hasActiveDiary = false
        tattooName = ""
        tattooStartDate = 0L
        completedTasksSet = emptySet()
        notificationsEnabled = false

        updatePrefs {
            putBoolean("hasActiveDiary", false)
            putString("tattooName", "")
            putLong("tattooStartDate", 0L)
            putString("completedTasks", "")
            putBoolean("notificationsEnabled", false)
        }
        cancelAllReminders(context)
    }

    fun toggleTask(day: Int, taskIndex: Int) {
        val key = "${day}_$taskIndex"
        val newSet = if (completedTasksSet.contains(key)) {
            completedTasksSet - key
        } else {
            completedTasksSet + key
        }
        completedTasksSet = newSet
        updatePrefs {
            putString("completedTasks", newSet.joinToString(","))
        }
    }

    fun handleNotifications(enabled: Boolean) {
        if (enabled) {
            // Register notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Request Permission is handled on MainActivity side or simple prompt
                Toast.makeText(context, "Per favore, acconsenti alle notifiche quando richiesto dal sistema.", Toast.LENGTH_SHORT).show()
            }
            scheduleDailyReminders(context)
            notificationsEnabled = true
            updatePrefs { putBoolean("notificationsEnabled", true) }
        } else {
            // Unregister notifications
            cancelAllReminders(context)
            notificationsEnabled = false
            updatePrefs { putBoolean("notificationsEnabled", false) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        if (!hasActiveDiary) {
            // SETUP DIARY VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                // Header
                Text(
                    text = "ASSISTENTE DI GUARIGIONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "REGISTRA IL TUO DIARIO",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Segui giorno per giorno il processo di guarigione del tuo nuovo tatuaggio con i consigli personalizzati di Chantal e una comoda checklist quotidiana.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Setup Card Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Tattoo Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "NOME DEL TATUAGGIO *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("es. Rosa sul braccio, Serpente, Lotus...", color = Color.Gray.copy(alpha = 0.6f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CharcoalBackground,
                                unfocusedContainerColor = CharcoalBackground,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.1f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    // Start Date picker
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DATA DI ESECUZIONE *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalBackground)
                                .border(1.dp, GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .clickable {
                                    val datePicker = DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectCalendar = Calendar.getInstance()
                                            selectCalendar.set(year, month, dayOfMonth)
                                            inputDateMillis = selectCalendar.timeInMillis
                                            
                                            // Format text
                                            val today = Calendar.getInstance()
                                            if (today.get(Calendar.YEAR) == year &&
                                                today.get(Calendar.MONTH) == month &&
                                                today.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                                                inputDateText = "Oggi"
                                            } else {
                                                inputDateText = "$dayOfMonth/${month + 1}/$year"
                                            }
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    datePicker.show()
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = inputDateText, color = TextPrimary, fontSize = 15.sp)
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Scegli Data", tint = GoldAccent)
                        }
                    }

                    // Start Button
                    Button(
                        onClick = { startDiary() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 10.dp),
                        enabled = inputName.trim().isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            disabledContainerColor = GoldAccent.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("INIZIA DIARIO", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
        } else {
            // ACTIVE DIARY DASHBOARD
            val daysDiff = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - tattooStartDate) + 1
            val currentDay = Math.min(Math.max(1, daysDiff.toInt()), 15) // Clamped between 1 and 15

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 30.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                // Dashboard Header Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIARIO DI GUARIGIONE",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tattooName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp
                        )
                    }

                    // Stop / Delete button
                    IconButton(
                        onClick = { stopDiary() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Cancella Diario", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }

                // Progress dashboard widget (Circular ring + Text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBackground)
                        .border(1.dp, GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Circular progress Canvas ring
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progressSweep = if (currentDay >= 14) 360f else (currentDay / 14f) * 360f
                        Canvas(modifier = Modifier.size(80.dp)) {
                            // Grey track ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Gold progress ring
                            drawArc(
                                color = GoldAccent,
                                startAngle = -90f,
                                sweepAngle = progressSweep,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (currentDay >= 14) "COMPLETATO" else "$currentDay",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (currentDay >= 14) 10.sp else 20.sp,
                                letterSpacing = if (currentDay >= 14) 0.5.sp else 0.sp
                            )
                            if (currentDay < 14) {
                                Text(text = "di 14", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentDay >= 14) "GUARIGIONE COMPLETATA!" else "PROCESSO DI RECUPERO",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentDay >= 14) 
                                "Superficie guarita al 100%. Ricordati sempre la protezione solare." 
                                else "Sei nella fase più importante per fissare i dettagli sotto pelle.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Chantal's personalized Tips Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GoldAccent.copy(alpha = 0.08f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Tip", tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = "CONSIGLIO DEL GIORNO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = getChantalDailyTip(currentDay),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Daily tasks checklist
                if (currentDay < 14) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "CHECKLIST QUOTIDIANA (GIORNO $currentDay)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )

                        val dayTasks = listOf(
                            Pair("Lavaggio Mattutino 🧼", "Lava delicatamente con sapone neutro e acqua tiepida, poi asciuga tamponando."),
                            Pair("Crema Aftercare (Pranzo) 🧴", "Applica un velo sottile di crema specifica dopo pranzo per idratare."),
                            Pair("Crema Aftercare (Pomeriggio) 🧴", "Idrata se avverti la pelle secca o che tira."),
                            Pair("Lavaggio e Crema Serale ✨", "Esegui il lavaggio serale e stendi un leggero velo di crema prima di dormire.")
                        )

                        dayTasks.forEachIndexed { index, task ->
                            val taskKey = "${currentDay}_$index"
                            val isCompleted = completedTasksSet.contains(taskKey)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground.copy(alpha = if (isCompleted) 0.4f else 1.0f))
                                    .border(
                                        1.dp, 
                                        if (isCompleted) Color.Transparent else GoldAccent.copy(alpha = 0.15f), 
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { toggleTask(currentDay, index) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Custom Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCompleted) GoldAccent else Color.Transparent)
                                        .border(1.5.dp, GoldAccent, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Fatto",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.first,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCompleted) TextMuted else TextPrimary,
                                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.second,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Push notifications prompt toggler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Promemoria", tint = GoldAccent)
                        Column {
                            Text(text = "PROMEMORIA GIORNALIERI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Ricevi notifiche per i lavaggi e crema", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { handleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldAccent,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CardBackground
                        )
                    )
                }
            }
        }
    }
}

// Return daily customized suggestions matching the original iOS business logic
private fun getChantalDailyTip(day: Int): String {
    return when (day) {
        1 -> "È il giorno di inizio! Tieni protetto il tatuaggio e lavalo solo prima di andare a dormire usando sapone neutro delicato. Asciugalo tamponando senza strofinare e applica un velo sottilissimo di crema aftercare."
        2, 3 -> "La pelle produce ancora secrezioni ed è normale vedere sbavature scure. Continua a lavare 2-3 volte al giorno, applica sempre poca crema massaggiando bene per farla assorbire completamente. Non lasciare accumuli di crema."
        4, 5, 6, 7 -> "Il tatuaggio inizierà a spellarsi e potrebbe produrre un forte prurito. È del tutto normale! Non grattare e assolutamente non tirare via le pellicine: rischieresti di rimuovere il pigmento e rovinare le linee. Continua ad idratare ogni volta che senti tirare."
        in 8..14 -> "La guarigione superficiale è a buon punto, ma sotto pelle il tessuto è ancora molto sensibile. Evita bagni caldi, saune, terme o nuotate in piscina/mare. Evita l'esposizione diretta ai raggi solari e continua ad applicare la crema 2-3 volte al giorno."
        else -> "La guarigione primaria è completata! Ora puoi passare a una normale crema idratante. Ricorda sempre la regola d'oro di Chantal: applica sempre la protezione solare 50+ quando esponi il tatuaggio al sole. È l'unico segreto per mantenerlo nitido e splendido negli anni."
    }
}

// Alarm Scheduling logic for daily reminders
private fun scheduleDailyReminders(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val times = listOf(
        Triple(10, 0, 1),  // 10:00 AM
        Triple(15, 0, 2),  // 03:00 PM
        Triple(21, 0, 3)   // 09:00 PM
    )

    for (time in times) {
        val intent = Intent(context, AftercareReceiver::class.java).apply {
            action = "com.chantalbortolussi.tattooapp.ACTION_AFTERCARE_ALARM"
            putExtra("reminder_id", time.third)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.third,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1) // Schedule for tomorrow if hour passed
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}

private fun cancelAllReminders(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val ids = listOf(1, 2, 3)
    for (id in ids) {
        val intent = Intent(context, AftercareReceiver::class.java).apply {
            action = "com.chantalbortolussi.tattooapp.ACTION_AFTERCARE_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
