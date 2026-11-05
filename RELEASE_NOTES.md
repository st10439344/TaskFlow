# Release notes

## Version 2.0.0 — Portfolio of Evidence (final)
*Changes since the Part 2 prototype (version 1.0.0).*

### Innovative features added
| Feature | What it does |
|---|---|
| **Real-time push notifications (Firebase Cloud Messaging)** | The API pushes a 🔥 streak alert at 3, 7, 14, 30, 60 and 100 days, a 🎉 "all caught up" alert when the last open task is completed, and a silent message that makes a user's other phones sync immediately. Notification text is shown in the user's chosen language. Settings has a *Send test notification* button. |
| **Repeating tasks** | Tasks can repeat daily, weekly or monthly. Completing one creates the next occurrence, even offline, and it syncs like any other task. Monthly tasks handle short months and leap years. |
| **Search, smart filters and sorting** | Search by title or description, Today / Overdue / Upcoming / Done views and four sort orders (due date, priority, recently updated, title). |
| **Fully localised experience** | Error messages and notifications are now translated into isiZulu and Afrikaans too, not only the screens. |

### Play Store readiness
* Now targets **Android 16 (API 36)** — required by Google Play for new apps and updates since 31 August 2026 (Google, 2026).
* Release signing configuration (keystore kept out of GitHub), version 2.0.0 (code 2), adaptive icon with a themed monochrome layer, Play Store icon and feature graphic, privacy policy, store listing text.
* **In-app account deletion** (Settings → Delete account) and `DELETE /api/users/me`, as required by Google Play for apps with accounts.
* Edge-to-edge layout so screens look correct on Android 15 and 16.

### Improvements and fixes
* Detailed logging in every layer (`tag:TaskFlow` in Logcat; request, sync and push logs on the API). The JWT is redacted from HTTP logs.
* Several devices per account (`fcmTokens` array replaces the single `fcmToken` in the data dictionary); a phone belongs to one account at a time and is unregistered on log out.
* Notification channels are renamed when the language changes; reminders also use the chosen language.
* Build tooling upgraded: Android Gradle Plugin 8.9.1, Gradle 8.11.1, compileSdk/targetSdk 36. CI now uses the Gradle wrapper.
* Tests: 40 backend tests and Android unit tests for recurrence, filters and push parsing, in addition to the Part 2 tests.
* Demo feedback from Part 2 addressed: clear screen recording with voice-over, server warm-up, full offline demonstration, streak and language switching shown.

### Known limitations
* Creating, renaming and deleting lists needs an internet connection (tasks work fully offline).
* The free Render tier sleeps after inactivity, so the first request can take about a minute.
* Forgot-password by e-mail is not included.

## Version 1.0.0 — Part 2 prototype
Registration and login (bcrypt, JWT), Google SSO, settings, REST API on Render with MongoDB Atlas, tasks / lists / subtasks, offline mode with WorkManager sync, streaks, local reminders, English / isiZulu / Afrikaans interface.

Google (2026) *Target API level requirements for Google Play apps*. Available at: https://support.google.com/googleplay/android-developer/answer/11926878
