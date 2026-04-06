# Mobile Update Session Fix

Date: 2026-04-03

## Goal

Fix the mobile bug where, after updating the app from Play, a user could open the app and be automatically signed into the admin account and redirected into Erik's dashboard without entering credentials.

Also rebuild the main release bundle as version `15` using `app-release.aab`.

## Root Cause

There were two issues in the session restore logic:

1. The app only cleared saved auth on version change if a stored app-version marker already existed.
   - If a user updated from an older build that had saved tokens but did not yet have that marker, the app treated the saved session as valid legacy state and restored it.

2. The version check only ran from `BookingApplication.onCreate()`.
   - That is a good first layer, but it was too narrow for a release-safety fix.
   - Running the same check from the foreground screens makes the logout behavior more defensive and prevents stale auth from surviving into the first opened screen.

There was also a cleanup timing weakness:

3. `WebSessionStore.clear()` always posted its cleanup work asynchronously to the main thread.
   - That made the admin `WebView` session cleanup less deterministic during startup/logout flows.

## Code Changes

### 1. Hardened version-based session invalidation

Updated:
- `mobile/app/src/main/java/com/example/uiprototypebeta/AppSessionStore.kt`

Changes:
- Added `enforceCurrentAppVersion(context)` as a reusable foreground safety check.
- Changed update detection so the app now resets persisted auth when:
  - the stored version marker is missing, or
  - the stored version marker does not match `BuildConfig.VERSION_CODE`.
- Changed auth clearing to remove only auth/session fields while preserving the current version marker.
- Wrote the current app version marker back immediately with `commit()`.
- Stored the current version marker whenever a new session is persisted.

Why:
- This closes the legacy-upgrade gap where old saved tokens could survive because the version marker was missing.

### 2. Enforced the version/session check from UI entry points

Updated:
- `mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt`
- `mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt`

Changes:
- `BaseDrawerActivity.onCreate()` now runs `AppSessionStore.enforceCurrentAppVersion(this)` before loading drawer UI.
- `BaseDrawerActivity.onResume()` now runs the same check before refreshing drawer auth state.
- `LoginActivity.onCreate()` now runs the same check before redirecting or restoring sessions.

Why:
- This makes update logout behavior apply at the actual screens users open, not only at application startup.
- If a user opens the app after an update, the login screen becomes the authoritative entry point again.

### 3. Made WebView session cleanup more immediate

Updated:
- `mobile/app/src/main/java/com/example/uiprototypebeta/WebSessionStore.kt`

Changes:
- If already on the main thread, WebView/cookie/storage cleanup now runs immediately.
- If not on the main thread, it still posts safely to the main thread.

Why:
- This reduces cleanup races around admin web-session persistence.

### 4. Bumped release version

Updated:
- `mobile/app/build.gradle.kts`

Changes:
- `versionCode = 15`
- `versionName = "15"`

## Build Output

Release bundle:
- `mobile/app/build/outputs/bundle/release/app-release.aab`

## Verification Performed

### Android builds

Ran:

```powershell
cmd /c gradlew.bat :app:assembleDebug
cmd /c gradlew.bat :app:bundleRelease -PapiBaseUrl=https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net -PwebBaseUrl=https://t36-comp-3078-w26.vercel.app
```

Result:
- both builds succeeded

### Bundle validation

Ran:

```powershell
jarsigner -verify -verbose -certs mobile/app/build/outputs/bundle/release/app-release.aab
keytool -printcert -jarfile mobile/app/build/outputs/bundle/release/app-release.aab
java -jar artifacts/tools/bundletool-all-1.18.2.jar dump manifest --bundle=mobile/app/build/outputs/bundle/release/app-release.aab --module=base
java -jar artifacts/tools/bundletool-all-1.18.2.jar validate --bundle=mobile/app/build/outputs/bundle/release/app-release.aab
```

Confirmed:
- bundle is signed
- upload key SHA1 matches Play:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`
- package name:
  - `com.brazwebdes.hairstylistbooking`
- version code:
  - `15`
- version name:
  - `15`
- `allowBackup="false"`
- `targetSdkVersion="36"`

## Practical Effect

After this change:
- a user updating from an older build with saved tokens should no longer be silently restored into Erik's admin session just because the older install did not have the version marker yet
- when the app version changes, both native auth state and persisted web admin session artifacts are cleared
- the app should land on the sign-in flow again instead of redirecting into the admin dashboard automatically

## Remaining Limit

I verified the logic, build, signature, and bundle metadata locally.

I did not perform a real Play-delivered update test on a physical device from this machine, so the final confirmation should still be:

1. install the previous public build
2. sign in as admin
3. update to this new build
4. open the app
5. confirm it opens at the sign-in screen instead of the admin dashboard
