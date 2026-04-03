# Mobile Admin Upload And Delete Fix

Date: 2026-04-02

## Scope

This change set addressed two admin-facing gaps:

1. In the mobile app, the admin dashboard could not open the device file picker from the web admin image upload controls.
2. The admin UI did not expose delete actions for services, add-ons, portfolio items, and blog posts.

The work touched Android, web admin pages, and the backend admin detail views.

## Root Causes

### 1. Mobile file upload

The admin dashboard runs inside an Android `WebView`.  
The web UI already renders `<input type="file">` controls, but the Android host did not implement `WebChromeClient.onShowFileChooser(...)`.

Result:

- tapping "Choose file" inside the admin UI did nothing in the mobile app

### 2. Missing delete actions

The web admin pages only exposed create/edit flows.  
At the API layer, the frontend had no delete helpers, and the backend admin detail views were update-only.

Result:

- no visible delete button for services
- no visible delete button for add-ons
- no visible delete button for portfolio items
- no visible delete button for blog posts

## What Changed

### Android: file chooser support in the mobile admin dashboard

File:

- [AdminDashboardActivity.kt](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt)

Changes:

- Added a native file chooser bridge for the admin `WebView`
- Enabled `allowFileAccess` and `allowContentAccess`
- Implemented `WebChromeClient.onShowFileChooser(...)`
- Added an `ActivityResultLauncher` to return selected files back to the web page
- Supported single-file and multi-file responses
- Used `ACTION_GET_CONTENT` with MIME type forwarding from the page
- Cleared pending callbacks safely on destroy

Relevant lines:

- [AdminDashboardActivity.kt:28](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L28)
- [AdminDashboardActivity.kt:70](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L70)
- [AdminDashboardActivity.kt:78](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L78)
- [AdminDashboardActivity.kt:174](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L174)
- [AdminDashboardActivity.kt:199](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/src/main/java/com/example/uiprototypebeta/AdminDashboardActivity.kt#L199)

### Backend: enabled DELETE on admin detail endpoints

File:

- [views.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py)

Changes:

- changed these views from `RetrieveUpdateAPIView` to `RetrieveUpdateDestroyAPIView`
  - `AdminServiceDetailView`
  - `AdminAddOnDetailView`
  - `AdminPortfolioItemDetailView`
  - `AdminBlogPostDetailView`

Relevant lines:

- [views.py:607](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py#L607)
- [views.py:621](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py#L621)
- [views.py:633](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py#L633)
- [views.py:650](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py#L650)

### Web API layer: added admin delete helpers

File:

- [api.ts](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts)

Changes:

- added:
  - `apiDeleteAdminService(...)`
  - `apiDeleteAdminAddOn(...)`
  - `apiDeleteAdminPortfolioItem(...)`
  - `apiDeleteAdminBlogPost(...)`

Relevant lines:

- [api.ts:475](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts#L475)
- [api.ts:506](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts#L506)
- [api.ts:540](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts#L540)
- [api.ts:571](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/api.ts#L571)

### Web admin UI: added a reusable delete-button style

File:

- [admin-ui.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/admin-ui.tsx)

Changes:

- added `dangerButtonClass` for destructive row actions

Relevant line:

- [admin-ui.tsx:16](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/admin-ui.tsx#L16)

### Web admin page: services and add-ons

File:

- [services/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx)

Changes:

- added delete handlers for services and add-ons
- added confirmation prompts before deletion
- added delete buttons to the existing editable rows
- reset the current edit form if the deleted item was being edited
- reload the page data after delete
- show delete-state labels such as `Deleting...`

Relevant lines:

- [services/page.tsx:238](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx#L238)
- [services/page.tsx:260](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx#L260)
- [services/page.tsx:446](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx#L446)
- [services/page.tsx:566](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/services/page.tsx#L566)

### Web admin page: portfolio

File:

- [portfolio/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/portfolio/page.tsx)

Changes:

- added delete handler
- added confirmation prompt
- added delete button to each portfolio row
- resets active edit state when the deleted item was open in the form

Relevant lines:

- [portfolio/page.tsx:157](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/portfolio/page.tsx#L157)
- [portfolio/page.tsx:308](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/portfolio/page.tsx#L308)

### Web admin page: blog

File:

- [blog/page.tsx](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/blog/page.tsx)

Changes:

- added delete handler
- added confirmation prompt
- added delete button to each blog post row
- resets active edit state when the deleted post was open in the form

Relevant lines:

- [blog/page.tsx:162](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/blog/page.tsx#L162)
- [blog/page.tsx:316](/c:/Users/Lucas/College/T36_COMP3078_W26/web/src/app/admin/dashboard/blog/page.tsx#L316)

## Functional Result

After this change:

- the admin can tap the file upload control inside the mobile admin dashboard and choose an image from the device
- the admin can delete:
  - services
  - add-ons
  - portfolio items
  - blog posts

## Important Behavior Note

Service deletion can still be blocked by existing booking data, because the backend model uses protected relationships for appointments.

That means:

- the delete button now exists
- the backend will allow deletion when safe
- if a service is still referenced by protected records, the delete request will return an error instead of silently breaking data integrity

## Verification Performed

### Android

- built debug app successfully with `:app:assembleDebug`
- built signed release bundle successfully with `:app:bundleRelease`
- verified release signing with `jarsigner`
- verified upload certificate SHA1:
  - `55:20:93:62:26:70:48:FE:B0:55:0F:FF:11:E6:F1:CC:D0:50:41:A1`

### Web

- built the web app successfully with `npm run build`

### Backend

- ran Python syntax compilation on [views.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/api/views.py)

## Release Output

Updated Android version:

- [build.gradle.kts](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build.gradle.kts#L45)
  - `versionCode = 10`
  - `versionName = "10"`

Latest bundle:

- [app-release.aab](/c:/Users/Lucas/College/T36_COMP3078_W26/mobile/app/build/outputs/bundle/release/app-release.aab)

## Limits

- I did not perform a live phone tap-through from this machine.
- I did not run Django runtime tests because the local backend Python environment here is not fully installed, but the changed backend file passed syntax compilation.
