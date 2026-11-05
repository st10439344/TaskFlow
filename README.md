# TaskFlow

**Open-Source Coding (OPSC6312) — Portfolio of Evidence: Parts 2 and 3**

TaskFlow is an offline-capable personal task management application developed for Android using **Kotlin**. It integrates with a **Node.js/Express REST API** and **MongoDB Atlas** to allow users to create, organise and manage tasks, track their progress and synchronise their work.

The application was initially developed as a prototype for Part 2 and extended during Part 3 with additional task management features, Firebase Cloud Messaging integration, improved localisation, testing and release preparation.

**GitHub repository:** https://github.com/st10439344/TaskFlow

## 1. Project Objectives

TaskFlow was developed to help users manage their daily responsibilities through a simple and accessible mobile interface.

The main objectives are to:

- Allow users to register and log in securely.
- Create, update, organise, complete and delete tasks.
- Support task lists, subtasks, priorities and reminders.
- Allow users to work when the backend is unavailable.
- Synchronise pending changes with the cloud database.
- Support English, isiZulu and Afrikaans.
- Provide productivity tracking through streaks and weekly statistics.
- Incorporate additional functionality for the final PoE.

## 2. Repository Layout

```text
TaskFlow/
├── app/                     Android application
│   └── src/main/
│       ├── java/            Kotlin source code
│       └── res/             XML layouts and resources
├── backend/                 Node.js/Express REST API
│   ├── src/                 Routes, models and services
│   └── tests/               Backend unit and API tests
├── docs/                    Project documentation
│   ├── screenshots/         Application and testing evidence
│   ├── play-store/          Store listing graphics
│   ├── AI_USE_WRITEUP.md
│   ├── DEMO_SCRIPT.md
│   ├── PRIVACY_POLICY.md
│   └── PUBLISHING.md
├── .github/workflows/       GitHub Actions
├── RELEASE_NOTES.md
└── README.md
```

## 3. Features Implemented in Part 2

Part 2 focused on developing the core TaskFlow prototype, implementing the Android interface and connecting the application to a functional backend.

| Rubric item | Implementation |
|---|---|
| Authentication | User registration and email/password login, bcrypt password hashing, JWT authentication and locally stored sessions |
| Settings | User name, language preferences, notifications, dark theme, password management and logout |
| REST API | Node.js/Express endpoints for authentication, users, lists, tasks and synchronisation |
| Database | MongoDB Atlas for cloud storage and Room for local Android storage |
| API integration | Retrofit, OkHttp, JWT interceptor and centralised error handling |
| Task management | Create, view, edit, complete and delete tasks, lists and subtasks |
| Offline capability | Changes are written to Room and marked as pending until synchronisation is possible |
| Productivity features | Streak tracking, weekly statistics and local task reminders |
| Localisation | English, isiZulu and Afrikaans |

The Part 2 design provided the foundation for the additional features and testing completed during Part 3.

## 4. Features Added or Extended in Part 3

### 4.1 Advanced Task Management

Part 3 expanded TaskFlow's task management capabilities.

The application supports:

- Recurring tasks.
- Task searching.
- Filtering and sorting tasks.
- Task priorities and due dates.
- List and subtask management.
- Improved task display and user interaction.

Recurring-task logic was implemented to calculate future dates while accounting for differences in the number of days in each month.

### 4.2 Firebase Cloud Messaging

Firebase Cloud Messaging (FCM) was integrated to support push notifications.

The implementation includes:

- Firebase configuration in the Android application.
- Firebase Admin SDK integration in the backend.
- Android device-token registration.
- A test notification feature in Settings.
- Notification handling for selected task-related events.

**Testing outcome:** Firebase Admin initialised successfully and the Android application registered device tokens. However, some test messages were rejected with `messaging/registration-token-not-registered`. Actual notification delivery to the Android notification panel was not consistently confirmed.

### 4.3 Improved Offline Synchronisation

TaskFlow uses **Room** and **WorkManager** to support offline-first task management.

When the backend is unavailable, users can continue creating tasks. These changes are saved locally and marked as pending.

Once the backend becomes available again, the application attempts to synchronise the pending changes with MongoDB Atlas.

**Part 3 verification:** The backend was stopped while BlueStacks remained connected to the internet. Two tasks were created and displayed as waiting to synchronise. After restarting the backend, the pending changes were processed, and both tasks were confirmed in MongoDB Atlas.

This verified synchronisation following a backend outage. A complete airplane-mode test was not performed during this test.

### 4.4 Multilingual Support

TaskFlow supports three languages:

- English
- isiZulu
- Afrikaans

Part 3 included further localisation improvements and translated interface messages.

### 4.5 Additional Application Improvements

Other Part 3 improvements included:

- Notification settings and test functionality.
- Updated Android compatibility handling.
- Improved error messages.
- UI adjustments for newer Android versions.
- Additional Kotlin unit tests.
- Backend notification tests.
- GitHub Actions workflow updates.
- Release notes and Play Store preparation resources.

## 5. System Architecture

TaskFlow follows a repository-based architecture.

