package com.mesender.app.domain.lock

import androidx.fragment.app.FragmentActivity

interface BiometricAuth {
    fun isAvailable(): Boolean
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (cancelled: Boolean) -> Unit = {})
}
