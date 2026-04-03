# Running The Project

This repo has 3 apps:

- `backend` = Django + DRF API
- `web` = Next.js web app
- `mobile` = Android app

## 1. Start the database

From the repo root:

```powershell
docker compose up -d db
```

Postgres runs on:

- `localhost:5432`

## 2. Run the backend

From the repo root:

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver 127.0.0.1:8000
```

Backend URL:

- `http://127.0.0.1:8000`

Useful backend commands:

```powershell
cd backend
.\.venv\Scripts\Activate.ps1
python manage.py seed_data --flush
python manage.py check
```

## 3. Run the web app

From the repo root:

```powershell
cd web
npm install
npm run dev -- --webpack
```

Web URL:

- `http://127.0.0.1:3000`

Important:

- Use `--webpack` for this repo. That is the current working dev command.
- If PowerShell blocks `npm.ps1`, use:

```powershell
cd web
cmd /c npm install
cmd /c npm run dev -- --webpack
```

## 4. Mobile notes

Mobile debug is currently configured to use the remote stack by default:

- API: `https://brazdes-api-cvedaxfjc6gedwhn.canadacentral-01.azurewebsites.net`
- Web support pages: `https://t36-comp-3078-w26.vercel.app`

To build Android locally, make sure the Android SDK is configured first:

- set `ANDROID_HOME`, or
- create `mobile/local.properties` with `sdk.dir=...`

Then run:

```powershell
cd mobile
.\gradlew.bat :app:assembleDebug
```

## 5. Login notes

There is only one normal sign-in flow now:

- `/login` on web handles both user and admin accounts
- mobile login also routes by account role

Registration is user-only:

- admin accounts cannot be created through sign-up
- admin accounts must be created manually in the database or with `python manage.py createsuperuser`
- no default admin credentials are committed to this repository
