# Mobile Admin Login Loop Fix

Date: 2026-04-06

## Problem

Admin sign-in in the mobile app was immediately bouncing back to the main sign-in screen instead of opening the admin dashboard. The same area was also involved in the bad logout flow from the admin top bar.

## Root Cause

The mobile admin `WebView` guard in `AdminDashboardActivity.kt` was treating any visit to `/login` or `/admin/login` as a real logout. That was too aggressive.

During the admin handoff, the app intentionally loads the web login page first, injects the native admin tokens into `localStorage`, and then redirects into `/admin/dashboard`. With the previous guard, that login route was sometimes interpreted as a logout before the web admin session had fully settled, so the app cleared the native session and sent the admin back to the primary sign-in page.

## What Changed

File updated:

- `mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt`

Behavior change:

- The `WebView` guard no longer treats the login route as an automatic logout.
- If the web view is on `/login` or `/admin/login` but still has a valid admin token in `localStorage`, it now redirects back to the intended admin destination instead of clearing the session.
- Real session expiry still logs the admin out correctly through the existing `401` handling and native session clear path.

Version bump:

- `mobile/app/build.gradle.kts`
  - `versionCode = 19`
  - `versionName = "19"`

## Validation

Commands run:

- `gradlew.bat :app:assembleDebug :app:bundleRelease`
- `bundletool dump manifest`
- `keytool -printcert -jarfile app-release.aab`
- `jarsigner -verify -verbose -certs app-release.aab`

Verified results:

- Package: `com.brazwebdes.hairstylistbooking`
- Version code: `19`
- Version name: `19`
- Signing SHA1: `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`

## Output

Latest signed bundle:

- `mobile/app/build/outputs/bundle/release/app-release.aab`
