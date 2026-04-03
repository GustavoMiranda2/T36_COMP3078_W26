# Mobile Auth Fix Report

Date: 2026-04-02

## Problem Found

The main login failure was in the mobile session flow, not the backend.

- `LoginActivity` was saving the JWT access/refresh tokens and then immediately clearing them when it cleared the opposite role session.
- `UserSession.clear()` and `AdminSession.clear()` were both wiping `ApiClient.accessToken` and `ApiClient.refreshToken`.
- That meant a successful login could still leave the app effectively logged out.
- This matched the symptoms you reported:
  - user and admin sign-in looked broken
  - admin could not open the admin dashboard correctly from mobile nav
  - booking requests could fail with `Authentication credentials were not provided`

## What I Changed

### 1. Replaced the fragile in-memory auth flow with a persisted session store

Files:

- `mobile/app/src/main/java/com/example/uiprototypebeta/AppSessionStore.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/BookingApplication.kt`
- `mobile/app/src/main/AndroidManifest.xml`

What changed:

- Added `AppSessionStore` to persist:
  - role
  - access token
  - refresh token
  - display name
  - user id
  - email
- Added `BookingApplication` so the session is restored when the app starts.
- Registered that application class in the manifest.

Why:

- The web app keeps auth state across navigation and reloads.
- Mobile was only keeping auth in memory, which made it much easier for the session state to drift or disappear.

### 2. Fixed the login screen so successful login no longer destroys its own tokens

File:

- `mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt`

What changed:

- Replaced the direct token assignment + opposite-session clear logic with:
  - `AppSessionStore.saveAdminSession(...)`
  - `AppSessionStore.saveUserSession(...)`
- Added redirect logic so if the app already has a valid session:
  - admins go straight to the admin dashboard
  - users go straight to their bookings or their saved booking draft flow

Why:

- This removes the exact bug that was breaking authenticated requests right after login.
- It also makes the mobile login behavior closer to the web version.

### 3. Decoupled role clearing from token clearing

Files:

- `mobile/app/src/main/java/com/example/uiprototypebeta/UserSession.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/AdminSession.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/ApiClient.kt`

What changed:

- `UserSession.clear()` and `AdminSession.clear()` now clear only role/profile data.
- Token lifecycle moved into the auth layer:
  - `ApiClient.setAuthTokens(...)`
  - `ApiClient.clearAuthTokens()`
- When the access token is refreshed, the new token is also written back to `AppSessionStore`.

Why:

- Session identity and token storage should not be mixed together.
- Clearing one role should not accidentally log the whole app out.

### 4. Fixed drawer/nav role gating

Files:

- `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`
- `mobile/app/src/main/res/layout/activity_drawer_base.xml`
- `mobile/app/src/main/res/values/strings.xml`

What changed:

- The drawer now updates based on the real session:
  - guest: shows `Sign in` and `Admin sign in`
  - signed-in user: shows only `My bookings`
  - signed-in admin: shows only `Admin dashboard`
- Logout now clears the persisted session through `AppSessionStore.clear(...)`.

Why:

- A normal signed-in user should not keep seeing an admin entry in the drawer.
- This matches the separation you asked for and removes the user/admin overlap.

### 5. Updated authenticated account cleanup to use the shared session store

File:

- `mobile/app/src/main/java/com/example/uiprototypebeta/UserDashboardActivity.kt`

What changed:

- Account deletion now clears the whole app session through `AppSessionStore.clear(...)`.

Why:

- It keeps logout/account removal behavior consistent with the new auth model.

## How I Fixed It

1. Traced the mobile login path and confirmed the tokens were being wiped after a successful login.
2. Compared the mobile flow to the web flow to identify the mismatch.
3. Moved auth ownership into a single shared session store.
4. Updated login, logout, token refresh, and drawer behavior to all use the same source of truth.
5. Rebuilt the app to verify the fix compiled in both debug and release modes.

## Verification

Commands run:

```powershell
cd mobile
cmd /c gradlew.bat :app:assembleDebug
cmd /c gradlew.bat :app:bundleRelease -PapiBaseUrl=https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net -PwebBaseUrl=https://t36-comp-3078-w26.vercel.app
```

Results:

- `:app:assembleDebug` passed
- `:app:bundleRelease` passed

## Expected Outcome After This Fix

- User sign-in should keep the JWT tokens instead of clearing them.
- Booking creation and user appointment requests should stop failing with missing auth credentials caused by the mobile client.
- Admin sign-in should be able to open the admin dashboard again.
- A signed-in user should no longer see the admin login/dashboard drawer entry.
- Auth state should survive app relaunches more reliably because it is now restored at startup.

## Notes

- I did not change the backend login API for this fix.
- I did not bump the Android version code in this step.
- If you want to upload this to Play, the next bundle may need a new version code depending on what was already uploaded.
