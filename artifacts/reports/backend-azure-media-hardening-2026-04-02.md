# Backend Azure Media Hardening

Date: 2026-04-02

## Scope

This change set addressed two backend production risks:

1. Admin-uploaded images were stored only on the app's local disk.
2. The public Azure backend needed to be rechecked after deployment to confirm the admin delete endpoints were live.

## What Changed

### 1. Durable media storage support

Files:

- [settings.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/config/settings.py)
- [urls.py](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/config/urls.py)
- [requirements.txt](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/requirements.txt)
- [.env.example](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/.env.example)
- [README.md](/c:/Users/Lucas/College/T36_COMP3078_W26/backend/README.md)

Changes:

- Added `django-storages[azure]` to backend dependencies.
- Added Azure Blob media settings:
  - `AZURE_CONNECTION_STRING`
  - `AZURE_ACCOUNT_NAME`
  - `AZURE_ACCOUNT_KEY`
  - `AZURE_SAS_TOKEN`
  - `AZURE_MEDIA_CONTAINER`
  - `AZURE_MEDIA_LOCATION`
  - `AZURE_MEDIA_CUSTOM_DOMAIN`
  - `AZURE_MEDIA_URL_EXPIRATION_SECS`
  - `AZURE_MEDIA_OVERWRITE_FILES`
- Switched Django to `STORAGES` configuration so media can use:
  - local `FileSystemStorage` in development
  - `storages.backends.azure_storage.AzureStorage` when Azure media credentials are present
- Added `SERVE_LOCAL_MEDIA_FILES` so Django only mounts local `/media/...` routes when local filesystem storage is active.

Result:

- In development, existing local media behavior still works.
- In production, once Azure Blob credentials are configured, uploaded images stop depending on App Service local disk and will use durable blob storage URLs instead.

### 2. Backend delete route confirmation

The deployed Azure API was rechecked after the GitHub Actions deployment completed.

Confirmed live:

- `OPTIONS /admin/services/<id>` now returns:
  - `Allow: GET, PUT, PATCH, DELETE, HEAD, OPTIONS`

That means the admin delete endpoint change is now active in production.

## Validation

### Local validation

Commands run:

```powershell
cd backend
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m py_compile config\settings.py config\urls.py api\tests.py
.\.venv\Scripts\python.exe manage.py test api
```

Result:

- dependency install succeeded
- Python compile check succeeded
- Django test suite passed: `15` tests

Additional settings smoke tests:

- Local fallback branch:
  - `USE_AZURE_MEDIA_STORAGE=False`
  - `SERVE_LOCAL_MEDIA_FILES=True`
  - default storage = `FileSystemStorage`
- Azure branch with a dummy connection string:
  - `USE_AZURE_MEDIA_STORAGE=True`
  - `SERVE_LOCAL_MEDIA_FILES=False`
  - default storage = `AzureStorage`

### Live endpoint validation

Command checked:

```powershell
curl.exe -i -X OPTIONS "https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net/admin/services/00000000-0000-0000-0000-000000000000"
```

Observed:

- `Allow: GET, PUT, PATCH, DELETE, HEAD, OPTIONS`

## Remaining External Requirement

The Azure Blob storage path is now implemented in code, but it still requires real Azure App Service environment variables to be set before production uploads become durable:

- `AZURE_CONNECTION_STRING` or equivalent account credentials
- `AZURE_MEDIA_CONTAINER`

The container should already exist and should allow public read access if the app needs stable public image URLs for mobile/web previews without expiring signatures.
