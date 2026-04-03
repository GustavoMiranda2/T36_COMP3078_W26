# Mobile Admin Dashboard Fix

Date: 2026-04-02

## Problem

The Android app was still using the old native `AdminDashboardActivity` as the primary admin landing page.

That native screen was not the real admin product surface. The real admin experience already existed in the web app under:

- `/admin/dashboard`
- `/admin/dashboard/bookings`
- `/admin/dashboard/services`
- `/admin/dashboard/portfolio`
- `/admin/dashboard/blog`
- `/admin/dashboard/testimonials`
- `/admin/dashboard/analytics`

The result was:

- admin sign-in landed on the wrong dashboard
- the drawer admin entry pointed to the wrong dashboard
- some admin actions only reached the correct dashboard indirectly through buttons inside the old native screen

## Root Cause

The mobile app had two different admin entry concepts:

1. a native Android dashboard screen
2. the real web admin dashboard loaded inside the app

The login flow and drawer used the native one as the default, while some deeper admin actions opened the web one.

That split created an inconsistent admin experience and made the wrong screen look like the primary dashboard.

## What I Changed

### 1. Replaced the old native admin dashboard with a drawer-backed web admin host

File:

- [AdminDashboardActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt)

Main changes:

- `AdminDashboardActivity` now loads the real web admin dashboard instead of rendering the old native dashboard UI.
- It still extends `BaseDrawerActivity`, so it keeps the mobile drawer/menu shell.
- It accepts an admin `title` and `path`, defaulting to:
  - title: `Admin Dashboard`
  - path: `/admin/dashboard`
- It opens the admin web app through a `WebView`, injects the stored admin session into local storage, and redirects into the requested admin route.

Important lines:

- [AdminDashboardActivity.kt:16](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L16)
- [AdminDashboardActivity.kt:38](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L38)
- [AdminDashboardActivity.kt:74](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L74)
- [AdminDashboardActivity.kt:87](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L87)
- [AdminDashboardActivity.kt:136](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L136)

### 2. Added a dedicated layout for the new admin web host

File:

- [content_admin_web.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/content_admin_web.xml)

What it contains:

- a top progress bar for page loading
- a full-height `WebView` inside the existing drawer shell

Important lines:

- [content_admin_web.xml:8](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/content_admin_web.xml#L8)
- [content_admin_web.xml:19](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/content_admin_web.xml#L19)

### 3. Kept `WebAdminActivity` only as a compatibility redirect

File:

- [WebAdminActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt)

What changed:

- It no longer hosts the real admin UI.
- It now forwards any old admin web route launches into the new `AdminDashboardActivity` host.

This keeps older entry points working while making sure the app only has one real admin dashboard implementation.

Important lines:

- [WebAdminActivity.kt:7](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt#L7)
- [WebAdminActivity.kt:16](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt#L16)

### 4. Updated the drawer admin entry so it points to the correct overview

File:

- [BaseDrawerActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt)

What changed:

- The admin drawer action now routes to the real admin overview path.
- If the admin is already inside an admin sub-page, tapping the admin drawer item takes them back to `/admin/dashboard` instead of doing nothing.
- If the admin is already on the overview page, it does not restart the screen.

Important lines:

- [BaseDrawerActivity.kt:131](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt#L131)
- [BaseDrawerActivity.kt:134](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt#L134)
- [BaseDrawerActivity.kt:138](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt#L138)

### 5. Updated admin login redirect to the correct dashboard host

File:

- [LoginActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt)

What changed:

- After successful admin sign-in, the app now launches the new admin web host directly.
- Stored admin-session redirect on app reopen now also goes to the same correct dashboard host.

Important lines:

- [LoginActivity.kt:59](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt#L59)
- [LoginActivity.kt:112](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt#L112)

### 6. Updated admin shortcuts in other screens to use the same dashboard host

Files:

- [BlogActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BlogActivity.kt)
- [PortfolioActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/PortfolioActivity.kt)

What changed:

- Admin manage buttons now open the shared drawer-backed admin host with the correct web path instead of opening the older standalone admin web activity directly.

Important lines:

- [BlogActivity.kt:33](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BlogActivity.kt#L33)
- [PortfolioActivity.kt:59](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/PortfolioActivity.kt#L59)

## Functional Behavior After The Fix

### Admin sign-in

- Admin signs in through the shared sign-in page.
- App launches `AdminDashboardActivity`.
- `AdminDashboardActivity` opens the real admin web dashboard at `/admin/dashboard`.

### Admin drawer behavior

- The admin drawer entry now points to the correct overview dashboard.
- If the admin is already in a deeper admin page, tapping the admin entry returns to the overview.

### Existing admin deep links / older mobile routes

- Any route that still launches `WebAdminActivity` is redirected into the new shared admin dashboard host.

### Admin web logout

- If the admin web session falls back to `/login` or `/admin/login`, the app clears the session and returns to the primary native sign-in page.

### Android back behavior inside admin dashboard

- If the `WebView` has page history, Android back goes to the previous admin page in the `WebView`.
- If there is no `WebView` history, the normal drawer-based back behavior applies.

## Verification I Ran

### Android

- Built debug app successfully with `:app:assembleDebug`
- Built signed release bundle successfully with `:app:bundleRelease`
- Verified the release AAB signature with `jarsigner`
- Verified the upload key SHA1 matches Play Console:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`

### Web

- Built the Next.js app successfully with `npm run build`
- Confirmed the admin routes are present in the build:
  - `/admin/dashboard`
  - `/admin/dashboard/analytics`
  - `/admin/dashboard/bookings`
  - `/admin/dashboard/services`
  - `/admin/dashboard/portfolio`
  - `/admin/dashboard/blog`
  - `/admin/dashboard/testimonials`

Relevant web route references:

- [page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/page.tsx)
- [admin-ui.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/admin-ui.tsx#L50)

## Release Output

Updated Android version:

- [build.gradle.kts](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build.gradle.kts#L45)
  - `versionCode = 9`
  - `versionName = "9"`

Latest bundle:

- [app-release.aab](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build/outputs/bundle/release/app-release.aab)

## Limits

- I did not run a live tap-through on a physical device from this machine.
- I validated the route wiring, build integrity, and signing locally, but the last confirmation should still be one quick admin login smoke test on-device:
  - admin sign-in
  - admin overview
  - drawer to home/blog/portfolio
  - drawer back to admin overview
  - admin sub-page navigation
  - admin sign-out
