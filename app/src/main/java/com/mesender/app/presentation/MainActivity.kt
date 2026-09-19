package com.mesender.app.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.mesender.app.presentation.navigation.MeSenderNavHost
import com.mesender.app.presentation.theme.MeSenderTheme
import com.mesender.app.presentation.ui.lock.AppLockGate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeSenderTheme {
                AppLockGate {
                    MeSenderNavHost(navController = rememberNavController())
                }
            }
        }
    }
}
