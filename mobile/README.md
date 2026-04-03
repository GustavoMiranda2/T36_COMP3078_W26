# Hair Stylist Booking Web Application  

**Course:** Capstone Project — UI Prototype  
**Group Number:** 33  

## Project Overview
**Project Name:** Hair Stylist Booking Web Application  
**Prototype Title:** Project UI Prototype  

This project is a **UI prototype** for a web and mobile application designed to simplify hairstylist appointment bookings. The prototype demonstrates the core user interface, flow, and visual identity of the system before full backend development and integration.  

The goal is to provide a **user-friendly**, **visually appealing**, and **functional mockup** that allows customers to browse available services, schedule appointments, and explore hairstylist portfolios.  

---

## Purpose
This prototype aims to address the inefficiencies of manual appointment scheduling (via phone calls or messages) by offering a digital solution that enhances both **customer convenience** and **stylist workflow management**.

---

##  Key Features (Prototype Scope)
- **Online Booking Interface:** Displays available services and time slots.  
- **Admin Dashboard Mockup:** For hairstylists to manage schedules, clients, and services.  
- **Customer Profile Pages:** Includes booking history and saved preferences.  
- **Portfolio Section:** Showcases hairstylist work (before/after images).  
- **Branding and UI Design:** Consistent with the hairstylist’s identity and aesthetic.  
- **Optional Blog Section:** Placeholder for future stylist updates or tips.  





---

##  Team Members

| Name | Student ID | GitHub |
|------|-------------|--------|
| **Gustavo Miranda** | 101488574 | [GitHub](https://github.com/GustavoMiranda2) |
| **Renan Makoto** | 101536279 | [GitHub](https://github.com/renanmakoto) |
| **Lucas Tavares Criscuolo** | 101500671 | [GitHub](https://github.com/Stuaarts) |
| **Mateus de Souza Carvalho Melco Sfeir** | 101484904 | [GitHub](https://github.com/mateussfeir) |





---

##  Note
This repository contains only the **UI prototype** for the Hair Stylist Booking Web Application. The **backend logic and database integration** will be developed and integrated in the next project phase.

---

**© 2025 Group 33 — BrazWebDes Hair Stylist Booking Web Application**


## Mobile API Connection (Android)
- Default API URL in the app: `http://10.0.2.2:8000` (Android emulator -> host machine).
- For physical devices, build with your machine IP:

```bash
./gradlew :app:assembleDebug -PapiBaseUrl=http://192.168.1.50:8000
```

- Backend must include that host in `ALLOWED_HOSTS`.

## Release / Play Console Build

Release builds now require explicit production URLs:

```bash
./gradlew :app:bundleRelease \
  -PapiBaseUrl=https://api.example.com \
  -PwebBaseUrl=https://app.example.com
```

Optional release signing properties:

```bash
./gradlew :app:bundleRelease \
  -PapiBaseUrl=https://api.example.com \
  -PwebBaseUrl=https://app.example.com \
  -PreleaseStoreFile=/absolute/path/upload-keystore.jks \
  -PreleaseStorePassword=... \
  -PreleaseKeyAlias=... \
  -PreleaseKeyPassword=...
```

Notes:

- Debug builds still default to emulator loopback URLs.
- Release builds disable cleartext traffic and must use HTTPS endpoints.
- The app exposes privacy policy and account deletion pages from the deployed web frontend.
