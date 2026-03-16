package com.superlist

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.net.HttpURLConnection
import java.net.URL
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SuperListApp() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(SUPER_LIST_PREFS_NAME, Context.MODE_PRIVATE)
    }
    val storedHouseholdCode = remember(prefs) { loadHouseholdCode(prefs) }
    val storedSyncConnected = remember(prefs) { loadSyncConnected(prefs) }
    val storedFamilyMembers = remember(prefs) { loadFamilyMembers(prefs) }
    val storedCoupons = remember(prefs) { loadPersistedCoupons(prefs) }
    val storedCouponBalanceUrlTemplate = remember(prefs) { loadCouponBalanceUrlTemplate(prefs) }

    var screen by remember { mutableStateOf("voice") }
    var householdCode by remember { mutableStateOf(storedHouseholdCode) }
    var familyMembers by remember { mutableStateOf(storedFamilyMembers) }
    var syncConnected by remember { mutableStateOf(storedSyncConnected) }
    var couponBalanceUrlTemplate by remember { mutableStateOf(storedCouponBalanceUrlTemplate) }
    var syncStatus by remember {
        mutableStateOf(
            if (storedSyncConnected) "מחובר למשפחה בקוד: $storedHouseholdCode" else "לא מחובר למשפחה",
        )
    }
    val items = remember(prefs) {
        mutableStateListOf<GroceryItem>().apply {
            addAll(loadPersistedItems(prefs))
        }
    }
    val coupons = remember(prefs) {
        mutableStateListOf<CouponRecord>().apply {
            addAll(storedCoupons)
        }
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.toList()) {
        persistItems(prefs, items)
    }

    LaunchedEffect(coupons.toList()) {
        persistCoupons(prefs, coupons)
    }

    LaunchedEffect(householdCode) {
        saveHouseholdCode(prefs, householdCode)
    }

    LaunchedEffect(syncConnected) {
        saveSyncConnected(prefs, syncConnected)
    }

    LaunchedEffect(familyMembers) {
        saveFamilyMembers(prefs, familyMembers)
    }

    LaunchedEffect(couponBalanceUrlTemplate) {
        saveCouponBalanceUrlTemplate(prefs, couponBalanceUrlTemplate)
    }

    fun replaceItemsFromServer(serverItems: List<GroceryItem>) {
        items.clear()
        items.addAll(serverItems)
    }

    fun replaceCouponsFromServer(serverCoupons: List<CouponRecord>) {
        coupons.clear()
        coupons.addAll(serverCoupons)
    }

    fun pushAddOrMerge(parsed: ParsedVoiceItem) {
        if (!syncConnected) return

        scope.launch {
            val ok = HouseholdSyncApi.pushAddOrMerge(
                baseUrl = BuildConfig.SYNC_SERVER_URL,
                householdId = householdCode,
                itemName = parsed.name,
                quantity = parsed.quantity,
            )

            if (ok) {
                val latest = HouseholdSyncApi.fetchList(BuildConfig.SYNC_SERVER_URL, householdCode)
                if (latest != null) {
                    replaceItemsFromServer(latest)
                    syncStatus = "מסונכרן למשפחה"
                }
            } else {
                syncStatus = "שגיאת סנכרון — ננסה שוב"
            }
        }
    }

    fun pushRemoveMany(removedItems: List<GroceryItem>) {
        if (!syncConnected || removedItems.isEmpty()) return

        scope.launch {
            var allOk = true
            removedItems.forEach { item ->
                val ok = HouseholdSyncApi.pushRemove(
                    baseUrl = BuildConfig.SYNC_SERVER_URL,
                    householdId = householdCode,
                    itemName = item.name,
                )
                if (!ok) {
                    allOk = false
                }
            }

            val latest = HouseholdSyncApi.fetchList(BuildConfig.SYNC_SERVER_URL, householdCode)
            if (latest != null) {
                replaceItemsFromServer(latest)
            }
            syncStatus = if (allOk) "סנכרון מחיקה נשלח" else "שגיאת סנכרון במחיקה"
        }
    }

    fun pushCouponUpsert(coupon: CouponRecord) {
        if (!syncConnected || householdCode.isBlank()) return

        scope.launch {
            val ok = HouseholdSyncApi.upsertCoupon(
                baseUrl = BuildConfig.SYNC_SERVER_URL,
                householdId = householdCode,
                coupon = coupon,
            )

            val latest = HouseholdSyncApi.fetchCoupons(BuildConfig.SYNC_SERVER_URL, householdCode)
            if (latest != null) {
                replaceCouponsFromServer(mergeCouponCollections(coupons.toList(), latest))
                syncStatus = if (ok) "קופונים מסונכרנים למשפחה" else "שגיאת סנכרון בקופונים"
            } else if (!ok) {
                syncStatus = "שגיאת סנכרון בקופונים"
            }
        }
    }

    LaunchedEffect(syncConnected, householdCode) {
        if (!syncConnected || householdCode.isBlank()) {
            return@LaunchedEffect
        }

        val initialCoupons = HouseholdSyncApi.fetchCoupons(BuildConfig.SYNC_SERVER_URL, householdCode)
        if (initialCoupons != null) {
            val mergedCoupons = mergeCouponCollections(coupons.toList(), initialCoupons)
            replaceCouponsFromServer(mergedCoupons)
            mergedCoupons.forEach { coupon ->
                HouseholdSyncApi.upsertCoupon(
                    baseUrl = BuildConfig.SYNC_SERVER_URL,
                    householdId = householdCode,
                    coupon = coupon,
                )
            }
        }

        while (syncConnected) {
            val latest = HouseholdSyncApi.fetchList(BuildConfig.SYNC_SERVER_URL, householdCode)
            if (latest != null) {
                replaceItemsFromServer(latest)
            }
            val latestCoupons = HouseholdSyncApi.fetchCoupons(BuildConfig.SYNC_SERVER_URL, householdCode)
            if (latestCoupons != null) {
                replaceCouponsFromServer(mergeCouponCollections(coupons.toList(), latestCoupons))
            }
            delay(1500)
        }
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
            syncStatus = "מסנכרן למשפחה..."
            pushAddOrMerge(parsed)
        }
    }

    fun addCoupon(number: String) {
        val normalizedNumber = number.filter(Char::isDigit)
        if (normalizedNumber.length < 9) {
            return
        }

        val existingIndex = coupons.indexOfFirst { it.number == normalizedNumber }
        val updated = CouponRecord(
            id = coupons.getOrNull(existingIndex)?.id ?: "coupon-${System.currentTimeMillis()}",
            number = normalizedNumber,
            remainingBalance = coupons.getOrNull(existingIndex)?.remainingBalance,
            balanceLastCheckedAt = coupons.getOrNull(existingIndex)?.balanceLastCheckedAt,
            lastImportedAt = nowIsoString(),
        )

        if (existingIndex >= 0) {
            coupons[existingIndex] = updated
        } else {
            coupons.add(0, updated)
        }

        if (syncConnected) {
            syncStatus = "מסנכרן קופון למשפחה..."
            pushCouponUpsert(updated)
        }
    }

    fun updateCouponBalance(couponId: String, amountText: String) {
        val index = coupons.indexOfFirst { it.id == couponId }
        if (index < 0) return

        val updated = coupons[index].copy(
            remainingBalance = amountText.trim().ifBlank { null },
            balanceLastCheckedAt = nowIsoString(),
        )
        coupons[index] = updated

        if (syncConnected) {
            syncStatus = "מסנכרן יתרת קופון למשפחה..."
            pushCouponUpsert(updated)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (screen != "voice") {
                    Button(
                        onClick = {
                            screen = "voice"
                        },
                        modifier = Modifier.width(64.dp).height(44.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "מעבר למסך קולי",
                                modifier = Modifier.size(30.dp),
                            )
                        }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { screen = "coupons" }, modifier = Modifier.height(36.dp)) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = "מעבר לקופונים",
                            )
                        }
                        Button(onClick = { screen = "settings" }, modifier = Modifier.height(36.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "מעבר להגדרות",
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(104.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (screen) {
                "voice" -> VoiceTab(
                    onAddParsed = addParsedItem,
                    items = items,
                    onRemoveSelected = { selected ->
                        val removedSnapshot = selected
                            .sorted()
                            .mapNotNull { index -> items.getOrNull(index) }

                        selected.sortedDescending().forEach { index ->
                            if (index in items.indices) {
                                items.removeAt(index)
                            }
                        }
                        if (syncConnected && selected.isNotEmpty()) {
                            pushRemoveMany(removedSnapshot)
                        }
                    },
                    onClearAll = {
                        val removedSnapshot = items.toList()
                        items.clear()
                        if (syncConnected) {
                            pushRemoveMany(removedSnapshot)
                        }
                    },
                )

                "coupons" -> CouponTab(
                    coupons = coupons,
                    balanceUrlTemplate = couponBalanceUrlTemplate,
                    onBalanceUrlTemplateChange = { couponBalanceUrlTemplate = it },
                    onAddCoupon = { number -> addCoupon(number) },
                    onUpdateCouponBalance = { couponId, amount -> updateCouponBalance(couponId, amount) },
                )

                else -> SettingsTab(
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
                )
            }
        }
    }
}

