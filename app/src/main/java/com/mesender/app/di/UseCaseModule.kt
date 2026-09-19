package com.mesender.app.di

import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.repository.InboxRepository
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.inbox.CreateInbox
import com.mesender.app.domain.usecase.inbox.DeleteInbox
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.inbox.RenameInbox
import com.mesender.app.domain.usecase.inbox.ToggleInboxLock
import com.mesender.app.domain.usecase.item.ComposeTextItem
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.GetItemsByInbox
import com.mesender.app.domain.usecase.item.ShareLinkItem
import com.mesender.app.domain.usecase.item.ShareMediaItem
import com.mesender.app.domain.usecase.lock.IsInboxUnlocked
import com.mesender.app.domain.usecase.lock.LockAllInboxes
import com.mesender.app.domain.usecase.lock.RemoveAppLock
import com.mesender.app.domain.usecase.lock.SetAppLock
import com.mesender.app.domain.usecase.lock.UnlockInbox
import com.mesender.app.domain.usecase.lock.VerifyPin
import com.mesender.app.domain.usecase.search.SearchItems
import com.mesender.app.domain.usecase.search.SearchItemsInInbox
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton fun createInbox(r: InboxRepository) = CreateInbox(r)
    @Provides @Singleton fun renameInbox(r: InboxRepository) = RenameInbox(r)
    @Provides @Singleton fun deleteInbox(r: InboxRepository) = DeleteInbox(r)
    @Provides @Singleton fun getInboxes(r: InboxRepository) = GetInboxes(r)
    @Provides @Singleton fun toggleInboxLock(r: InboxRepository) = ToggleInboxLock(r)

    @Provides @Singleton fun getItemsByInbox(r: ItemRepository) = GetItemsByInbox(r)
    @Provides @Singleton fun composeTextItem(r: ItemRepository) = ComposeTextItem(r)
    @Provides @Singleton fun shareMediaItem(r: ItemRepository) = ShareMediaItem(r)
    @Provides @Singleton fun shareLinkItem(r: ItemRepository) = ShareLinkItem(r)
    @Provides @Singleton fun deleteItem(r: ItemRepository) = DeleteItem(r)

    @Provides @Singleton fun searchItems(r: ItemRepository) = SearchItems(r)
    @Provides @Singleton fun searchItemsInInbox(r: ItemRepository) = SearchItemsInInbox(r)

    @Provides @Singleton fun setAppLock(p: PinStore) = SetAppLock(p)
    @Provides @Singleton fun removeAppLock(p: PinStore, l: LockManager) = RemoveAppLock(p, l)
    @Provides @Singleton fun verifyPin(p: PinStore, l: LockManager) = VerifyPin(p, l)
    @Provides @Singleton fun unlockInbox(l: LockManager) = UnlockInbox(l)
    @Provides @Singleton fun isInboxUnlocked(l: LockManager) = IsInboxUnlocked(l)
    @Provides @Singleton fun lockAllInboxes(l: LockManager) = LockAllInboxes(l)
}