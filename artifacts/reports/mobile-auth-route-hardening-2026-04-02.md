# Mobile Auth And Route Hardening

Date: 2026-04-02

## Scope

This change set fixes the three issues previously identified in the Android app:

1. Signed-in users and admins could get trapped in a login/dashboard back-navigation loop.
2. Saved sessions from shared preferences were treated as valid before the backend had revalidated them.
3. A stale `AdminLoginActivity` route still existed in the app manifest even though the app no longer uses a separate admin sign-in screen.

No backend or web code was changed in this pass. The work was limited to the Android app.

## Root Causes

### 1. Back-navigation loop

In `BaseDrawerActivity`, pressing Android back on drawer-based screens always called `goToLoginScreen()`.  
That forced the app back to `LoginActivity`, and `LoginActivity` would immediately redirect authenticated users back into their dashboard.  
Result: the user could bounce between login and dashboard instead of exiting or returning normally.

### 2. Stale saved-session trust

`AppSessionStore.restore()` was loading saved access/refresh tokens from shared preferences and immediately marking the matching session as logged in in memory.  
That meant the UI could act authenticated before the backend had confirmed the refresh token was still valid.  
If the token had expired or been revoked, protected API calls could fail later with auth errors while the app still looked signed in.

### 3. Stale admin route surface

`AdminLoginActivity` was still present and registered in the manifest, but it only forwarded to `LoginActivity`.  
It was no longer needed and left an extra route surface in the app.

## What Changed

### `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`

- Replaced the old unconditional back-to-login behavior.
- New behavior:
  - if the drawer is open, close it
  - else if the screen is not the task root, finish the current activity
  - else move the app task to the background

This removes the login/dashboard loop and gives drawer screens normal Android back behavior.

### `mobile/app/src/main/java/com/example/uiprototypebeta/AppSessionStore.kt`

- Added a `StoredSession` model inside the session store.
- Changed restore behavior so saved sessions are now loaded as **pending**, not immediately treated as active.
- Added:
  - `hasPendingSession()`
  - `pendingRefreshToken()`
  - `activatePendingSession(updatedAccessToken)`
- Kept `saveUserSession()` and `saveAdminSession()` as the only paths that immediately activate a session in memory.
- Updated access-token persistence so refreshed access tokens update storage cleanly.

This separates:

- "a saved session exists"
- from
- "the backend has verified that session and it is active"

### `mobile/app/src/main/java/com/example/uiprototypebeta/ApiClient.kt`

- Added stored-session validation logic for app launch.
- Added refresh result states:
  - `SUCCESS`
  - `INVALID`
  - `NETWORK_ERROR`
- Changed token refresh handling so:
  - successful refresh updates the session correctly
  - invalid refresh clears the saved session
  - auth failures now return a clearer `"Session expired. Please sign in again."` error

This prevents the app from silently holding onto a broken auth state after the backend has rejected the refresh token.

### `mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt`

- Added startup handling for pending saved sessions.
- The login screen now:
  - checks for an already active in-memory session and redirects if valid
  - otherwise checks for a pending saved session
  - validates that saved session against the backend refresh endpoint before redirecting
- While validation is running, the login, guest, and sign-up buttons are disabled to avoid conflicting actions.
- If validation fails because the session is invalid, the app clears it and leaves the user at the primary sign-in screen.
- If validation cannot be completed because of a network issue, the app stays on the primary sign-in screen and shows a toast instead of pretending the session is valid.

### `mobile/app/src/main/java/com/example/uiprototypebeta/UserDashboardActivity.kt`

- Added `ensureUserSession()`.
- The screen now redirects back to the primary sign-in flow if it is opened without a live user session.
- This check runs in both `onCreate()` and `onResume()`.

### `mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt`

- Added `ensureAdminSession()`.
- The screen now redirects back to the primary sign-in flow if it is opened without a live admin session.
- This check runs in both `onCreate()` and `onResume()`.

These two route guards reduce the chance of landing on a protected page after a stale activity restore or after a session has already been cleared.

### `mobile/app/src/main/AndroidManifest.xml`

- Removed the old `AdminLoginActivity` registration.
- The app continues to use the single shared `LoginActivity` as the entry point.

### Removed file

- `mobile/app/src/main/java/com/example/uiprototypebeta/AdminLoginActivity.kt`

This activity was obsolete and no longer needed.

## How The Fix Works End To End

### Fresh sign-in

- User or admin signs in through the shared native sign-in screen.
- The backend returns access, refresh, and role.
- The app saves the session and activates only the correct role in memory.

### App reopen with saved session

- The app restores saved credentials as a pending session only.
- `LoginActivity` validates the refresh token with the backend.
- If valid:
  - the session is activated
  - the user is redirected to the correct dashboard
- If invalid:
  - the saved session is cleared
  - the user stays on the primary sign-in screen

### Expired session during later API use

- If an authenticated API call gets a `401`, the app tries a token refresh once.
- If that refresh is invalid, the saved session is cleared and the request returns a sign-in-again message.
- Protected dashboards now also redirect away if they are opened without a live session.

## Verification Performed

- Built debug app successfully with `:app:assembleDebug`
- Built signed release bundle successfully with `:app:bundleRelease`
- Verified the release AAB is signed
- Verified the upload certificate SHA1 is:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`
- Verified the current Android version config is:
  - `versionCode = 7`
  - `versionName = "7"`

Latest release bundle:

- `mobile/app/build/outputs/bundle/release/app-release.aab`

## Limits

- I did not run a live tap-through on a connected device from this machine because `adb` is not available in this environment.
- I did not change backend auth behavior in this pass.

## Recommended Smoke Test

1. Open the app with no saved session and continue as guest.
2. Open the drawer and confirm only the expected guest routes appear.
3. Sign in as a user and confirm:
   - dashboard opens
   - bookings load
   - Android back no longer loops to login and back again
4. Force-close and reopen the app while signed in and confirm the saved session restores correctly.
5. Sign in as an admin and confirm:
   - admin dashboard opens
   - admin dashboard is still protected after reopen
   - logout returns to the primary shared sign-in screen
6. Test with an expired or revoked token if possible and confirm the app returns cleanly to sign-in instead of acting logged in.