@Composable
private fun VoiceTab(
    onAddParsed: (ParsedVoiceItem) -> Unit,
    items: List<GroceryItem>,
    onRemoveSelected: (List<Int>) -> Unit,
    onClearAll: () -> Unit,
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("לחץ על הכפתור האדום להתחלה/עצירה של מצב רציף") }
    var isHolding by remember { mutableStateOf(false) }
    var isRecognizing by remember { mutableStateOf(false) }
    var pendingPermissionStart by remember { mutableStateOf(false) }
    var continuousListening by remember { mutableStateOf(false) }
    var commitPendingBatchOnNextResult by remember { mutableStateOf(false) }
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
                        if (parsedItems.isEmpty()) {
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
                                onAddParsed(parsed)
                                statusText = "נוסף: ${parsed.name} ×${parsed.quantity}"
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Text("דיבור רציף בזמן לחיצה")
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .size(196.dp)
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
            Text(
                text = "לחץ ודבר",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
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

        Spacer(modifier = Modifier.height(18.dp))
        YellowListSection(
            modifier = Modifier.weight(1f),
            items = items,
            onRemoveSelected = onRemoveSelected,
            onClearAll = onClearAll,
        )
    }
}

@Composable
private fun SettingsTab(
    householdCode: String,
    familyMembers: Int,
    syncStatus: String,
    onJoinHousehold: (String) -> Unit,
) {
    var joinCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("הגדרות משפחה וסנכרון", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

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
    }
}

