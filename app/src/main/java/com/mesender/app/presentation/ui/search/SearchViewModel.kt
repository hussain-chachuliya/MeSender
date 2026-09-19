package com.mesender.app.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.UpdateItemText
import com.mesender.app.domain.usecase.search.SearchItems
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchItemsUseCase: SearchItems,
    private val deleteItemUseCase: DeleteItem,
    private val updateItemTextUseCase: UpdateItemText
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(isSearching = true)
            searchItemsUseCase(query).collect { results ->
                _uiState.value = _uiState.value.copy(results = results, isSearching = false)
            }
        }
    }

    fun toggleSelection(item: Item) {
        val isLocked = _uiState.value.results.firstOrNull { it.item.id == item.id }?.inboxLocked ?: return
        if (isLocked) return
        val current = _uiState.value.selectedIds
        _uiState.value = _uiState.value.copy(
            selectedIds = if (item.id in current) current - item.id else current + item.id
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    fun selectAllResults() {
        val allIds = _uiState.value.results.filterNot { it.inboxLocked }.map { it.item.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedIds = allIds)
    }

    fun getSelectedItems(): List<Item> {
        val selectedIds = _uiState.value.selectedIds
        return _uiState.value.results.map { it.item }.filter { it.id in selectedIds }
    }

    fun getSelectedItem(): Item? {
        val selectedIds = _uiState.value.selectedIds
        if (selectedIds.size != 1) return null
        return _uiState.value.results.map { it.item }.find { it.id == selectedIds.first() }
    }

    fun deleteSelected() {
        val selectedIds = _uiState.value.selectedIds
        if (selectedIds.isEmpty()) return
        val itemsToDelete = _uiState.value.results.map { it.item }.filter { it.id in selectedIds }
        viewModelScope.launch {
            itemsToDelete.forEach { deleteItemUseCase(it) }
            _uiState.value = _uiState.value.copy(
                results = _uiState.value.results.filterNot { it.item.id in selectedIds },
                selectedIds = emptySet()
            )
        }
    }

    fun updateDraft(text: String) {
        _uiState.value = _uiState.value.copy(draft = text)
    }

    fun startEditing(item: Item) {
        _uiState.value = _uiState.value.copy(
            editingItem = item,
            draft = item.textContent.orEmpty(),
            selectedIds = emptySet()
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingItem = null, draft = "")
    }

    fun saveEdit() {
        val editingItem = _uiState.value.editingItem ?: return
        val newText = _uiState.value.draft.trim()
        if (newText.isEmpty() || newText == editingItem.textContent) {
            cancelEditing()
            return
        }
        viewModelScope.launch {
            updateItemTextUseCase(editingItem, newText)
            _uiState.value = _uiState.value.copy(
                results = _uiState.value.results.map { result ->
                    if (result.item.id == editingItem.id) {
                        result.copy(item = result.item.copy(textContent = newText, updatedAt = System.currentTimeMillis()))
                    } else {
                        result
                    }
                },
                editingItem = null,
                draft = ""
            )
        }
    }
}