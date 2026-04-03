# Full Chat Handoff Summary

Date: 2026-04-02

## Scope

This report summarizes the full working session across backend, web, mobile, Play Console release preparation, Azure deployment checks, signing, screenshots, auth fixes, admin dashboard fixes, upload/delete fixes, and backend storage hardening.

It also includes the main literal values that were explicitly used or shared during the chat.

## High-Level Outcome

By the end of the session:

- backend, frontend, and emulator flows were run
- Play Store screenshots were generated
- Android release signing/package/version issues were resolved
- package name was updated to the Play-required value
- multiple mobile auth, route, dashboard, navbar, admin, upload, and delete issues were fixed
- the latest mobile bundle was rebuilt as version `12`
- backend admin delete support was deployed live to Azure
- backend durable Azure Blob media storage support was added in code

## Major Deliverables

### 1. Runtime and screenshot setup

Completed:

- ran backend locally
- ran frontend locally
- ran the mobile app in emulator
- generated Play Console screenshot sets for:
  - phone
  - 7-inch tablet
  - 10-inch tablet

Artifacts:

- [artifacts/screenshots](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/screenshots)
- [artifacts/run-logs](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/run-logs)

### 2. Play submission readiness review

Reviewed and documented:

- production URLs vs emulator URLs
- HTTPS / cleartext handling
- Azure/Django proxy settings
- persistent media storage need
- web frontend dependency for admin/dashboard/email links
- production env vars required in Azure
- account deletion requirement
- privacy policy hosting/linking
- Play bundle signing/upload key issues
- target SDK / package name / version code requirements

### 3. Mobile production release preparation

The mobile app was prepared for release by:

- building signed Android App Bundles
- fixing package/signing/version issues
- switching release builds to real production URLs
- using the correct Play upload key
- changing the package name to:
  - `com.brazwebdes.hairstylistbooking`

### 4. Mobile UI/system bar fixes

The app was adjusted to handle Pixel 9 Pro / Pixel 9 Pro XL safe-area and system bar overlap issues. The early broad edge-to-edge fix was reverted and replaced with a narrower toolbar/drawer inset solution.

### 5. Mobile auth/session/dashboard fixes

Auth and session handling were reworked to fix:

- broken user/admin sign-in behavior
- token/session clearing bugs
- role-based navigation issues
- admin sign-out loop behavior
- dead-end and stale route behavior
- protected screen re-entry after app reopen

### 6. Admin dashboard correction

The app originally used the wrong admin landing dashboard. It was changed so the app uses the correct admin dashboard flow as the primary admin screen.

### 7. Admin mobile upload and delete support

The admin mobile WebView and web admin UI were updated so that:

- `Choose file` can open the Android device picker from mobile
- delete actions are visible in the admin UI for:
  - services
  - add-ons
  - portfolio items
  - blog posts

### 8. Backend hardening

Backend work added:

- live admin `DELETE` support on the API
- media serving path correction for local mode
- Azure Blob durable storage support in Django settings
- tests covering admin content deletion
- updated backend docs/env template

## Chronological Work Log

### Phase 1. Local run + screenshots

Completed:

- local backend env file created so Django could boot on this machine
- backend and frontend run locally
- emulator/mobile run performed
- store screenshot assets generated

### Phase 2. Initial Play deployment guidance

Reviewed:

- Azure backend dependencies
- mobile release URL requirements
- HTTPS requirements
- Django production settings
- persistent media risks
- admin/web dependencies
- account deletion / privacy policy

### Phase 3. Mobile production preparation

Implemented/rebuilt over several iterations:

- release URL wiring
- privacy policy URL handling
- package name update
- signed AAB generation
- upload keystore correction
- version increments to satisfy Play upload rules

### Phase 4. Navigation/system bar/UI fixes

Adjusted:

- drawer/top bar behavior
- top inset handling
- bottom drawer overlap handling
- Pixel 9 Pro / XL navbar conflict

### Phase 5. Auth and session fixes

Resolved:

- sign-in path mismatch between user/admin
- admin/user session clearing conflict
- user booking auth failures caused by token/session logic
- stale admin route behavior
- back-navigation loops
- restored-session validation problems

### Phase 6. Admin dashboard fixes

Resolved:

- wrong admin dashboard landing screen
- incorrect admin navigation shell
- logout returning to the wrong screen
- admin route flow into the correct dashboard pages

### Phase 7. Admin upload/delete fixes

Resolved:

- mobile `Choose file` not opening from WebView
- missing visible delete controls in admin web UI
- backend delete methods missing in earlier deployment

