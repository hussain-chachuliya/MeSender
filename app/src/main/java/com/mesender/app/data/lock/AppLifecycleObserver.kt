package com.mesender.app.data.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mesender.app.di.AppScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val lockManager: LockManagerImpl,
    @AppScope private val scope: CoroutineScope
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        scope.launch {
            lockManager.lockApp()
            lockManager.lockAllInboxes()
        }
    }
}