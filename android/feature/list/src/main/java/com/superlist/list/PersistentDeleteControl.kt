package com.superlist.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PersistentDeleteControl(onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Button(onClick = onDelete, modifier = Modifier.background(Color.Red)) {
            Text(text = "מחק")
        }
    }
}
