package com.mesender.app.data.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val lockManager: LockManagerImpl
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            lockManager.lockApp()
            lockManager.lockAllInboxes()
        }
    }
}