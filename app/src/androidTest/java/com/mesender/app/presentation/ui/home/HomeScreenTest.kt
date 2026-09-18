package com.mesender.app.presentation.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.repository.InboxRepository
import com.mesender.app.domain.usecase.inbox.CreateInbox
import com.mesender.app.domain.usecase.inbox.DeleteInbox
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.inbox.RenameInbox
import com.mesender.app.domain.usecase.inbox.ToggleInboxLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyState_showsPrompt() {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onOpenInbox = {},
                    onOpenSearch = {},
                    viewModel = homeViewModelWith()
                )
            }
        }
        composeRule.onNodeWithText("No inboxes yet. Create one to start saving.").assertIsDisplayed()
    }

    @Test
    fun createDialog_createsInbox() {
        var created: String? = null
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onOpenInbox = {},
                    onOpenSearch = {},
                    viewModel = homeViewModelWith(createCallback = { created = it })
                )
            }
        }
        composeRule.onNodeWithContentDescription("New inbox").performClick()
        composeRule.onNodeWithText("Inbox name").performTextReplacement("Work")
        composeRule.onNodeWithText("Create").performClick()
        assertEquals("Work", created)
    }

    private fun homeViewModelWith(createCallback: (String) -> Unit = {}): HomeViewModel {
        val repository = object : InboxRepository {
            private val inboxes = MutableStateFlow<List<Inbox>>(emptyList())

            override fun observeInboxes(): Flow<List<Inbox>> = inboxes

            override fun observeInbox(id: Long): Flow<Inbox?> =
                inboxes.map { list -> list.firstOrNull { it.id == id } }

            override suspend fun createInbox(name: String): Inbox {
                val inbox =
                    Inbox(System.currentTimeMillis(), name, false, 0, System.currentTimeMillis())
                inboxes.value = inboxes.value + inbox
                createCallback(name)
                return inbox
            }

            override suspend fun renameInbox(id: Long, newName: String) = Unit
            override suspend fun deleteInbox(id: Long) = Unit
            override suspend fun setInboxLocked(id: Long, locked: Boolean) = Unit
        }
        return HomeViewModel(
            GetInboxes(repository),
            CreateInbox(repository),
            RenameInbox(repository),
            DeleteInbox(repository),
            ToggleInboxLock(repository)
        )
    }
}