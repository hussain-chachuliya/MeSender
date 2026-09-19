# Task 21 – Lock Screen with PIN Pad and Biometric

## Summary
Implemented app-lock and per-inbox lock screen with PIN entry, biometric fallback, and setup-mode support.

## Files Changed
- `LockViewModel.kt` – ViewModel handling PIN verify, biometric, setup/unlock modes
- `LockScreenTest.kt` – Compose UI tests for setup, unlock, inbox, biometric flows
- `LockViewModelTest.kt` – Unit tests for biometric ordering guarantees

## Verification
- `:app:testDebugUnitTest` — all green
- `:app:connectedDebugAndroidTest` — skipped (no device)
- `:app:assembleDebug` — BUILD SUCCESSFUL

---

## Review Fixes

### Fix: Biometric inbox-mode data-layer unlock (critical)
In `onBiometricSuccess()`, `_unlocked=true` was set without first calling `UnlockInbox(targetInboxId)`. On real hardware + inbox mode the UI unlocked while the data-layer inbox stayed locked (`IsInboxUnlocked` still false).

**Fix:** Wrapped biometric success in `viewModelScope.launch`, call `targetInboxId?.let { unlockInboxUseCase(it) }` BEFORE flipping `_unlocked`, mirroring the PIN path exactly.

### Fix: Stronger assertion in `lockScreen_setupMode_acceptsPin`
Added explicit PIN-hash persistence assertion via `pinStore.pinHash()` + `PinHasher.verify("1234", persisted)` to confirm `SetAppLock` wrote the hash, not just that `unlocked` flipped.

### New unit tests (`LockViewModelTest.kt`)
- `biometric success in inbox mode unlocks the inbox before flipping the flag` — uses a custom `LockManager` that snapshots `_unlocked` at the moment `unlockInbox` runs, asserting the ordering invariant (no reveal-before-unlock race).
- `biometric success without inbox mode unlocks the app flag only` — verifies `unlockInbox` is never called when `targetInboxId` is null.
