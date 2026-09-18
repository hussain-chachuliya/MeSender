package com.mesender.app.presentation.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mesender.app.domain.model.Inbox
import com.mesender.app.presentation.theme.MeSenderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareTargetActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pending = ShareIntentParser.parse(intent)
        pending?.let(viewModel::init)

        setContent {
            MeSenderTheme {
                ShareTargetContent(
                    pendingLabel = pending?.let { labelFor(it) },
                    state = viewModel.uiState.collectAsState().value,
                    onPick = viewModel::saveTo,
                    onFinish = { finish() },
                    onOpenApp = {
                        startActivity(
                            Intent(this, com.mesender.app.presentation.MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun labelFor(pending: PendingShare): String = when (pending) {
        is PendingShare.Text -> "Text"
        is PendingShare.Link -> "Link"
        is PendingShare.Media -> pending.mimeType
        is PendingShare.Multiple -> "${pending.shares.size} items"
    }
}

@Composable
private fun ShareTargetContent(
    pendingLabel: String?,
    state: ShareUiState,
    onPick: (Long) -> Unit,
    onFinish: () -> Unit,
    onOpenApp: () -> Unit
) {
    if (pendingLabel == null) return

    if (state.saving) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Saving…", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    state.savedTo?.let { name ->
        AlertDialog(
            onDismissRequest = onFinish,
            title = { Text("Saved") },
            text = { Text("Saved to $name") },
            confirmButton = { TextButton(onClick = onFinish) { Text("Done") } }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Save $pendingLabel to…", style = MaterialTheme.typography.titleMedium)
        if (state.error) {
            Text(
                "Couldn't save. Check storage and try again.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (state.inboxes.isEmpty()) {
            Text(
                "No inboxes yet.",
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenApp) { Text("Open MeSender to create one") }
        } else {
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(state.inboxes, key = { it.id }) { inbox: Inbox ->
                    Surface(onClick = { onPick(inbox.id) }) {
                        Text(
                            inbox.name,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}