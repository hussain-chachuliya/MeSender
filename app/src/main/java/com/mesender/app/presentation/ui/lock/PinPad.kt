package com.mesender.app.presentation.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinPad(
    digits: String,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onBiometric: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { i ->
                val filled = i < digits.length
                Surface(
                    modifier = Modifier.size(16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = if (filled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }
        }
        val rows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('X', '0', 'D') // X → biometric if available, D → backspace
        )
        rows.forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                rowKeys.forEach { key ->
                    when (key) {
                        'X' -> if (onBiometric != null) {
                            IconButton(onClick = onBiometric, modifier = Modifier.size(56.dp)) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = "Unlock with biometric"
                                )
                            }
                        } else {
                            Spacer(Modifier.size(56.dp))
                        }
                        'D' -> IconButton(onClick = onDelete, modifier = Modifier.size(56.dp)) {
                            Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete"
                            )
                        }
                        else -> TextButton(onClick = { onDigit(key) }, modifier = Modifier.size(56.dp)) {
                            Text(key.toString(), fontSize = 28.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}