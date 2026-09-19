package com.mesender.app.presentation.ui.thread

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mesender.app.presentation.ui.lock.LockScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    inboxId: Long,
    onBack: () -> Unit,
    viewModel: ThreadViewModel = hiltViewModel()
) {
    LaunchedEffect(inboxId) { viewModel.start(inboxId) }
    val state by viewModel.uiState.collectAsState()

    if (state.isGated) {
        LockScreen(setupMode = false, inboxId = inboxId, onUnlocked = {})
        return
    }

    val listState = rememberLazyListState()
    val isSearchActive = state.isSearchMode
    val displayItems = if (isSearchActive && state.searchQuery.isNotEmpty()) state.searchResults else state.items
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }

    LaunchedEffect(displayItems.size) {
        if (displayItems.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search in inbox…") },
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
                        Text(state.inbox?.name ?: "…")
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
        },
        bottomBar = {
            ThreadInputBar(
                draft = state.draft,
                isSending = state.isSending,
                onDraftChange = viewModel::updateDraft,
                onSend = viewModel::sendText,
                onMediaPicked = { uri, mime -> viewModel.sendMedia(uri, mime, null) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .background(MaterialTheme.colorScheme.surface),
            reverseLayout = true
        ) {
            items(displayItems, key = { it.id }) { item ->
                ItemBubble(item = item, onDelete = { viewModel.deleteItem(item) })
            }
        }
    }
}
