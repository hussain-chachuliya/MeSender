package com.mesender.app.presentation.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mesender.app.presentation.ui.common.DeleteItemsDialog
import com.mesender.app.presentation.ui.common.ItemSelectionTopBar
import com.mesender.app.presentation.ui.common.shareItems
import com.mesender.app.presentation.ui.thread.ItemBubble
import com.mesender.app.presentation.ui.thread.ThreadInputBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenInbox: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    if (showDeleteDialog) {
        DeleteItemsDialog(
            itemCount = state.selectedIds.size,
            onConfirm = {
                viewModel.deleteSelected()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            if (state.isSelectionMode) {
                ItemSelectionTopBar(
                    selectedCount = state.selectedIds.size,
                    showEdit = state.selectedIds.size == 1,
                    onClearSelection = viewModel::clearSelection,
                    onEdit = {
                        viewModel.getSelectedItem()?.let { viewModel.startEditing(it) }
                    },
                    onDelete = { showDeleteDialog = true },
                    onShare = { shareItems(context, viewModel.getSelectedItems()) },
                    onSelectAll = viewModel::selectAllResults
                )
            } else {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = { Text("Search everything you saved") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .focusRequester(focusRequester)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (state.isEditing) {
                ThreadInputBar(
                    draft = state.draft,
                    isSending = false,
                    onDraftChange = viewModel::updateDraft,
                    onSend = {},
                    onMediaPicked = { _, _ -> },
                    onFocus = {},
                    editingItem = state.editingItem,
                    onCancelEdit = viewModel::cancelEditing,
                    onSaveEdit = viewModel::saveEdit,
                    showAttach = false,
                    modifier = Modifier.imePadding()
                )
            }
        }
    ) { padding ->
        if (state.query.isBlank()) {
            Text(
                "Type to search across all your inboxes.",
                modifier = Modifier.padding(padding),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(state.results, key = { it.item.id }) { result ->
                    if (result.inboxLocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Text(
                                "Locked — ${result.inboxName}",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } else {
                        ItemBubble(
                            item = result.item,
                            isSelected = result.item.id in state.selectedIds,
                            isSelectionMode = state.isSelectionMode,
                            onToggleSelection = { viewModel.toggleSelection(result.item) },
                            modifier = if (state.isSelectionMode) {
                                Modifier
                            } else {
                                Modifier.clickable { onOpenInbox(result.item.inboxId) }
                            }
                        )
                    }
                }
            }
        }
    }
}