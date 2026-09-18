package com.mesender.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.mesender.app.data.lock.AppLifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MeSenderApplication : Application() {

    @Inject lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }
}