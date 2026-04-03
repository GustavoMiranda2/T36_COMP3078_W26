# Play Store Release Guide

This guide is for publishing the Android app to Google Play with:

- mobile app on Google Play
- backend on Azure App Service
- PostgreSQL on Azure Database for PostgreSQL
- web app on Vercel
- booking emails sent from the backend through Resend

## 1. What is already prepared in the code

The app is already configured for production release with:

- production API URL set to `https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net`
- production web URL set to `https://t36-comp-3078-w26.vercel.app`
- privacy policy URL set to `https://stuaarts.github.io/PrivacyPolicy/`
- account deletion URL set to `https://t36-comp-3078-w26.vercel.app/account/delete`
- release builds using `usesCleartextTraffic=false`
- app launcher name set to `Brazdes`

## 2. What the mobile app does and does not do

The Android app does not send emails directly.

The real production flow is:

`Android app -> Azure backend API -> Azure PostgreSQL + Resend email provider`

That means:

- if booking emails work in production, the backend and Resend are working
- Google Play does not change how emails are sent
- the mobile app only needs the correct HTTPS backend URL

## 3. Backend requirements before publishing

Before uploading the Android bundle to Google Play, verify the backend production environment in Azure has all of these values:

```env
DEBUG=false
ALLOWED_HOSTS=brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net
CORS_ALLOWED_ORIGINS=https://t36-comp-3078-w26.vercel.app
DATABASE_URL=postgresql://...@brazdes-db16.postgres.database.azure.com:5432/postgres?sslmode=require
SECRET_KEY=...
JWT_SECRET_KEY=...
FRONTEND_BASE_URL=https://t36-comp-3078-w26.vercel.app
BOOKING_EMAILS_ENABLED=true
BOOKING_REPLY_TO=no-reply@brazdes.site
RESEND_API_KEY=...
RESEND_FROM_EMAIL=no-reply@brazdes.site
RESEND_OWNER_EMAIL=gustavomendoncamiranda@gmail.com
RESEND_WEBHOOK_SECRET=...
BUSINESS_NAME=Brazdes
BUSINESS_ADDRESS=230 Woolner Avenue, Toronto, ON
BUSINESS_PHONE=+1 (437) 717-7785
```

Critical notes:

- `BOOKING_REPLY_TO` must be a real email address, not a placeholder string
- `RESEND_API_KEY` must be valid
- `RESEND_WEBHOOK_SECRET` is recommended so delivery events can be tracked correctly
- `FRONTEND_BASE_URL` should point to the Vercel app so booking links open correctly

## 4. Vercel requirements before publishing

In Vercel, the frontend should have:

```env
NEXT_PUBLIC_API_URL=/api-proxy
NEXT_SERVER_API_PROXY_TARGET=https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net
```

Also confirm:

- production branch is `main`
- the page `https://t36-comp-3078-w26.vercel.app/account/delete` is live

## 5. Create the upload keystore

If you do not already have a release keystore, create one once and keep it safe forever.

Run:

```powershell
cd mobile
keytool -genkeypair -v -keystore release-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

After that:

1. Copy `mobile/keystore.properties.example` to `mobile/keystore.properties`
2. Fill in the real values:

```properties
storeFile=release-upload-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

Store these values securely. Do not lose them.

## 6. Build the release bundle

Google Play should receive an `.aab` file, not a debug APK.

Build it with:

```powershell
cd mobile
.\gradlew.bat :app:clean :app:bundleRelease
```

Expected output:

```text
mobile/app/build/outputs/bundle/release/app-release.aab
```

If you want a release APK for direct testing on devices:

```powershell
cd mobile
.\gradlew.bat :app:assembleRelease
```

Expected output:

```text
mobile/app/build/outputs/apk/release/app-release.apk
```

## 7. Test the release build before upload

Before uploading anything to Google Play, test the release build on a real Android phone.

Minimum checklist:

1. Open the app and confirm it launches normally
2. Sign up with a new client account
3. Sign in with an existing client account
4. Create a booking
5. Confirm the booking shows in `My bookings`
6. Cancel a booking
7. Reschedule a booking
8. Confirm booking confirmation and cancellation emails arrive
9. Sign in as admin and open the native admin dashboard
10. Confirm admin overview and booking data load from the remote backend
11. Open blog and portfolio pages
12. Confirm no screen depends on `localhost`, `127.0.0.1`, or `10.0.2.2`

## 8. Create the Play Console app

In Google Play Console:

1. Create a new app
2. Choose default language
3. Enter app name: `Brazdes`
4. Choose `App` and `Free`
5. Complete the policy declarations

## 9. Prepare the Play Store listing

You will need:

- app name
- short description
- full description
- icon
- feature graphic
- screenshots for phone
- privacy policy URL

Use these URLs:

- Privacy policy: `https://stuaarts.github.io/PrivacyPolicy/`
- Account deletion: `https://t36-comp-3078-w26.vercel.app/account/delete`

## 10. Complete the Play Console policy sections

At minimum, review:

1. App access
2. Ads
3. Content rating
4. Data safety
5. Target audience
6. Health / finance / other special declarations if applicable

For this app, pay close attention to:

- account creation
- appointment data
- email address collection
- possible user-generated content through testimonials

## 11. Upload to internal testing first

Do not publish directly to production first.

Recommended path:

1. Go to `Testing > Internal testing`
2. Create the test release
3. Upload `app-release.aab`
4. Add tester emails
5. Publish the internal test release
6. Install the Play-distributed build from the tester link
7. Repeat the booking + email verification on the real distributed build

## 12. After internal testing passes

When internal testing looks good:

1. Create a production release
2. Upload the same or newer `.aab`
3. Review release notes
4. Send for review

## 13. Common failure points

If something breaks after Play release, check these first:

1. Wrong backend URL in the release build
2. Missing or invalid Azure environment variables
3. Resend API key invalid or sender domain not verified
4. `BOOKING_REPLY_TO` not set to a real email
5. Vercel env variables missing after a redeploy
6. Upload keystore mismatch
7. Version code not incremented for the next upload

## 14. Versioning for the next release

Each new Play upload must increase `versionCode`.

Current app version prepared in the project:

- `versionCode = 12`
- `versionName = 1.0.0`

For the next release after this one, increase the code again.