```text
                   ANDROID APPLICATION
                 Kotlin + XML + ViewBinding
                            |
                            v
                       Repositories
                            |
              +-------------+-------------+
              |                           |
              v                           v
        Room Database              Retrofit / OkHttp
       Local task storage                 |
              |                           v
              |                    Node.js / Express
              |                        REST API
              |                           |
              v                           v
         WorkManager                 MongoDB Atlas
       Background sync               Cloud database

            Firebase Cloud Messaging
                      |
                      v
             Firebase Admin SDK
                Node.js backend
```

### Architectural Components

**Android application:** Responsible for user interaction, task management and local storage.

**Room database:** Stores local task data and supports offline access.

**Repositories:** Coordinate data access and synchronisation between the Android application, local database and backend.

**WorkManager:** Processes background synchronisation and scheduled work.

**REST API:** Handles authentication, user information, tasks, lists, synchronisation and device registration.

**MongoDB Atlas:** Stores users, lists and tasks.

**Firebase Cloud Messaging:** Provides infrastructure for server-initiated push notifications.

### Data Synchronisation

Local tasks use client-generated identifiers to support retries without creating duplicate records. The synchronisation process reconciles local and remote changes using the application's conflict-resolution rules.

## 6. Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML and ViewBinding | Android user interface |
| Room | Local database |
| WorkManager | Background operations and synchronisation |
| Retrofit and OkHttp | HTTP communication |
| Node.js | Backend runtime |
| Express.js | REST API |
| MongoDB Atlas | Cloud database |
| JWT | Authenticated sessions |
| bcrypt | Password hashing |
| Firebase Cloud Messaging | Push notification integration |
| Firebase Admin SDK | Backend notification requests |
| JUnit | Android unit testing |
| Jest and Supertest | Backend testing |
| Git and GitHub | Version control |
| GitHub Actions | Automated build and test workflows |
| Android Studio | Development and debugging |

## 7. Installation and Setup

### 7.1 Requirements

- Android Studio with a compatible Android SDK.
- Java version compatible with the project's Gradle configuration.
- Node.js and npm.
- MongoDB Atlas database.
- Firebase project for push notification configuration.
- Android device or emulator.

The original Part 3 configuration targets modern Android versions. A separate Android 7.1-compatible test configuration was also used for BlueStacks.

### 7.2 Clone the Repository

```bash
git clone https://github.com/st10439344/TaskFlow.git
cd TaskFlow
```

Open the project in Android Studio and allow Gradle to synchronise.

### 7.3 Backend Setup

Navigate to the backend directory:

```bash
cd backend
npm install
```

Copy `.env.example` to `.env` and configure the required environment variables.

```env
PORT=3000
MONGODB_URI=YOUR_MONGODB_CONNECTION_STRING
JWT_SECRET=YOUR_SECURE_JWT_SECRET
JWT_EXPIRES_IN=7d
GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID
FIREBASE_SERVICE_ACCOUNT=YOUR_BASE64_ENCODED_SERVICE_ACCOUNT
```

Start the server:

```bash
npm start
```

The API exposes a health-check endpoint:

```text
GET /health
```

A healthy backend returns a response containing:

```json
{
  "status": "ok"
}
```

**Security:** The real `.env` file, database passwords, JWT secrets and Firebase Admin private credentials must never be committed to GitHub.

### 7.4 Android Configuration

Open `app/build.gradle.kts` and configure the relevant build settings:

- `API_BASE_URL`: backend HTTPS address, including the trailing `/`.
- `GOOGLE_WEB_CLIENT_ID`: Google OAuth Web application client ID.

Register the Android application in Firebase using:

```text
com.taskflow.app
```

Download the matching `google-services.json` and place it in the `app/` directory.

For Google authentication, configure the relevant Android signing certificate fingerprint in Firebase and Google Cloud.

The project can then be built and launched using Android Studio.

## 8. Testing and Quality Assurance

Testing was performed during development using Android Studio, BlueStacks, backend logs, MongoDB Atlas and a physical Android phone.

### 8.1 Part 2 Testing

Part 2 included testing of:

- User registration and login.
- Task creation and editing.
- Lists and subtasks.
- Data validation.
- API integration.
- Local database operations.
- Basic offline functionality.
- Streak and reminder calculations.

### 8.2 Part 3 Testing Results

| Test case | Result | Evidence |
|---|---|---|
| Android APK build | Passed | Android Studio build output |
| Launch in BlueStacks | Passed | Emulator demonstration |
| Installation on physical Xiaomi | Passed | Physical-device installation |
| Email account registration on Xiaomi | Passed | Registration and dashboard |
| Backend connection | Passed | API health response |
| MongoDB Atlas connection | Passed | Backend terminal |
| Task creation and editing | Passed in development testing | Application demonstration |
| Search, filtering and sorting | Tested in development | Application demonstration |
| Offline task queue | Passed | Pending task indicators |
| Synchronisation after backend restart | Passed | Sync logs and MongoDB Atlas |
| Language selection | Implemented and exercised | Application settings |
| Firebase Admin initialisation | Passed | Backend logs |
| FCM device-token registration | Passed | Backend logs |
| End-to-end FCM push delivery | Not confirmed | Firebase token rejection |
| Google Sign-In | Unresolved | Google authentication troubleshooting |
| Google Play publication | Not completed | Release preparation only |

