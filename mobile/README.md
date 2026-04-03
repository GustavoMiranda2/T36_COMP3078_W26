# Brazdes Android App

This module contains the Android app that talks to:

- Azure backend API: `https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net`
- Vercel web app: `https://t36-comp-3078-w26.vercel.app`

## Local development

Debug builds already point to the remote stack through `gradle.properties`.

Build a debug APK:

```powershell
cd mobile
.\gradlew.bat :app:assembleDebug
```

## Release defaults

Release builds are preconfigured to use:

- `apiBaseUrl=https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net`
- `webBaseUrl=https://t36-comp-3078-w26.vercel.app`
- `privacyPolicyUrl=https://stuaarts.github.io/PrivacyPolicy/`
- `accountDeletionUrl=https://t36-comp-3078-w26.vercel.app/account/delete`

## Play Store release

Before generating the final Play Store bundle, create your signing files:

1. Generate or obtain the upload keystore.
2. Create `mobile/keystore.properties` from `mobile/keystore.properties.example`.
3. Build the release bundle:

```powershell
cd mobile
.\gradlew.bat :app:bundleRelease
```

The full publication checklist is in [PLAYSTORE_RELEASE.md](/c:/capstone/T36_COMP3078_W26/mobile/PLAYSTORE_RELEASE.md).
