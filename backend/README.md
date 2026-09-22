# TaskFlow Backend (REST API)

Node.js + Express + MongoDB Atlas. Hosted on Render. Consumed by the TaskFlow Android app via Retrofit.

## Run locally
```bash
npm install
cp .env.example .env     # fill in the values
npm run dev
npm test
```

## Environment variables
| Name | Purpose |
|---|---|
| `MONGODB_URI` | Atlas connection string |
| `JWT_SECRET` | Long random string used to sign tokens |
| `JWT_EXPIRES_IN` | Token lifetime, default `7d` |
| `GOOGLE_WEB_CLIENT_ID` | Web OAuth client ID, used to verify Google ID tokens |

## Endpoints
All `/api/*` routes except `/api/auth/*` need `Authorization: Bearer <token>`. Errors are `{ "message": "...", "errors": { field: "..." } }`.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | `{fullName,email,password}` → `{user,token}` (bcrypt hash, creates default lists) |
| POST | `/api/auth/login` | `{email,password}` → `{user,token}` |
| POST | `/api/auth/google` | `{idToken}` → `{user,token}` (creates or links account) |
| GET / PUT | `/api/users/me` | Profile and settings (`fullName, preferredLanguage, theme, notificationsEnabled`); GET includes `streakCount`, `weeklyCompletedCount` |
| PUT | `/api/users/me/password` | `{currentPassword,newPassword}` |
| GET / POST | `/api/lists` | List / create lists |
| PUT / DELETE | `/api/lists/:id` | Rename / delete (deletes its tasks) |
| GET / POST | `/api/tasks` | List (`?listId=&isComplete=`) / create task with `subtasks[]` |
| GET / PUT / DELETE | `/api/tasks/:id` | Read / edit or complete / delete |
| POST | `/api/sync` | `{tasks:[{clientId,listId,updatedAt,deleted?,...}]}` → per-item results |
| POST | `/api/devices/token` | `{fcmToken}` |
| GET | `/health` | Wake-up / health check |

Task JSON: `id, listId, clientId, title, description, dueDate, dueTime ("HH:mm"), priority (low|med|high), repeatRule (none|daily|weekly|monthly), isComplete, completedAt, subtasks[{id,title,isComplete}]`.

Task create/update/sync responses include `stats: { streakCount, weeklyCompletedCount }`.

### Sync rules
Keyed by `clientId` (the Room UUID), so retries never create duplicates. If the server copy has a newer `updatedAt`, the result is `status: "conflict"` and includes the server task. Statuses: `created | updated | deleted | conflict | error`.

## Deploy to Render
1. Push to GitHub. Render → New → Web Service → pick the repo.
2. Build command `npm install`, start command `npm start`.
3. Add the four environment variables above.
4. In Atlas → Network Access, allow `0.0.0.0/0` (Render free tier has no fixed IP).
