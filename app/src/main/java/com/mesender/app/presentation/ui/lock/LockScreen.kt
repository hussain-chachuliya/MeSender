package com.mesender.app.presentation.ui.lock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    setupMode: Boolean,
    onUnlocked: () -> Unit,
    inboxId: Long? = null,
    viewModel: LockViewModel = hiltViewModel()
) {
    val digits by viewModel.enteredPin.collectAsState()
    val error by viewModel.error.collectAsState()
    val unlocked by viewModel.unlocked.collectAsState()

    LaunchedEffect(Unit) {
        if (inboxId != null) viewModel.setInboxUnlock(inboxId)
        else if (setupMode) viewModel.setSetupMode()
    }
    LaunchedEffect(unlocked) { if (unlocked) onUnlocked() }

    val activity = LocalActivity()
    val biometricAvailable = !setupMode && viewModel.biometric.isAvailable()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                if (setupMode) "Set your PIN" else "Enter your PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            PinPad(
                digits = digits,
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDeleteLast,
                onBiometric = if (biometricAvailable) {
                    {
                        activity?.let { a ->
                            viewModel.biometric.authenticate(
                                a,
                                onSuccess = { viewModel.onBiometricSuccess() }
                            )
                        }
                    }
                } else null
            )
        }
    }
}

@Composable
private fun LocalActivity(): FragmentActivity? =
    (LocalContext.current as? FragmentActivity)