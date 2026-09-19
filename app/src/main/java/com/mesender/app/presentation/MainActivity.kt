package com.mesender.app.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.mesender.app.presentation.navigation.MeSenderNavHost
import com.mesender.app.presentation.theme.AppTheme
import com.mesender.app.presentation.theme.MeSenderTheme
import com.mesender.app.presentation.ui.lock.AppLockGate
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val APP_THEME_KEY = stringPreferencesKey("app_theme")

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeFlow = dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                try {
                    AppTheme.valueOf(prefs[APP_THEME_KEY] ?: AppTheme.GREEN.name)
                } catch (_: Exception) {
                    AppTheme.GREEN
                }
            }
            .flowOn(Dispatchers.IO)
        setContent {
            val theme by themeFlow.collectAsState(initial = AppTheme.GREEN)
            MeSenderTheme(appTheme = theme) {
                AppLockGate {
                    MeSenderNavHost(navController = rememberNavController())
                }
            }
        }
    }
}
