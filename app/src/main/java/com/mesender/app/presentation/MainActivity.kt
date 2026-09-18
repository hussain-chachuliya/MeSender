package com.mesender.app.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import com.mesender.app.presentation.theme.MeSenderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeSenderTheme {
                Surface {
                    // Empty placeholder; navigation is wired in Task 16, lock gate in Task 22.
                }
            }
        }
    }
}