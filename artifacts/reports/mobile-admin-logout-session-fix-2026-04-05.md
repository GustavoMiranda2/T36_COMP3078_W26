# Mobile Admin Logout and Session Expiry Fix

Date: 2026-04-05

## Goal

Fix two mobile issues:

1. In the admin dashboard WebView, the top web header sign-out button was sending the app into the wrong sign-in flow and could break later logins.
2. After a session expired, users could remain in a broken state instead of being returned cleanly to the primary sign-in screen.

Also rebuild the Play bundle as version `18`.

## Root Cause

### 1. Admin WebView logout path

The mobile admin dashboard is a native Android screen that loads the web admin app inside a `WebView`.

The web admin header still exposed its own `Sign out` button. That button was part of the web app, not the native Android session flow. Because of that:

- it could navigate inside the web app without clearing native mobile auth at the right moment
- it could land on the wrong login route inside the embedded web UI
- native and web session state could drift apart

### 2. Session expiry on mobile

For native user flows, the app already cleared stored auth when token refresh failed, but some screens only showed an error toast and did not actively send the user back to the primary sign-in screen.

That left the app in a stale authenticated UI state.

## Changes Made

### A. Hardened the admin WebView inside mobile

Updated:
- `mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt`

Changes:
- Added an Android JavaScript bridge to the admin `WebView`.
- Injected a mobile-only script into the loaded admin pages.
- Hid the web header inside the mobile admin WebView so the duplicated top web bar no longer remains visible in the app.
- Intercepted web `Sign out` button clicks inside the WebView and routed them back to the native sign-in flow.
- Wrapped `window.fetch` inside the WebView so `401` responses trigger session cleanup and a return to the native sign-in screen.
- Added extra route/state guards so if the embedded admin UI ends up at `/login` or `/admin/login`, the app returns to the main native sign-in screen.
- Added a re-entry guard so the native return-to-login action only happens once.

Practical effect:
- the top web sign-out control is no longer left active as a broken path inside mobile
- admin logout now returns to the correct native sign-in screen
- expired admin sessions in the WebView now exit cleanly instead of drifting into a broken or stale state

### B. Added a shared native auth-expiry redirect helper

Added:
- `mobile/app/src/main/java/com/example/uiprototypebeta/SessionExpiryHandler.kt`

Changes:
- Created `handleAuthExpiry(message)` to detect auth-expiry style backend errors such as:
  - `Session expired`
  - `Authentication credentials were not provided`
  - `Saved session not found`
- When detected, it:
  - clears the app session
  - shows a user-facing toast
  - sends the app to the primary `LoginActivity`
  - clears the back stack

### C. Applied auth-expiry redirect to affected native user flows

Updated:
- `mobile/app/src/main/java/com/example/uiprototypebeta/UserDashboardActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/BookingScheduleActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/PortfolioActivity.kt`

Changes:
- `UserDashboardActivity`
  - redirect on expired session while loading appointments
  - redirect on expired session while cancelling appointments
  - redirect on expired session while deleting the user account
- `BookingScheduleActivity`
  - redirect on expired session while creating a booking
  - redirect on expired session while rescheduling a booking
- `PortfolioActivity`
  - redirect on expired session while submitting a testimonial

Practical effect:
- if a user session expires during normal mobile use, the app now returns to the main sign-in screen instead of leaving the user in a broken partially-authenticated UI

### D. Bumped release version

Updated:
- `mobile/app/build.gradle.kts`

New values:
- `versionCode = 18`
- `versionName = "18"`

## Build Verification

Ran:

```powershell
cmd /c gradlew.bat :app:assembleDebug
cmd /c gradlew.bat :app:bundleRelease
```

Verified on the built bundle:

- package: `com.brazwebdes.hairstylistbooking`
- version code: `18`
- version name: `18`
- signing SHA1:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`

Bundle path:

- `mobile/app/build/outputs/bundle/release/app-release.aab`

## Important Scope Note

This fix was implemented in the mobile app itself so it works against the currently deployed admin web app loaded inside the WebView.

That means the user does not need to wait for a separate web redeploy just to fix the admin sign-out behavior inside the Android app.
