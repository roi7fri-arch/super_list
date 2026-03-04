package com.superlist

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.Normalizer

@Composable
fun SuperListApp() {
    var screen by remember { mutableStateOf("voice") }
    var householdCode by remember { mutableStateOf(generateHouseholdCode()) }
    var familyMembers by remember { mutableStateOf(1) }
    var syncConnected by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf("לא מחובר למשפחה") }
    val items = remember {
        mutableStateListOf(
            GroceryItem(name = "חלב", quantity = 1),
            GroceryItem(name = "לחם", quantity = 1),
        )
    }

    val addParsedItem: (ParsedVoiceItem) -> Unit = addParsed@{ parsed ->
        if (parsed.name.isBlank()) {
            return@addParsed
        }

        val existingIndex = items.indexOfFirst { normalizeItemKey(it.name) == normalizeItemKey(parsed.name) }
        if (existingIndex >= 0) {
            val current = items[existingIndex]
            items[existingIndex] = current.copy(quantity = current.quantity + parsed.quantity)
        } else {
            items += GroceryItem(name = parsed.name, quantity = parsed.quantity)
        }

        if (syncConnected) {
            syncStatus = "מסונכרן למשפחה: ${parsed.name} ×${parsed.quantity}"
        }
    }

    val addFromTranscript: (String) -> Unit = add@{ transcript ->
        val parsed = parseHebrewTranscript(transcript)
        if (parsed.name.isBlank()) {
            return@add
        }
        addParsedItem(parsed)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (screen == "list") {
                    Button(onClick = { screen = "voice" }, modifier = Modifier.height(36.dp)) {
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Text(
                    text = "Super List",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                if (screen == "voice") {
                    Button(onClick = { screen = "list" }, modifier = Modifier.height(36.dp)) {
                        Text("📝")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (screen) {
                "voice" -> VoiceTab(
                    onAdd = addFromTranscript,
                    onAddParsed = addParsedItem,
                )

                else -> ListTab(
                    items = items,
                    householdCode = householdCode,
                    familyMembers = familyMembers,
                    syncStatus = syncStatus,
                    onJoinHousehold = { enteredCode ->
                        if (enteredCode.isNotBlank()) {
                            householdCode = enteredCode.trim().uppercase()
                            syncConnected = true
                            familyMembers = maxOf(familyMembers, 2)
                            syncStatus = "מחובר למשפחה בקוד: $householdCode"
                        }
                    },
                    onRemoveSelected = { selected ->
                        selected.sortedDescending().forEach { index ->
                            if (index in items.indices) {
                                items.removeAt(index)
                            }
                        }
                        if (syncConnected && selected.isNotEmpty()) {
                            syncStatus = "סנכרון מחיקה נשלח ל-$familyMembers חברי משפחה"
                        }
                    },
                    onClearAll = {
                        items.clear()
                        if (syncConnected) {
                            syncStatus = "הרשימה כולה נוקתה וסונכרנה למשפחה"
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun VoiceTab(onAdd: (String) -> Unit, onAddParsed: (ParsedVoiceItem) -> Unit) {
    val context = LocalContext.current
    var draft by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("לחץ על הכפתור האדום להתחלה/עצירה של מצב רציף") }
    var isHolding by remember { mutableStateOf(false) }
    var isRecognizing by remember { mutableStateOf(false) }
    var pendingPermissionStart by remember { mutableStateOf(false) }
    var continuousListening by remember { mutableStateOf(false) }
    var commitPendingBatchOnNextResult by remember { mutableStateOf(false) }
    var pendingTranscript by remember { mutableStateOf<String?>(null) }
    var pendingParsedName by remember { mutableStateOf("") }
    var pendingParsedQuantity by remember { mutableStateOf(1) }
    val pendingBatch = remember { mutableStateListOf<ParsedVoiceItem>() }
    val thresholdMs = 300L
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80) }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    val recognitionIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "he-IL")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
    }

    fun beginRecognition() {
        if (speechRecognizer == null) {
            statusText = "זיהוי קול אינו זמין במכשיר זה"
            return
        }
        runCatching {
            speechRecognizer.startListening(recognitionIntent)
            isRecognizing = true
            statusText = "מצב רציף פעיל — דבר ברצף"
        }.onFailure {
            isRecognizing = false
            statusText = "שגיאת התחלה בזיהוי קול"
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            statusText = "אישור מיקרופון התקבל"
            if (pendingPermissionStart) {
                beginRecognition()
            }
        } else {
            statusText = "נדרש אישור מיקרופון לזיהוי קול"
        }
        pendingPermissionStart = false
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    isRecognizing = false
                }

                override fun onError(error: Int) {
                    isRecognizing = false
                    if (continuousListening && isHolding) {
                        statusText = "מצב רציף: ממשיך להאזין..."
                        beginRecognition()
                    } else if (commitPendingBatchOnNextResult) {
                        if (pendingBatch.isNotEmpty()) {
                            pendingBatch.forEach { parsed -> onAddParsed(parsed) }
                            val count = pendingBatch.size
                            pendingBatch.clear()
                            statusText = "נוספו $count פריטים מהרצף"
                        } else {
                            statusText = "לא זוהה טקסט, נסה שוב"
                        }
                        commitPendingBatchOnNextResult = false
                    } else {
                        statusText = "זיהוי הקול נכשל, נסה שוב"
                    }
                }

                override fun onResults(results: Bundle?) {
                    isRecognizing = false
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()

                    if (spoken.isNotBlank()) {
                        val parsedItems = parseTranscriptItems(spoken)
                        draft = spoken
                        if (parsedItems.isEmpty()) {
                            pendingTranscript = null
                            statusText = "לא זוהה פריט ברור, נסה שוב"
                        } else {
                            if (continuousListening || commitPendingBatchOnNextResult) {
                                parsedItems.forEach { parsed -> mergeIntoBatch(pendingBatch, parsed) }
                                statusText = "מצב רציף: נקלטו ${parsedItems.size} פריטים"
                                if (isHolding) {
                                    beginRecognition()
                                } else {
                                    pendingBatch.forEach { parsed -> onAddParsed(parsed) }
                                    val count = pendingBatch.size
                                    pendingBatch.clear()
                                    statusText = "נוספו $count פריטים מהרצף"
                                    commitPendingBatchOnNextResult = false
                                }
                            } else {
                                val parsed = parsedItems.first()
                                pendingTranscript = spoken
                                pendingParsedName = parsed.name
                                pendingParsedQuantity = parsed.quantity
                                statusText = "פוענח פריט — אשר הוספה"
                            }
                        }
                    } else {
                        statusText = "לא זוהה טקסט, נסה שוב"
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        onDispose {
            runCatching {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                toneGenerator.release()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("דיבור רציף בזמן לחיצה")
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(144.dp)
                .clip(CircleShape)
                .background(if (isHolding || isRecognizing) Color(0xFFB71C1C) else Color(0xFFD32F2F))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)

                            val hasPermission = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED

                            continuousListening = true
                            commitPendingBatchOnNextResult = false
                            if (hasPermission) {
                                beginRecognition()
                            } else {
                                pendingPermissionStart = true
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                statusText = "מבקש אישור מיקרופון למצב רציף..."
                            }

                            val startedAt = SystemClock.elapsedRealtime()
                            val released = tryAwaitRelease()
                            val duration = SystemClock.elapsedRealtime() - startedAt
                            isHolding = false

                            if (released && duration < thresholdMs) {
                                runCatching {
                                    speechRecognizer?.cancel()
                                }
                                isRecognizing = false
                                continuousListening = false
                                commitPendingBatchOnNextResult = false
                                statusText = "לחיצה קצרה מדי — החזק לפחות 300ms"
                            } else if (released && duration >= thresholdMs) {
                                continuousListening = false
                                commitPendingBatchOnNextResult = true
                                runCatching { speechRecognizer?.stopListening() }
                                statusText = "מעבד דיבור..."
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("לחץ ודבר", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(statusText, textAlign = TextAlign.Center)

        if (pendingBatch.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("פריטים שנקלטו במצב רציף", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    pendingBatch.forEach {
                        Text("• ${it.name} ×${it.quantity}")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                pendingBatch.forEach { parsed -> onAddParsed(parsed) }
                                pendingBatch.clear()
                                statusText = "כל הפריטים נוספו לרשימה"
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("הוסף הכל")
                        }
                        Button(
                            onClick = {
                                pendingBatch.clear()
                                statusText = "רשימת מצב רציף נוקתה"
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("נקה")
                        }
                    }
                }
            }
        }

        if (pendingTranscript != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        text = "תצוגה לפני הוספה",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("פריט: $pendingParsedName")
                    Text("כמות: $pendingParsedQuantity")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                val transcript = pendingTranscript
                                if (!transcript.isNullOrBlank()) {
                                    onAdd(transcript)
                                    statusText = "נוסף: $pendingParsedName ×$pendingParsedQuantity"
                                }
                                pendingTranscript = null
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("אשר")
                        }
                        Button(
                            onClick = {
                                pendingTranscript = null
                                statusText = "בוטל — נסה שוב"
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("בטל")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                val item = if (draft.isBlank()) "פריט לדוגמה" else draft.trim()
                onAdd(item)
                pendingTranscript = null
                statusText = "נוסף ידנית: $item"
            }, modifier = Modifier.fillMaxWidth()) {
                Text("הוסף ידנית")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("פריט להוספה") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ListTab(
    items: List<GroceryItem>,
    householdCode: String,
    familyMembers: Int,
    syncStatus: String,
    onJoinHousehold: (String) -> Unit,
    onRemoveSelected: (List<Int>) -> Unit,
    onClearAll: () -> Unit,
) {
    val selected = remember { mutableStateListOf<Int>() }
    val scrollState = rememberScrollState()
    var joinCode by remember { mutableStateOf("") }

    fun toggleSelection(index: Int) {
        if (selected.contains(index)) {
            selected.remove(index)
        } else {
            selected.add(index)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("רשימת סופר משותפת", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("נבחרו: ${selected.size}", modifier = Modifier.align(Alignment.CenterVertically))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onRemoveSelected(selected.toList())
                        selected.clear()
                    },
                    enabled = selected.isNotEmpty() && items.isNotEmpty(),
                ) {
                    Text("אשר מחיקה")
                }
                Button(
                    onClick = {
                        onClearAll()
                        selected.clear()
                    },
                    enabled = items.isNotEmpty(),
                ) {
                    Text("נקה רשימה")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("שיתוף משפחתי", style = MaterialTheme.typography.titleSmall)
                Text("קוד הזמנה: $householdCode")
                Text("חברי משפחה מחוברים: $familyMembers")
                Text("מצב סנכרון: $syncStatus")
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it },
                    label = { Text("הכנס קוד משפחה") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { onJoinHousehold(joinCode) },
                    enabled = joinCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("הצטרף למשפחה וסנכרן")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            val lineColor = Color(0xFF9BB7D4)
            val noteColor = Color(0xFFFFF59D)
            val marginLineColor = Color(0xFFD46A6A)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .drawBehind {
                        drawRect(color = noteColor)

                        val lineGap = 36.dp.toPx()
                        val startY = 28.dp.toPx()
                        var y = startY
                        while (y < size.height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2f,
                            )
                            y += lineGap
                        }

                        drawLine(
                            color = marginLineColor,
                            start = Offset(28.dp.toPx(), 0f),
                            end = Offset(28.dp.toPx(), size.height),
                            strokeWidth = 2f,
                        )
                    }
                    .padding(horizontal = 36.dp, vertical = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selected.contains(index)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(2.dp, Color(0xFF4E342E))
                                .background(if (isSelected) Color(0xFFFFCDD2) else Color.Transparent)
                                .pointerInput(index) {
                                    detectTapGestures(
                                        onTap = {
                                            toggleSelection(index)
                                        },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Text(
                                    "X",
                                    color = Color(0xFFB71C1C),
                                    modifier = Modifier.pointerInput(index) {
                                        detectTapGestures(
                                            onTap = {
                                                toggleSelection(index)
                                            },
                                        )
                                    },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        val displayText = if (item.quantity > 1) {
                            "${item.name} ×${item.quantity}"
                        } else {
                            item.name
                        }
                        Text(text = displayText, color = Color(0xFF4E342E))
                    }
                }

                if (items.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("אין פריטים עדיין", color = Color(0xFF6D4C41))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE6E6A8)),
        )
    }
}

private data class GroceryItem(
    val name: String,
    val quantity: Int,
)

internal data class ParsedVoiceItem(
    val name: String,
    val quantity: Int,
)

private fun mergeIntoBatch(batch: MutableList<ParsedVoiceItem>, item: ParsedVoiceItem) {
    val idx = batch.indexOfFirst { normalizeItemKey(it.name) == normalizeItemKey(item.name) }
    if (idx >= 0) {
        val current = batch[idx]
        batch[idx] = current.copy(quantity = current.quantity + item.quantity)
    } else {
        batch += item
    }
}

private fun generateHouseholdCode(): String {
    return "SL-" + (100000..999999).random()
}

internal fun parseTranscriptItems(transcript: String): List<ParsedVoiceItem> {
    val cleaned = transcript
        .replace(Regex("[\\n\\r]+"), " ")
        .replace(Regex("\\s*[,;،]+\\s*"), " | ")
        .replace(Regex("\\s+"), " ")
        .trim()

    if (cleaned.isBlank()) {
        return emptyList()
    }

    val hardParts = cleaned
        .split(Regex("\\s*\\|\\s*"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val listSeparatorWords = setOf("וגם", "גם", "ואז", "ואחר", "אחר", "אחרכך", "ולאחר")
    val parsedItems = mutableListOf<ParsedVoiceItem>()

    hardParts.forEach { part ->
        val tokens = part.split(" ").filter { it.isNotBlank() }
        val segmentTokens = mutableListOf<String>()

        fun flushSegment() {
            if (segmentTokens.isEmpty()) return
            val phrase = segmentTokens.joinToString(" ").trim()
            if (phrase.isNotBlank()) {
                val parsed = parseHebrewTranscript(phrase)
                if (parsed.name.isNotBlank()) {
                    parsedItems += parsed
                }
            }
            segmentTokens.clear()
        }

        tokens.forEach tokenLoop@{ raw ->
            val token = raw.trim()
            val norm = normalizeHebrewWord(token)

            if (norm in listSeparatorWords) {
                flushSegment()
                return@tokenLoop
            }

            val startsWithVavConnector =
                token.startsWith("ו") && token.length > 2 && segmentTokens.isNotEmpty() && !isHebrewNumberToken(token)

            if (startsWithVavConnector) {
                flushSegment()
                val withoutVav = token.drop(1).trim()
                if (withoutVav.isNotBlank()) {
                    segmentTokens += withoutVav
                }
            } else {
                segmentTokens += token
            }
        }

        flushSegment()
    }

    return parsedItems
}

private fun stripLeadingConnector(value: String): String {
    return value
        .replace(Regex("^(וגם|גם|ואז|ואחר כך|אחר כך|אחרכך|ולאחר מכן)\\s+"), "")
        .trim()
}

private fun isHebrewNumberToken(token: String): Boolean {
    return token.toIntOrNull() != null || parseHebrewNumberToken(token) in 1..99
}

private fun parseHebrewTranscript(transcript: String): ParsedVoiceItem {
    val cleaned = transcript
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    if (cleaned.isBlank()) {
        return ParsedVoiceItem(name = "", quantity = 1)
    }

    val tokens = cleaned.split(" ")
    val quantityMatch = extractHebrewQuantity(tokens)
    val quantity = quantityMatch.first.coerceIn(1, 99)
    val quantityIndexes = quantityMatch.second

    val nameTokens = tokens.filterIndexed { index, _ -> index !in quantityIndexes }
    val name = nameTokens.joinToString(" ").trim().ifBlank { cleaned }

    return ParsedVoiceItem(name = name, quantity = quantity)
}

private fun extractHebrewQuantity(tokens: List<String>): Pair<Int, Set<Int>> {
    tokens.forEachIndexed { index, token ->
        val numeric = token.toIntOrNull()
        if (numeric != null && numeric in 1..99) {
            return numeric to setOf(index)
        }
    }

    for (i in 0 until tokens.size - 1) {
        val tens = parseHebrewNumberToken(tokens[i])
        val ones = parseHebrewNumberToken(tokens[i + 1])
        if (tens in listOf(20, 30, 40, 50, 60, 70, 80, 90) && ones in 1..9) {
            return (tens + ones) to setOf(i, i + 1)
        }
    }

    tokens.forEachIndexed { index, token ->
        val parsed = parseHebrewNumberToken(token)
        if (parsed in 1..99) {
            return parsed to setOf(index)
        }
    }

    return 1 to emptySet()
}

private fun parseHebrewNumberToken(token: String): Int {
    val normalized = normalizeHebrewWord(token)
    val withoutVav = if (normalized.startsWith("ו") && normalized.length > 1) {
        normalized.drop(1)
    } else {
        normalized
    }

    val numbers = mapOf(
        "אחד" to 1,
        "אחת" to 1,
        "אחדה" to 1,
        "שניים" to 2,
        "שתיים" to 2,
        "שתים" to 2,
        "שני" to 2,
        "שתי" to 2,
        "שלוש" to 3,
        "שלושה" to 3,
        "ארבע" to 4,
        "ארבעה" to 4,
        "חמש" to 5,
        "חמישה" to 5,
        "שש" to 6,
        "שישה" to 6,
        "שבע" to 7,
        "שבעה" to 7,
        "שמונה" to 8,
        "תשע" to 9,
        "תשעה" to 9,
        "עשר" to 10,
        "עשרה" to 10,
        "אחתעשרה" to 11,
        "אחדעשר" to 11,
        "שתיםעשרה" to 12,
        "שניםעשר" to 12,
        "שתייםעשרה" to 12,
        "שלושעשרה" to 13,
        "שלושהעשר" to 13,
        "ארבעעשרה" to 14,
        "ארבעהעשר" to 14,
        "חמשעשרה" to 15,
        "חמישהעשר" to 15,
        "ששעשרה" to 16,
        "שישהעשר" to 16,
        "שבעעשרה" to 17,
        "שבעהעשר" to 17,
        "שמונהעשרה" to 18,
        "תשעעשרה" to 19,
        "תשעהעשר" to 19,
        "עשרים" to 20,
        "שלושים" to 30,
        "ארבעים" to 40,
        "חמישים" to 50,
        "שישים" to 60,
        "שבעים" to 70,
        "שמונים" to 80,
        "תשעים" to 90,
    )

    return numbers[withoutVav] ?: 0
}

private fun normalizeHebrewWord(value: String): String {
    return Normalizer
        .normalize(value.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("[\\u0591-\\u05C7]"), "")
        .replace(Regex("[^\\p{L}\\p{N}]"), "")
}

private fun normalizeItemKey(name: String): String {
    val normalized = Normalizer.normalize(name.lowercase(), Normalizer.Form.NFD)
    return normalized
        .replace(Regex("[\\u0591-\\u05C7]"), "")
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
