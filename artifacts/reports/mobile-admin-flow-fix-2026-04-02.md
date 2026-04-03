# Mobile Admin Flow Fix Report

Date: 2026-04-02

## Issues Reported

- Guest navigation still showed both `Admin sign in` and `Sign in`.
- Admin login was redirecting into the web admin page instead of the native admin dashboard shell.
- The top-left control in that admin entry screen was a close/back button, not the app drawer menu.
- Admin logout could fall back into the old web login path instead of the primary native sign-in page.
- After that fallback, logging in again could create a broken loop until the app was reinstalled.

## What I Changed

### 1. Guest nav now shows only one sign-in entry

File:

- `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`

Change:

- Updated the drawer auth UI state so:
  - guest: only `Sign in`
  - user: only `My bookings`
  - admin: only `Admin dashboard`

Why:

- Admin and user now share the same login screen, so the guest drawer should not show a separate admin entry anymore.

### 2. Admin login now lands in the native admin dashboard shell

Files:

- `mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`

Change:

- Changed admin login redirect from `WebAdminActivity` to `AdminDashboardActivity`.
- Changed the admin drawer entry to open `AdminDashboardActivity` as well.

Why:

- `AdminDashboardActivity` is the native screen that already uses the shared app drawer.
- This restores the intended top-left menu behavior for admins.

### 3. Web admin logout now returns to the primary native sign-in page

File:

- `mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt`

Change:

- Added guard logic so `WebAdminActivity` refuses to open if there is no admin session.
- Added login-page detection for `/login` and `/admin/login`.
- After the admin web area has already visited a protected page, any later navigation back to a login page is treated as a real logout.
- On that logout path, the app now clears the persisted mobile session and returns to the primary native `LoginActivity`.

Why:

- This removes the stale web-login fallback that was causing the broken sign-out flow and the loop behavior.

## How I Fixed It

1. Traced where admin login was landing and confirmed it still went straight to `WebAdminActivity`.
2. Moved the main admin landing path back to the native admin dashboard shell.
3. Restricted the drawer UI so guests only see one login action.
4. Added WebView login-route detection so web-admin logout returns to the native login screen and clears the mobile session cleanly.
5. Rebuilt and re-verified the signed release bundle.

## Notification Check

I checked the codebase for mobile/local/push notification support.

What I found:

- I did **not** find Android local notification code.
- I did **not** find FCM / Firebase Messaging / WorkManager / AlarmManager appointment reminder code.
- I **did** find backend booking email logic in:
  - `backend/api/booking_notifications.py`
  - `backend/api/models.py`

Conclusion:

- The app currently appears to send **email notifications** related to booking events through the backend when enabled.
- The backend notification event model only covers:
  - booking created
  - booking rescheduled
  - booking cancelled
  - booking no-show
- The app does **not** appear to send native mobile push notifications or local upcoming-appointment reminder notifications.

No notification feature was added or changed in this fix.

## Verification

Commands run:

```powershell
cd mobile
cmd /c gradlew.bat :app:assembleDebug
cmd /c gradlew.bat :app:bundleRelease -PapiBaseUrl=https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net -PwebBaseUrl=https://t36-comp-3078-w26.vercel.app
jarsigner -verify -verbose -certs mobile/app/build/outputs/bundle/release/app-release.aab
```

Results:

- `:app:assembleDebug` passed
- `:app:bundleRelease` passed
- release AAB signature verified
- release manifest still shows:
  - `versionCode = 6`
  - `versionName = 6`

## Files Changed In This Round

- `mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt`