@Composable
private fun CouponTab(
    coupons: List<CouponRecord>,
    balanceUrlTemplate: String,
    onBalanceUrlTemplateChange: (String) -> Unit,
    onAddCoupon: (String) -> Unit,
    onUpdateCouponBalance: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val couponSectionState = remember(coupons) { buildCouponSectionState(coupons) }
    var statusText by remember { mutableStateOf("העלה תמונת קופון כדי לחלץ מספר בן 9 ספרות ומעלה") }
    var isProcessingImage by remember { mutableStateOf(false) }
    var pendingCouponNumber by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var balanceDraft by remember { mutableStateOf("") }
    var editingBalanceCouponId by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            statusText = "בחירת תמונת הקופון בוטלה"
            return@rememberLauncherForActivityResult
        }

        isProcessingImage = true
        statusText = "מחלץ מספר קופון מהתמונה..."
        extractCouponNumbersFromImage(
            context = context,
            uri = uri,
            onSuccess = { candidates ->
                isProcessingImage = false
                val best = candidates.firstOrNull().orEmpty()
                if (best.isBlank()) {
                    statusText = "לא נמצא מספר קופון ברור. נסה תמונה חדה יותר"
                } else {
                    pendingCouponNumber = best
                    showConfirmDialog = true
                    statusText = if (candidates.size > 1) {
                        "נמצאו כמה מספרים. בדוק את המספר לפני שמירה"
                    } else {
                        "נמצא מספר קופון. אשר שמירה"
                    }
                }
            },
            onFailure = {
                isProcessingImage = false
                statusText = "פענוח התמונה נכשל. נסה שוב עם צילום ברור יותר"
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1C1)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = "קופונים",
                    tint = Color(0xFF8D6E00),
                    modifier = Modifier.size(36.dp),
                )
                Text("ארנק הקופונים", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "שמור מספרי קופון, פתח את אתר היתרה, והצג את המספר בקופה בזמן אמת",
                    textAlign = TextAlign.Center,
                )
                couponSectionState.featuredCoupon?.let { featuredCoupon ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("מספר הקופון שלך", style = MaterialTheme.typography.titleMedium)
                    Text(
                        featuredCoupon.number,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF5D4037),
                    )
                    Text(
                        if (featuredCoupon.remainingBalance.isNullOrBlank()) {
                            "יתרה שמורה: עדיין לא נשמרה"
                        } else {
                            "יתרה שמורה: ₪${featuredCoupon.remainingBalance}"
                        },
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ייבוא קופון", style = MaterialTheme.typography.titleSmall)
                Text(statusText)
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isProcessingImage,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isProcessingImage) "מעבד תמונה..." else "בחר תמונת קופון")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("תבנית לבדיקת יתרה", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = balanceUrlTemplate,
                    onValueChange = onBalanceUrlTemplateChange,
                    label = { Text("הדבק כתובת יתרה") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("אפשר להשתמש בכתובת ישירה או בכתובת עם {coupon}")
                Text("אם האתר לא מאפשר מילוי אוטומטי, אפשר להעתיק את המספר ולהדביק ידנית")
            }
        }

        if (couponSectionState.actionCoupons.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("אין קופונים שמורים עדיין")
                }
            }
        } else {
            couponSectionState.actionCoupons.forEach { coupon ->
                val balanceUrl = buildCouponBalanceLookupUrl(balanceUrlTemplate, coupon.number)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (coupon.remainingBalance.isNullOrBlank()) {
                                "יתרה: עדיין לא נשמרה"
                            } else {
                                "יתרה: ₪${coupon.remainingBalance}"
                            }
                        )
                        Text(
                            if (coupon.balanceLastCheckedAt.isNullOrBlank()) {
                                "בדיקה אחרונה: עדיין לא נבדק"
                            } else {
                                "בדיקה אחרונה: ${coupon.balanceLastCheckedAt}"
                            }
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("coupon-number", coupon.number))
                                    statusText = "מספר הקופון הועתק ללוח"
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("העתק מספר")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    editingBalanceCouponId = coupon.id
                                    balanceDraft = coupon.remainingBalance.orEmpty()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("עדכן יתרה")
                            }
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(balanceUrl))
                                runCatching { context.startActivity(intent) }
                                    .onSuccess { statusText = "נפתח אתר היתרה עבור הקופון" }
                                    .onFailure { statusText = "לא ניתן לפתוח את אתר היתרה כרגע" }
                            },
                            enabled = !balanceUrl.isNullOrBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("פתח אתר יתרה")
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("שמור קופון") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("אפשר לערוך את המספר לפני שמירה")
                    OutlinedTextField(
                        value = pendingCouponNumber,
                        onValueChange = { pendingCouponNumber = it.filter(Char::isDigit) },
                        label = { Text("מספר קופון") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddCoupon(pendingCouponNumber)
                        showConfirmDialog = false
                        statusText = "הקופון נשמר בהצלחה"
                        pendingCouponNumber = ""
                    },
                    enabled = pendingCouponNumber.filter(Char::isDigit).length >= 9,
                ) {
                    Text("שמור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("ביטול")
                }
            },
        )
    }

    if (editingBalanceCouponId != null) {
        AlertDialog(
            onDismissRequest = { editingBalanceCouponId = null },
            title = { Text("עדכון יתרת קופון") },
            text = {
                OutlinedTextField(
                    value = balanceDraft,
                    onValueChange = { balanceDraft = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    label = { Text("סכום נותר") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val couponId = editingBalanceCouponId
                        if (couponId != null) {
                            onUpdateCouponBalance(couponId, balanceDraft.replace(',', '.'))
                            statusText = "יתרת הקופון עודכנה"
                        }
                        editingBalanceCouponId = null
                    },
                ) {
                    Text("שמור")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBalanceCouponId = null }) {
                    Text("ביטול")
                }
            },
        )
    }
}

