package com.superlist.list

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SharedListScreen(onDeleteSelected: () -> Unit) {
    Column {
        PersistentDeleteControl(onDelete = onDeleteSelected)
        Text(text = "רשימת סופר משותפת")
    }
}
