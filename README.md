# TaskFlow

A simple, offline-capable personal task manager for Android (Kotlin), with a Node.js/Express REST API and MongoDB Atlas.
Module: Open-Source Coding (OPSC6312) — Part 2: App prototype.

## Repository layout
```
app/        Android app (Kotlin, XML layouts, Room, Retrofit, WorkManager)
backend/    REST API (Node.js, Express, MongoDB Atlas) — deployed on Render
.github/    GitHub Actions: Android unit tests + build, backend tests
```

## Features (Part 2 prototype)
| Rubric item | Implementation |
|---|---|
| Sign in | Register / log in with email + password (bcrypt hashed on the API, JWT session stored in EncryptedSharedPreferences) and **Google SSO** (Credential Manager → ID token verified by the API) |
| Settings | Name, language (English / isiZulu / Afrikaans, applied instantly), notifications, dark theme, change password, log out. Saved on the device immediately and synced to the API |
| REST API | `backend/` — auth, users, lists, tasks, sync. Hosted on Render, data in MongoDB Atlas |
| API integration | Retrofit + OkHttp JWT interceptor, central `safeCall` error handling, loading states, session-expiry handling |
| User feature 1 | Tasks, lists and subtasks (create / view / edit / complete / delete, priority, due date/time) |
| User feature 2 | **Offline mode and sync** — every change is written to Room first and marked *pending*; WorkManager pushes it to `/api/sync` when a connection returns |
| User feature 3 | **Streak and weekly stats** computed on the server, plus local **reminder notifications** for tasks with "Remind me" |

Deferred to the final PoE: FCM push notifications, forgot-password flow.

## Architecture
```
Activities (ViewBinding, XML)  →  Repositories  →  Room (source of truth)
                                        │                 ▲
                                        └── Retrofit ── REST API ── MongoDB Atlas
                        WorkManager: SyncWorker (push pending, pull server) · ReminderWorker
```
- Repository pattern: `UserRepository`, `TaskRepository`.
- Sync: each local task has a UUID (`clientId`), so retries never create duplicates. Last write wins using `updatedAt`.
- Pure logic (`Validators`, `DateUtils`, `SyncReconciler`, mappers) is unit-tested.

## Setup
1. **Backend** — see `backend/README.md`. Deploy to Render, set `MONGODB_URI`, `JWT_SECRET`, `GOOGLE_WEB_CLIENT_ID`.
2. **Android** — open this folder in Android Studio, then in `app/build.gradle.kts` set:
   - `API_BASE_URL` → your Render URL (with `https://` and a trailing `/`)
   - `GOOGLE_WEB_CLIENT_ID` → the *Web application* OAuth client ID
3. **Google Sign-In** — in Google Cloud Console create an *Android* OAuth client for package `com.taskflow.app` with each developer's debug SHA-1 (`./gradlew signingReport`), plus the *Web* client from step 2.
4. Run on a phone (Android 8.0+).

## Tests and CI
```bash
./gradlew :app:testDebugUnitTest      # Android unit tests
cd backend && npm test                # API tests
```
GitHub Actions (`.github/workflows/ci.yml`) runs both on every push and uploads the debug APK.

## Team
Sindisiwe Ntuli (lead, Android integration) · Thandiswa Gama (backend/API) · Boelo Mashego (offline data and sync) · Mandisa Motha (UI/UX, localisation, QA)