### 8.3 Physical Device Testing

TaskFlow was installed successfully on a **Xiaomi smartphone running HyperOS**.

The physical phone was used to verify that the application could launch and successfully register a user account.

Because Android Studio and the Node.js backend were running in a CloudLabs virtual machine, a temporary **Cloudflare Tunnel** was used to provide an HTTPS connection between the physical device and the backend.

This enabled physical-device API testing without requiring a permanent hosted backend.

### 8.4 Testing Limitations

Although Firebase configuration and backend device registration were completed, reliable FCM message delivery was not demonstrated during testing.

Google Sign-In was also configured using Firebase Authentication, an Android debug SHA-1 fingerprint and a Google Web OAuth client ID, but a successful sign-in was not achieved.

These issues are recorded as outstanding limitations rather than completed test cases.

## 9. Automated Tests and CI/CD

The repository contains automated tests for Android and backend logic.

### Android Tests

```bash
./gradlew :app:testDebugUnitTest
```

Examples include:

- Input validation.
- Date calculations.
- Data mapping.
- Synchronisation reconciliation.
- Recurring-task calculations.
- Task filtering.
- Notification-related message logic.

### Backend Tests

```bash
cd backend
npm test
```

Backend tests cover selected API operations and business logic.

GitHub Actions workflows are included under `.github/workflows/` to support automated build and test execution.

The presence of these workflows does not imply that every workflow passed for the final commit; results should be checked in the repository's Actions tab.

## 10. Screenshots and Demonstration

The following screenshots and recordings provide evidence of development and testing:

- Login screen.
- Registration screen.
- Main task dashboard.
- Task creation and editing.
- Search, filtering and sorting.
- Settings and language selection.
- Tasks waiting to synchronise.
- Synchronised records in MongoDB Atlas.
- Firebase notification test request.
- TaskFlow installed and running on the Xiaomi phone.

Screenshots can be found in `docs/screenshots/` once uploaded.

The project demonstration guide is provided in:

`docs/DEMO_SCRIPT.md`

## 11. Google Play Store Preparation

Part 3 included preparation for distributing TaskFlow through Google Play.

The repository contains:

- Play Store application icon.
- Feature graphic.
- Release notes.
- Privacy policy draft.
- Publishing instructions.
- Release build workflow.

See:

`docs/PUBLISHING.md`

**Publication status:** TaskFlow was not publicly published on Google Play during this assessment. Public publication requires a Google Play developer account, a signed Android App Bundle, compliance information, applicable testing and review requirements, and a stable production backend.

## 12. Known Limitations and Future Improvements

The following improvements are planned:

1. **Google Sign-In:** Complete the OAuth flow and verify successful authentication on a physical Android device.
2. **FCM notifications:** Resolve Firebase's `registration-token-not-registered` response and confirm actual notification delivery.
3. **Permanent backend hosting:** Replace the temporary development tunnel with a stable production HTTPS backend.
4. **Expanded testing:** Conduct additional physical-device testing and full network-offline tests.
5. **Google Play release:** Complete release signing, Play Console setup, testing requirements and publication.
6. **Password recovery:** Complete and verify a forgot-password flow if required by the final assessment.

## 13. Security and Privacy

TaskFlow uses authenticated API requests and password hashing.

The repository includes an example backend environment file but excludes operational credentials.

Sensitive information such as MongoDB passwords, JWT secrets, Firebase service-account private keys and production keystores should not be uploaded to GitHub.

A privacy policy draft is available in:

`docs/PRIVACY_POLICY.md`

## 14. Use of Artificial Intelligence

AI tools were used as supporting resources during the project for selected implementation tasks, debugging, technical explanations and documentation.

The group's design decisions, configuration work, practical testing, evaluation and final submission remained human responsibilities.

A detailed reflection and Harvard-style references are provided in:

`docs/AI_USE_WRITEUP.md`

## 15. Conclusion

TaskFlow demonstrates the development of a native Android task management application integrated with a REST API and cloud database.

Part 2 established the application's core functionality, including authentication, task management, local storage and synchronisation.

Part 3 expanded the application with recurring tasks, search and filtering, improved localisation, Firebase integration, testing and release preparation.

Practical testing confirmed that TaskFlow could be installed and launched on a physical Xiaomi device, register users through the backend and synchronise locally created tasks after backend availability was restored.

Outstanding Google Sign-In, push notification delivery and Google Play publication requirements have been documented for future improvement.

---

**Module:** Open-Source Coding (OPSC6312)  
**Assessment:** Portfolio of Evidence — Parts 2 and 3  
**Repository:** https://github.com/st10439344/TaskFlow  
**Submission date:** 5 November 2026
cd backend && npm test                # API tests
```
GitHub Actions (`.github/workflows/ci.yml`) runs both on every push and uploads the debug APK.

## Team
Sindisiwe Ntuli (lead, Android integration) · Thandiswa Gama (backend/API) · Boelo Mashego (offline data and sync) · Mandisa Motha (UI/UX, localisation, QA)
