package com.mesender.app.presentation.ui.thread

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ThreadInputBar(
    draft: String,
    isSending: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onMediaPicked: (Uri, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
            onMediaPicked(it, mimeType)
        }
    }

    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }) { Icon(Icons.Default.Add, contentDescription = "Attach") }
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message yourself…") }
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSend, enabled = draft.isNotBlank() && !isSending) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}