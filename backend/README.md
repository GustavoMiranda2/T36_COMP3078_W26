# Capstone Backend

Backend API for the booking application, built with Python and Django REST Framework.

This backend provides authentication, service listing, availability calculation, and appointment booking APIs, designed to be consumed by both web and mobile clients.

---

## Tech Stack
- Python
- Django
- Django REST Framework (DRF)
- PostgreSQL
- JWT Authentication (SimpleJWT)

---

## Environment Setup

Create a `.env` file in the `backend/` directory (see `.env.example`).

Required variables:

- `DEBUG` (true / false)
- `SECRET_KEY`
- `DATABASE_URL` (PostgreSQL connection string)
- `ALLOWED_HOSTS`
- `JWT_SECRET_KEY` (or use Django `SECRET_KEY`)

Example:
```env
DEBUG=true
SECRET_KEY=django-secret-key
DATABASE_URL=postgres://user:password@localhost:5432/capstone_db
ALLOWED_HOSTS=localhost,127.0.0.1
```

---

## Authentication Flow
- `POST /auth/register` creates a user with email + password.
- `POST /auth/login` returns `access` and `refresh` JWTs and user data.
- All appointment endpoints require `Authorization: Bearer <access-token>`.

---

## Appointment Lifecycle
- `POST /appointments` creates a confirmed appointment.
- `GET /appointments?me=true` returns the authenticated user's appointments.
- `PATCH /appointments/<id>` with `{ "action": "cancel" }` cancels the appointment.
- `PATCH /appointments/<id>` with `{ "action": "reschedule", "date": "YYYY-MM-DD", "start_time": "HH:MM" }`
  reschedules the appointment if the slot is available.

---

## Business Hours
- Slots are in 15-minute increments.
- Business hours are 10:00 to 19:00.
- The last slot starts at 18:45.
