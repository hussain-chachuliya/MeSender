package com.mesender.app.presentation.ui.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.mesender.app.presentation.ui.common.DeleteItemsDialog
import com.mesender.app.presentation.ui.common.ItemSelectionTopBar
import com.mesender.app.presentation.ui.common.shareItems
import com.mesender.app.presentation.ui.lock.LockScreen

@OptIn(ExperimentalMaterial3Api::class)
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
            if (isSelectionMode) {
                ItemSelectionTopBar(
                    selectedCount = state.selectedIds.size,
                    showEdit = state.selectedIds.size == 1,
                    onClearSelection = viewModel::clearSelection,
                    onEdit = {
                        viewModel.getSelectedItem()?.let { viewModel.startEditing(it) }
                    },
                    onDelete = { showDeleteDialog = true },
                    onShare = {
                        shareItems(context, viewModel.getSelectedItems())
                        viewModel.clearSelection()
                    },
                    onSelectAll = viewModel::selectAllItems
                )
            } else if (isSearchActive) {
                Surface(color = MaterialTheme.colorScheme.primary) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    ) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Clear search"
                            )
                        }
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search in inbox\u2026") },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                                focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(searchFocusRequester)
                        )
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close search"
                            )
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(state.inbox?.name ?: "\u2026") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
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