@Composable
private fun YellowListSection(
    modifier: Modifier = Modifier,
    items: List<GroceryItem>,
    onRemoveSelected: (List<Int>) -> Unit,
    onClearAll: () -> Unit,
) {
    val selected = remember { mutableStateListOf<Int>() }
    val scrollState = rememberScrollState()

    fun toggleSelection(index: Int) {
        if (selected.contains(index)) {
            selected.remove(index)
        } else {
            selected.add(index)
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("הרשימה המשפחתית", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Text("נבחרו: ${selected.size}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onClearAll()
                    selected.clear()
                },
                enabled = items.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0B4B4),
                    disabledContentColor = Color(0xFF6D4C41),
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text("נקה רשימה")
            }
            OutlinedButton(
                onClick = {
                    onRemoveSelected(selected.toList())
                    selected.clear()
                },
                enabled = selected.isNotEmpty() && items.isNotEmpty(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6D4C41),
                    disabledContentColor = Color(0xFFBCAAA4),
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text("אשר מחיקה")
            }
        }

        val lineColor = Color(0xFF9BB7D4)
        val noteColor = Color(0xFFFFF59D)
        val marginLineColor = Color(0xFFD46A6A)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = noteColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
    }
}

private data class GroceryItem(
    val name: String,
    val quantity: Int,
)

