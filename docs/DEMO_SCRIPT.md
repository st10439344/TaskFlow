# Demo video script (target: 9–10 minutes)

Rubric: *fully professional, all features shown in detail, voice-over, shows the data stored online*. Part 2 feedback: **voice-over, clear screen, warm up the server, show offline mode, streaks and language switching.**

## Before you press record (checklist)
- [ ] **Wake the server 5 minutes before:** open `https://YOUR-SERVICE.onrender.com/health` in a browser until it says `ok`.
- [ ] Record on the **real phone**, not the emulator. Use a fully charged phone, brightness high, portrait.
- [ ] Settings → Developer options → **Show taps** (so viewers see your touches). Clear old notifications. Do **not** use Do Not Disturb (you need to show notifications).
- [ ] Use the phone's built-in screen recorder with the microphone on, or record silently and add the voice-over in a video editor. Speak slowly; quiet room.
- [ ] On the laptop open, in tabs: MongoDB Atlas (Browse Collections), Render (Logs), Google Cloud (Credentials), Firebase console, GitHub Actions page, Android Studio Logcat filtered `tag:TaskFlow`.
- [ ] Start with a **new account** and an empty app. **Blur or skip full tokens/passwords** when showing Atlas (show `passwordHash` starting `$2a$12$` but not the `fcmTokens` values).
- [ ] POST_NOTIFICATIONS allowed on the phone (Android asks on first launch).

## Shot list and voice-over notes
| Time | Show | Say / prove |
|---|---|---|
| 0:00 | Splash + title card | Name, module, team, purpose: offline-first task manager in English, isiZulu and Afrikaans |
| 0:30 | **Register** — try bad inputs first (bad e-mail, weak password, mismatched confirm) then register | Input is validated on the phone and the API; the app never crashes |
| 1:15 | Atlas → `users` | The password is stored as a **bcrypt hash**, not plain text |
| 1:40 | Log out, **log in** (wrong password first) | Generic, translated error message; JWT kept in encrypted storage |
| 2:10 | **Continue with Google** | Single sign-on. Show the new user in Atlas (`authProvider: google`) and the OAuth client in Google Cloud (the authentication service) |
| 2:50 | **Tasks:** create a list item with priority, due date, subtasks, *Remind me*; tick subtasks; edit; delete | Features from Part 1: lists, subtasks, priorities, reminders |
| 3:50 | **User-defined 4 — repeating task:** create a *Daily* task due today, complete it | The next occurrence appears with 🔁 and tomorrow's date |
| 4:20 | **User-defined 5 — search and filters:** type in search, use Today / Overdue / Upcoming / Done, change sort | Explain the smart views |
| 4:50 | **Streak:** complete a task and point at the streak and weekly count in the toolbar | Calculated on the server (show the `users` document) |
| 5:20 | **Offline mode (Part 2 feedback):** Airplane mode **ON** → orange *Offline* banner → create, edit, delete a task → show "⟳ Waiting to sync" → Airplane mode **OFF** → banner changes to *Syncing* → status becomes "✓ Synced" | WorkManager's SyncWorker pushed the changes. Show them in Atlas (`tasks`, with `clientId`) and the `[sync]` line in Render logs |
| 6:50 | **Real-time notifications:** Settings → *Send test notification* → show it arrive. Then complete your last open task to trigger "All caught up" | FCM pipeline: app → API → Firebase → phone. Show `[push] test: 1 sent` in Render logs and the Cloud Messaging section of the Firebase console |
| 7:40 | **Settings:** change name, toggle dark theme, notifications switch | Saved instantly on the phone, then synced to the API |
| 8:00 | **Multi-language (Part 2 feedback):** switch to **isiZulu**, tour Home, form, settings; switch to **Afrikaans**; send the test notification again | Whole UI, error messages and notifications change language immediately |
| 8:40 | **Data stored online:** Atlas collections `users`, `tasklists`, `tasks`; Render logs | Authentication, API and database evidence |
| 9:10 | Android Studio **Logcat** (`tag:TaskFlow`) while syncing | Logging shows how the code works |
| 9:25 | GitHub: repo, README, **green Actions run**, tag `final-poe`; the signed APK and Play Console upload | Version control, automated tests, publishing evidence |
| 9:50 | Closing title card | Thank you |

## Voice-over tips
Write a sentence for each row, read it once before recording, and pause for a second after each screen change. Keep the phone steady and avoid scrolling too quickly. If you make a mistake, pause, then repeat the sentence — it is easy to cut later.
