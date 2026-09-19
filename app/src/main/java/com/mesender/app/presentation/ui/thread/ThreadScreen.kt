package com.mesender.app.presentation.ui.thread

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.mesender.app.presentation.ui.lock.LockScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThreadScreen(
    inboxId: Long,
    onBack: () -> Unit,
    onEditItem: (Long) -> Unit = {},
    viewModel: ThreadViewModel = hiltViewModel()
) {
    LaunchedEffect(inboxId) { viewModel.start(inboxId) }
    val state by viewModel.uiState.collectAsState()

    if (state.isGated) {
        LockScreen(setupMode = false, inboxId = inboxId, onUnlocked = {})
        return
    }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isSearchActive = state.isSearchMode
    val isSelectionMode = state.isSelectionMode
    var showDeleteDialog by remember { mutableStateOf(false) }
    val displayItems = if (isSearchActive && state.searchQuery.isNotEmpty()) state.searchResults else state.items
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }

    LaunchedEffect(displayItems.size) {
        if (displayItems.isNotEmpty()) listState.animateScrollToItem(0)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${state.selectedIds.size} item(s)?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            "${state.selectedIds.size} selected",
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { viewModel.selectAllItems() }
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear selection"
                            )
                        }
                    },
                    actions = {
                        if (state.selectedIds.size == 1) {
                            IconButton(onClick = {
                                val selectedItem = viewModel.getSelectedItem()
                                selectedItem?.let { viewModel.startEditing(it) }
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected"
                            )
                        }
                        IconButton(onClick = {
                            val selectedItems = viewModel.getSelectedItems()
                            if (selectedItems.isNotEmpty()) {
                                val shareText = selectedItems.joinToString("\n\n") {
                                    it.textContent.orEmpty()
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                            }
                            viewModel.clearSelection()
                        }) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Share selected"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                placeholder = { Text("Search in inbox\u2026") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester)
                            )
                        } else {
                            Text(state.inbox?.name ?: "\u2026")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isSearchActive) viewModel.clearSearch() else onBack()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isSearchActive) "Clear search" else "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchActive) "Close search" else "Search"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            ThreadInputBar(
                draft = state.draft,
                isSending = state.isSending,
                onDraftChange = viewModel::updateDraft,
                onSend = viewModel::sendText,
                onMediaPicked = { uri, mime -> viewModel.sendMedia(uri, mime, null) },
                onFocus = { viewModel.clearSelection() },
                editingItem = state.editingItem,
                onCancelEdit = { viewModel.cancelEditing() },
                onSaveEdit = { viewModel.saveEdit() },
                modifier = Modifier.imePadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        val threadList = remember(displayItems) { buildThreadList(displayItems) }
        val visibleDate = remember(threadList, listState) {
            derivedStateOf {
                if (threadList.isEmpty()) return@derivedStateOf ""
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val clampedIndex = firstVisibleIndex.coerceIn(0, threadList.size - 1)
                val item = threadList[clampedIndex]
                val idx = threadList.indexOfLast { it.type == ThreadListItem.Type.DATE_HEADER && it.id <= item.id }
                if (idx >= 0) threadList[idx].dateLabel else ""
            }
        }
        val currentDate by visibleDate
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                reverseLayout = true
            ) {
                threadList.forEach { listItem ->
                    if (listItem.type == ThreadListItem.Type.NOTE) {
                        item(listItem.id) {
                            listItem.item?.let { item ->
                                ItemBubble(
                                    item = item,
                                    isSelected = item.id in state.selectedIds,
                                    isSelectionMode = isSelectionMode,
                                    onToggleSelection = { viewModel.toggleSelection(item) }
                                )
                            }
                        }
                    }
                }
            }
            if (currentDate.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            currentDate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeaderRow(dateLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                dateLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