### Phase 8. Backend Azure checks

Checked:

- GitHub Actions Azure deployment workflow
- live production endpoint methods
- live backend deploy status
- media route behavior in production

Result:

- live delete endpoints are now deployed
- durable blob storage support has been added in code
- actual Blob storage activation still depends on Azure App Service env vars

## Main Files Touched During The Session

### Mobile

- [mobile/app/build.gradle.kts](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build.gradle.kts)
- [mobile/app/src/main/AndroidManifest.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/AndroidManifest.xml)
- [mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BaseDrawerActivity.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/LoginActivity.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/SignUpActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/SignUpActivity.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/UserDashboardActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/UserDashboardActivity.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/ApiClient.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/ApiClient.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/AppSessionStore.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AppSessionStore.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/BookingApplication.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/BookingApplication.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/UiUtils.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/UiUtils.kt)
- [mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/WebAdminActivity.kt)
- [mobile/app/src/main/res/layout/activity_drawer_base.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/activity_drawer_base.xml)
- [mobile/app/src/main/res/layout/activity_login.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/activity_login.xml)
- [mobile/app/src/main/res/layout/activity_sign_up.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/activity_sign_up.xml)
- [mobile/app/src/main/res/layout/content_admin_web.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/content_admin_web.xml)
- [mobile/app/src/main/res/layout/content_user.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/layout/content_user.xml)
- [mobile/app/src/main/res/menu/drawer_menu.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/menu/drawer_menu.xml)
- [mobile/app/src/main/res/values/strings.xml](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/res/values/strings.xml)

### Web

- [web/src/app/admin/dashboard/admin-ui.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/admin-ui.tsx)
- [web/src/app/admin/dashboard/services/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx)
- [web/src/app/admin/dashboard/portfolio/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/portfolio/page.tsx)
- [web/src/app/admin/dashboard/blog/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/blog/page.tsx)
- [web/src/app/api.ts](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts)
- [web/src/app/login/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/login/page.tsx)
- [web/src/app/signup/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/signup/page.tsx)
- [web/src/app/privacy-policy/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/privacy-policy/page.tsx)
- [web/src/app/account/delete/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/account/delete/page.tsx)

### Backend

- [backend/config/settings.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/config/settings.py)
- [backend/config/urls.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/config/urls.py)
- [backend/api/views.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py)
- [backend/api/urls.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/urls.py)
- [backend/api/tests.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/tests.py)
- [backend/requirements.txt](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/requirements.txt)
- [backend/.env.example](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/.env.example)
- [backend/README.md](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/README.md)

### Reports and artifacts

- [artifacts/reports/mobile-auth-fix-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/mobile-auth-fix-2026-04-02.md)
- [artifacts/reports/mobile-admin-flow-fix-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/mobile-admin-flow-fix-2026-04-02.md)
- [artifacts/reports/mobile-auth-route-hardening-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/mobile-auth-route-hardening-2026-04-02.md)
- [artifacts/reports/mobile-admin-dashboard-fix-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/mobile-admin-dashboard-fix-2026-04-02.md)
- [artifacts/reports/mobile-admin-upload-delete-fix-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/mobile-admin-upload-delete-fix-2026-04-02.md)
- [artifacts/reports/backend-azure-media-hardening-2026-04-02.md](/c:/Users/Lucas/College/T36_COMP3078_W26/artifacts/reports/backend-azure-media-hardening-2026-04-02.md)

## Latest Confirmed Outputs

### Latest mobile bundle

- [app-release.aab](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build/outputs/bundle/release/app-release.aab)

Latest confirmed metadata:

- package: `com.brazwebdes.hairstylistbooking`
- versionCode: `12`
- versionName: `12`
- signing SHA1: `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`

### Latest backend deployment

Latest backend deploy commit:

- `d74fb35caf1f53fd682a419cb03e1fae7a3f82e0`

Latest backend GitHub Actions run:

- workflow run `#8`
- status: `success`

## Literal Values And Hard Text Used During The Chat

Warning:

This section contains sensitive values that were explicitly shared during the chat, including secret/config values. Rotate them if they should no longer be trusted.

### Production URLs

- API: `https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net`
- Web: `https://t36-comp-3078-w26.vercel.app`
- Privacy policy: `https://stuaarts.github.io/PrivacyPolicy/`

### Package and release values

- package name: `com.brazwebdes.hairstylistbooking`
- version progression used in the session:
  - `2`
  - `3`
  - `4`
  - `5`
  - `6`
  - `7`
  - `8`
  - `9`
  - `10`
  - `11`
  - `12`