internal data class CouponRecord(
    val id: String,
    val number: String,
    val remainingBalance: String? = null,
    val balanceLastCheckedAt: String? = null,
    val lastImportedAt: String? = null,
)

internal data class CouponSectionState(
    val featuredCoupon: CouponRecord?,
    val actionCoupons: List<CouponRecord>,
)

private const val SUPER_LIST_PREFS_NAME = "super_list_prefs"
private const val SUPER_LIST_ITEMS_KEY = "persisted_items"
private const val SUPER_LIST_HOUSEHOLD_CODE_KEY = "household_code"
private const val SUPER_LIST_SYNC_CONNECTED_KEY = "sync_connected"
private const val SUPER_LIST_FAMILY_MEMBERS_KEY = "family_members"
private const val SUPER_LIST_COUPONS_KEY = "persisted_coupons"
private const val SUPER_LIST_COUPON_BALANCE_URL_TEMPLATE_KEY = "coupon_balance_url_template"

private fun loadPersistedItems(prefs: SharedPreferences): List<GroceryItem> {
    val raw = prefs.getString(SUPER_LIST_ITEMS_KEY, null) ?: return emptyList()

    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val entry = array.optJSONObject(i) ?: continue
                val name = entry.optString("name").trim()
                val quantity = entry.optInt("quantity", 1).coerceIn(1, 99)
                if (name.isNotBlank()) {
                    add(GroceryItem(name = name, quantity = quantity))
                }
            }
        }
    }.getOrDefault(emptyList())
}

private fun persistItems(prefs: SharedPreferences, items: List<GroceryItem>) {
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject()
            .put("name", item.name)
            .put("quantity", item.quantity.coerceIn(1, 99))
        array.put(obj)
    }

    prefs.edit().putString(SUPER_LIST_ITEMS_KEY, array.toString()).apply()
}

