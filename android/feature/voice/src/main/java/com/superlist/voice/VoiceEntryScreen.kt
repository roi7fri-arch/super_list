package com.superlist.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun VoiceEntryScreen() {
    val isHolding = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Red, shape = CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding.value = true
                            tryAwaitRelease()
                            isHolding.value = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "לחץ ודבר", color = Color.White)
        }
    }
}