### Signing / Play values

- expected Play SHA1:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`
- wrong generated key SHA1 that was rejected:
  - `D5:7C:87:75:19:CB:F3:2D:DE:A6:A8:13:E0:72:DE:69:55:93:D4:F1`
- matching keystore alias:
  - `upload`

### Keystore values shared in the chat

- matching keystore path on the other laptop:
  - `Z:\T36_COMP3078_W26\mobile\upload-keystore.jks`
- matching keystore properties path:
  - `Z:\T36_COMP3078_W26\mobile\keystore.properties`
- releaseStorePassword:
  - `40KxduT2eUAg5n7oSE1BhR8m`
- releaseKeyPassword:
  - `40KxduT2eUAg5n7oSE1BhR8m`

### Secret/config values shared in the chat

- `SECRET_KEY`
  - `ronaldo**1234##ojusto`
- `DATABASE_URL`
  - `postgresql://ojusto:ronaldo1234%23%23@brazdes-db16.postgres.database.azure.com:5432/postgres?sslmode=require`

### Current local backend env text

Current local env file contents:

```env
DEBUG=true
SECRET_KEY=local-dev-secret-key-for-capstone
ALLOWED_HOSTS=localhost,127.0.0.1,10.0.2.2
CORS_ALLOWED_ORIGINS=http://127.0.0.1:3000,http://localhost:3000
DATABASE_URL=postgresql://capstone_user:capstone_pass@localhost:5432/capstone
JWT_SECRET_KEY=local-dev-jwt-secret
FRONTEND_BASE_URL=http://127.0.0.1:3000
BUSINESS_NAME=BrazWebDes Hairstylist Booking
BUSINESS_ADDRESS=230 Woolner Avenue, Toronto, ON
BUSINESS_PHONE=+1 000 000 0000
BOOKING_REPLY_TO=support@example.com
BOOKING_EMAILS_ENABLED=false
```

### Latest production-ready env template text

```env
DEBUG=true
SECRET_KEY=<set-a-strong-secret-key>
ALLOWED_HOSTS=localhost,127.0.0.1,10.0.2.2
CORS_ALLOWED_ORIGINS=http://127.0.0.1:3000,http://localhost:3000
CSRF_TRUSTED_ORIGINS=http://127.0.0.1:3000,http://localhost:3000
DATABASE_URL=postgresql://capstone_user:capstone_pass@localhost:5432/capstone
JWT_SECRET_KEY=<optional-jwt-secret-key>
FRONTEND_BASE_URL=http://127.0.0.1:3000
MEDIA_URL=/media/
USE_X_FORWARDED_HOST=false
USE_SECURE_PROXY_SSL_HEADER=false
SECURE_SSL_REDIRECT=false
SESSION_COOKIE_SECURE=false
CSRF_COOKIE_SECURE=false
SECURE_HSTS_SECONDS=0
SECURE_HSTS_INCLUDE_SUBDOMAINS=false
SECURE_HSTS_PRELOAD=false
SECURE_REFERRER_POLICY=same-origin
BUSINESS_NAME=Brazdes
BUSINESS_ADDRESS=<business-address>
BUSINESS_PHONE=<business-phone>
BOOKING_REPLY_TO=<reply-to-email>
BOOKING_EMAILS_ENABLED=true
RESEND_API_KEY=<resend-api-key>
RESEND_FROM_EMAIL=<from-email>
RESEND_OWNER_EMAIL=<owner-email>
RESEND_WEBHOOK_SECRET=<optional-webhook-secret>
AZURE_CONNECTION_STRING=<optional-azure-blob-connection-string>
AZURE_ACCOUNT_NAME=<optional-azure-storage-account-name>
AZURE_ACCOUNT_KEY=<optional-azure-storage-account-key>
AZURE_SAS_TOKEN=<optional-azure-storage-sas-token>
AZURE_MEDIA_CONTAINER=media
AZURE_MEDIA_LOCATION=
AZURE_MEDIA_CUSTOM_DOMAIN=
AZURE_MEDIA_URL_EXPIRATION_SECS=
AZURE_MEDIA_OVERWRITE_FILES=false
```

## Current Open Item

The backend now supports durable Azure Blob media storage in code, but it is not fully active in production until Azure App Service is configured with real Blob storage env vars such as:

- `AZURE_CONNECTION_STRING`
- `AZURE_MEDIA_CONTAINER`

or equivalent account-based Azure storage credentials.

Without those, the production backend will still use local app storage for uploaded images.