private fun loadPersistedCoupons(prefs: SharedPreferences): List<CouponRecord> {
    val raw = prefs.getString(SUPER_LIST_COUPONS_KEY, null) ?: return emptyList()

    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val entry = array.optJSONObject(i) ?: continue
                val id = entry.optString("id").trim()
                val number = entry.optString("number").filter(Char::isDigit)
                if (id.isBlank() || number.length < 9) continue
                add(
                    CouponRecord(
                        id = id,
                        number = number,
                        remainingBalance = entry.optString("remainingBalance").ifBlank { null },
                        balanceLastCheckedAt = entry.optString("balanceLastCheckedAt").ifBlank { null },
                        lastImportedAt = entry.optString("lastImportedAt").ifBlank { null },
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun persistCoupons(prefs: SharedPreferences, coupons: List<CouponRecord>) {
    val array = JSONArray()
    coupons.forEach { coupon ->
        val obj = JSONObject()
            .put("id", coupon.id)
            .put("number", coupon.number)
            .put("remainingBalance", coupon.remainingBalance)
            .put("balanceLastCheckedAt", coupon.balanceLastCheckedAt)
            .put("lastImportedAt", coupon.lastImportedAt)
        array.put(obj)
    }

    prefs.edit().putString(SUPER_LIST_COUPONS_KEY, array.toString()).apply()
}

private fun loadCouponBalanceUrlTemplate(prefs: SharedPreferences): String {
    return prefs.getString(
        SUPER_LIST_COUPON_BALANCE_URL_TEMPLATE_KEY,
        "https://htz.mltp.co.il/Getballance",
    )?.trim().orEmpty()
}

private fun saveCouponBalanceUrlTemplate(prefs: SharedPreferences, template: String) {
    prefs.edit().putString(SUPER_LIST_COUPON_BALANCE_URL_TEMPLATE_KEY, template.trim()).apply()
}

private fun loadHouseholdCode(prefs: SharedPreferences): String {
    val saved = prefs.getString(SUPER_LIST_HOUSEHOLD_CODE_KEY, null)?.trim().orEmpty()
    if (saved.isNotBlank()) {
        return saved
    }

    val generated = generateHouseholdCode()
    prefs.edit().putString(SUPER_LIST_HOUSEHOLD_CODE_KEY, generated).apply()
    return generated
}

private fun saveHouseholdCode(prefs: SharedPreferences, code: String) {
    prefs.edit().putString(SUPER_LIST_HOUSEHOLD_CODE_KEY, code.trim()).apply()
}

private fun loadSyncConnected(prefs: SharedPreferences): Boolean {
    return prefs.getBoolean(SUPER_LIST_SYNC_CONNECTED_KEY, true)
}

private fun saveSyncConnected(prefs: SharedPreferences, value: Boolean) {
    prefs.edit().putBoolean(SUPER_LIST_SYNC_CONNECTED_KEY, value).apply()
}

private fun loadFamilyMembers(prefs: SharedPreferences): Int {
    return prefs.getInt(SUPER_LIST_FAMILY_MEMBERS_KEY, 1).coerceAtLeast(1)
}

private fun saveFamilyMembers(prefs: SharedPreferences, value: Int) {
    prefs.edit().putInt(SUPER_LIST_FAMILY_MEMBERS_KEY, value.coerceAtLeast(1)).apply()
}

private fun nowIsoString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}

private fun buildCouponBalanceLookupUrl(template: String, couponNumber: String): String? {
    val trimmedTemplate = template.trim()
    if (trimmedTemplate.isBlank()) {
        return null
    }

    return if (trimmedTemplate.contains("{coupon}")) {
        trimmedTemplate.replace("{coupon}", couponNumber)
    } else {
        trimmedTemplate
    }
}

internal fun buildCouponSectionState(coupons: List<CouponRecord>): CouponSectionState {
    return CouponSectionState(
        featuredCoupon = coupons.firstOrNull(),
        actionCoupons = coupons,
    )
}

private fun mergeCouponCollections(localCoupons: List<CouponRecord>, serverCoupons: List<CouponRecord>): List<CouponRecord> {
    if (localCoupons.isEmpty()) return serverCoupons.sortedByDescending(::couponSortKey)
    if (serverCoupons.isEmpty()) return localCoupons.sortedByDescending(::couponSortKey)

    val merged = linkedMapOf<String, CouponRecord>()
    (serverCoupons + localCoupons).forEach { coupon ->
        val existing = merged[coupon.number]
        merged[coupon.number] = if (existing == null) coupon else mergeCouponRecord(existing, coupon)
    }

    return merged.values.sortedByDescending(::couponSortKey)
}

private fun mergeCouponRecord(first: CouponRecord, second: CouponRecord): CouponRecord {
    val latestBalanceTimestamp = maxIsoTimestamp(first.balanceLastCheckedAt, second.balanceLastCheckedAt)
    val latestImportTimestamp = maxIsoTimestamp(first.lastImportedAt, second.lastImportedAt)
    val preferFirstBalance = latestBalanceTimestamp == first.balanceLastCheckedAt

    return CouponRecord(
        id = first.id.ifBlank { second.id },
        number = first.number,
        remainingBalance = if (preferFirstBalance) {
            first.remainingBalance ?: second.remainingBalance
        } else {
            second.remainingBalance ?: first.remainingBalance
        },
        balanceLastCheckedAt = latestBalanceTimestamp,
        lastImportedAt = latestImportTimestamp,
    )
}

private fun couponSortKey(coupon: CouponRecord): String {
    return coupon.balanceLastCheckedAt ?: coupon.lastImportedAt ?: ""
}

private fun maxIsoTimestamp(first: String?, second: String?): String? {
    return listOfNotNull(first?.takeIf { it.isNotBlank() }, second?.takeIf { it.isNotBlank() }).maxOrNull()
}

private fun extractCouponCandidates(rawText: String): List<String> {
    val pattern = Regex("(?:\\d[\\d\\s-]{8,}\\d|\\d{9,})")
    return pattern.findAll(rawText)
        .map { match -> match.value.replace(Regex("[^0-9]"), "") }
        .filter { candidate -> candidate.length >= 9 }
        .distinct()
        .sortedByDescending { candidate -> candidate.length }
        .toList()
}

private fun extractCouponNumbersFromImage(
    context: Context,
    uri: Uri,
    onSuccess: (List<String>) -> Unit,
    onFailure: () -> Unit,
) {
    runCatching {
        InputImage.fromFilePath(context, uri)
    }.onSuccess { image ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val candidates = extractCouponCandidates(visionText.text)
                recognizer.close()
                if (candidates.isEmpty()) {
                    onFailure()
                } else {
                    onSuccess(candidates)
                }
            }
            .addOnFailureListener {
                recognizer.close()
                onFailure()
            }
    }.onFailure {
        onFailure()
    }
}

private object HouseholdSyncApi {
    suspend fun fetchList(baseUrl: String, householdId: String): List<GroceryItem>? = withContext(Dispatchers.IO) {
        syncBaseUrls(baseUrl).forEach { candidateBaseUrl ->
            val result = runCatching {
                val url = URL("${candidateBaseUrl.trimEnd('/')}/households/${householdId}/list")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                }

                conn.inputStream.bufferedReader().use { reader ->
                    val json = JSONObject(reader.readText())
                    val itemsArray = json.optJSONArray("items") ?: JSONArray()
                    buildList {
                        for (i in 0 until itemsArray.length()) {
                            val item = itemsArray.optJSONObject(i) ?: continue
                            val name = item.optString("name").trim()
                            val quantity = item.optInt("quantity", 1).coerceIn(1, 99)
                            if (name.isNotBlank()) {
                                add(GroceryItem(name, quantity))
                            }
                        }
                    }
                }
            }.getOrNull()

            if (result != null) {
                return@withContext result
            }
        }

        null
    }

    suspend fun fetchCoupons(baseUrl: String, householdId: String): List<CouponRecord>? = withContext(Dispatchers.IO) {
        syncBaseUrls(baseUrl).forEach { candidateBaseUrl ->
            val result = runCatching {
                val url = URL("${candidateBaseUrl.trimEnd('/')}/households/${householdId}/coupons")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                }

                conn.inputStream.bufferedReader().use { reader ->
                    val json = JSONObject(reader.readText())
                    val couponsArray = json.optJSONArray("coupons") ?: JSONArray()
                    buildList {
                        for (i in 0 until couponsArray.length()) {
                            val coupon = couponsArray.optJSONObject(i) ?: continue
                            val number = coupon.optString("couponNumber").filter(Char::isDigit)
                            if (number.length < 9) continue
                            add(
                                CouponRecord(
                                    id = coupon.optString("id").ifBlank { "coupon-$number" },
                                    number = number,
                                    remainingBalance = coupon.optString("remainingBalance").ifBlank { null },
                                    balanceLastCheckedAt = coupon.optString("balanceLastCheckedAt").ifBlank { null },
                                    lastImportedAt = coupon.optString("lastImportedAt").ifBlank { null },
                                ),
                            )
                        }
                    }
                }
            }.getOrNull()

            if (result != null) {
                return@withContext result
            }
        }

        null
    }

    suspend fun pushAddOrMerge(baseUrl: String, householdId: String, itemName: String, quantity: Int): Boolean =
        withContext(Dispatchers.IO) {
            syncBaseUrls(baseUrl).any { candidateBaseUrl ->
                runCatching {
                    val url = URL("${candidateBaseUrl.trimEnd('/')}/households/${householdId}/mutations")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 4000
                        readTimeout = 4000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                    }

                    val body = JSONObject()
                        .put("clientActionId", "android-${System.currentTimeMillis()}")
                        .put("actionType", "ADD_OR_MERGE")
                        .put("itemName", itemName)
                        .put("quantityDelta", quantity.coerceIn(1, 99))

                    conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
                    conn.responseCode in 200..299
                }.getOrDefault(false)
            }
        }

    suspend fun pushRemove(baseUrl: String, householdId: String, itemName: String): Boolean = withContext(Dispatchers.IO) {
        syncBaseUrls(baseUrl).any { candidateBaseUrl ->
            runCatching {
                val url = URL("${candidateBaseUrl.trimEnd('/')}/households/${householdId}/mutations")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 4000
                    readTimeout = 4000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                val body = JSONObject()
                    .put("clientActionId", "android-${System.currentTimeMillis()}-${itemName.hashCode()}")
                    .put("actionType", "REMOVE")
                    .put("itemName", itemName)

                conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
                conn.responseCode in 200..299
            }.getOrDefault(false)
        }
    }

    suspend fun upsertCoupon(baseUrl: String, householdId: String, coupon: CouponRecord): Boolean = withContext(Dispatchers.IO) {
        syncBaseUrls(baseUrl).any { candidateBaseUrl ->
            runCatching {
                val url = URL("${candidateBaseUrl.trimEnd('/')}/households/${householdId}/coupons")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 4000
                    readTimeout = 4000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                val body = JSONObject()
                    .put("clientActionId", "android-coupon-${System.currentTimeMillis()}-${coupon.number.hashCode()}")
                    .put("couponNumber", coupon.number)
                    .put("remainingBalance", coupon.remainingBalance)
                    .put("balanceLastCheckedAt", coupon.balanceLastCheckedAt)
                    .put("lastImportedAt", coupon.lastImportedAt ?: nowIsoString())

                conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
                conn.responseCode in 200..299
            }.getOrDefault(false)
        }
    }
}

private fun syncBaseUrls(baseUrl: String): List<String> {
    val primary = baseUrl.trim().trimEnd('/')
    val urls = mutableListOf(primary)
    if (primary.contains("dev-list.friedman-makers.com")) {
        urls += "http://127.0.0.1:8789"
        urls += "http://192.168.1.219:8789"
    }
    return urls.distinct()
}

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

    val listSeparatorWords = setOf("ו", "וגם", "גם", "ואז", "ואחר", "אחר", "אחרכך", "ולאחר")
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

            val startsWithVavConnector = token.startsWith("ו") && token.length > 1 && segmentTokens.isNotEmpty()

            if (startsWithVavConnector) {
                val withoutVav = token.drop(1).trim()
                val numberContinuation =
                    withoutVav.isNotBlank() &&
                        isHebrewNumberToken(withoutVav) &&
                        segmentTokens.all { existing -> isHebrewNumberToken(existing) }

                if (numberContinuation) {
                    segmentTokens += token
                } else if (withoutVav.isNotBlank()) {
                    flushSegment()
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
