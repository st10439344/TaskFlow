# TaskFlow

**Open-Source Coding (OPSC6312) — Portfolio of Evidence: Parts 2 and 3**

**GitHub Repository:** https://github.com/st10439344/TaskFlow

## 1. Project Overview

TaskFlow is an offline-capable Android task management application developed using Kotlin, XML layouts, Room, Retrofit and WorkManager. It communicates with a Node.js/Express REST API and uses MongoDB Atlas for cloud data storage.

The application helps users organise daily responsibilities, manage tasks and subtasks, track productivity and continue working when the backend is temporarily unavailable.

TaskFlow was initially developed as a functional prototype for Part 2 of the Portfolio of Evidence. During Part 3, it was expanded with recurring tasks, search and filtering, Firebase Cloud Messaging, improved localisation, additional testing and preparation for release.

## 2. Project Objectives

The objectives of TaskFlow are to:

- Provide user registration and secure authentication.
- Allow users to create, edit, complete and delete tasks.
- Organise tasks into lists and subtasks.
- Support priorities, due dates and reminders.
- Store tasks locally for offline use.
- Synchronise pending changes with MongoDB Atlas.
- Support English, isiZulu and Afrikaans.
- Provide productivity tracking through streaks and statistics.
- Implement push notifications using Firebase Cloud Messaging.

## 3. Repository Structure

```text
TaskFlow/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/             Kotlin source code
│       │   └── res/              Layouts and translations
│       └── test/                 Android unit tests
├── backend/
│   ├── src/
│   │   ├── models/
│   │   ├── routes/
│   │   ├── services/
│   │   └── middleware/
│   └── tests/                    Backend tests
├── docs/
│   ├── screenshots/              Testing evidence
│   ├── play-store/               Store graphics
│   ├── AI_USE_WRITEUP.md
│   ├── DEMO_SCRIPT.md
│   ├── PRIVACY_POLICY.md
│   └── PUBLISHING.md
├── .github/workflows/           CI/CD workflows
├── RELEASE_NOTES.md
└── README.md
```

## 4. Features Implemented in Part 2

Part 2 focused on developing the main application, implementing its backend and establishing the offline-first architecture.

| Rubric item | Implementation |
|---|---|
| Authentication | Email registration and login, bcrypt password hashing and JWT authentication |
| Settings | Name, language, notifications, dark theme, password management and logout |
| REST API | Node.js/Express endpoints for authentication, users, lists, tasks and synchronisation |
| Database | MongoDB Atlas for cloud storage and Room for local storage |
| API integration | Retrofit, OkHttp, JWT interceptor and centralised error handling |
| Task management | Create, view, edit, complete and delete tasks, lists and subtasks |
| Offline operation | Changes saved locally in Room and marked as pending |
| Synchronisation | WorkManager submits pending changes when backend connectivity is restored |
| Productivity | Streak tracking, weekly statistics and local task reminders |
| Languages | English, isiZulu and Afrikaans |

These features provided the foundation for the final PoE.

## 5. Features Added and Extended in Part 3

### 5.1 Recurring Tasks

TaskFlow supports repeating tasks for activities that occur regularly.

The recurrence logic calculates future due dates from the original schedule, accounting for months with different numbers of days.

### 5.2 Search, Filtering and Sorting

Part 3 introduced improved task navigation through:

- Searching tasks.
- Filtering tasks by status and list.
- Sorting tasks.
- Viewing today's, overdue, upcoming and completed tasks.
- Managing priorities and due dates.

These features make it easier for users to organise and locate their tasks.

### 5.3 Offline Synchronisation

TaskFlow uses Room as its local database and WorkManager for background synchronisation.

When the backend is unavailable, users can continue creating tasks. Pending changes are stored locally until synchronisation is possible.

During Part 3 testing, the Node.js backend was temporarily stopped while BlueStacks remained connected to the internet. Two tasks were created and marked **Waiting to sync**.

After the backend restarted, WorkManager processed the pending changes, and the tasks were verified in MongoDB Atlas.

**Result: Passed — pending tasks were successfully synchronised after backend restoration.**

### 5.4 Firebase Cloud Messaging

Firebase Cloud Messaging (FCM) was integrated into TaskFlow to provide push notification functionality.

The implementation includes:

- Firebase configuration in the Android application.
- Firebase Admin SDK integration in the Node.js backend.
- Android device-token registration.
- A **Send test notification** option in Settings.
- Notification handling in the Android application.
- Backend support for sending task-related notification messages.

#### Firebase Notification Testing

During testing, Firebase Admin initialised successfully and Android device tokens were registered with the backend.

Initial testing encountered Firebase token errors, including:

```text
messaging/registration-token-not-registered
```

The configuration and device-registration process were investigated and corrected during development.

A subsequent test was reported successful, with the TaskFlow notification received on an Android device.

**Final reported result: Passed — Firebase push notification functionality was successfully demonstrated.**

Earlier token-registration errors are retained in the testing history to document the troubleshooting process.

### 5.5 Google Sign-In

Google Sign-In configuration was extended during Part 3.

The following steps were completed:

- Google authentication enabled in Firebase.
- Android debug SHA-1 fingerprint registered.
- Google Web OAuth Client ID obtained.
- Android and backend configuration updated.

However, a successful Google Sign-In session was not achieved during final testing.

Email-and-password registration remained functional and was verified on the physical Xiaomi device.

### 5.6 Languages and User Interface

TaskFlow supports:

- English.
- isiZulu.
- Afrikaans.
- Dark theme.
- Notification preferences.
- Profile settings.

Part 3 included additional interface improvements, translations and Android compatibility changes.

## 6. System Architecture

TaskFlow follows a repository-based architecture.

```text
                   ANDROID APPLICATION
                     Kotlin + XML UI
                            |
                            v
                       Repositories
                            |
                  +---------+---------+
                  |                   |
                  v                   v
            Room Database      Retrofit / OkHttp
              Local data              |
                  |                   v
                  |             Node.js / Express
                  |                 REST API
                  |                   |
                  v                   v
             WorkManager          MongoDB Atlas
           Background sync        Cloud database

                 Firebase Cloud Messaging
                            |
                            v
                    Firebase Admin SDK
```

### Main Components

**Android application:** Provides the interface for authentication, task management, settings and notifications.

**Room database:** Stores task data locally and supports offline operation.

**Repositories:** Coordinate operations between the local database and backend API.

**WorkManager:** Handles background synchronisation and scheduled operations.

**Node.js/Express backend:** Provides authentication, task management, synchronisation and device registration endpoints.

**MongoDB Atlas:** Stores user accounts, task lists and tasks.

**Firebase Cloud Messaging:** Enables push messages to be sent from the backend to Android devices.

### Synchronisation Strategy

Tasks use client-generated identifiers to support retries and reduce duplicate records.

The application tracks pending local changes and synchronises them with the backend when connectivity is available.

## 7. Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| XML and ViewBinding | User interface |
| Room | Local database |
| WorkManager | Background synchronisation |
| Retrofit and OkHttp | API communication |
| Node.js | Backend runtime |
| Express.js | REST API framework |
| MongoDB Atlas | Cloud data storage |
| JWT | Authenticated sessions |
| bcrypt | Password hashing |
| Firebase Cloud Messaging | Push notifications |
| Firebase Admin SDK | Server-side notification requests |
| JUnit | Android unit tests |
| Jest and Supertest | Backend testing |
| Git and GitHub | Version control |
| GitHub Actions | Automated builds and tests |
| Android Studio | Development and debugging |

## 8. Installation and Setup

### 8.1 Requirements

- Android Studio and a compatible Android SDK.
- Java version compatible with the project's Gradle configuration.
- Node.js and npm.
- MongoDB Atlas database.
- Firebase project.
- Android device or compatible emulator.

### 8.2 Clone the Repository

```bash
git clone https://github.com/st10439344/TaskFlow.git
cd TaskFlow
```

Open the project in Android Studio and allow Gradle to synchronise.

### 8.3 Backend Setup

Navigate to the backend directory:

```bash
cd backend
npm install
```

Create a `.env` file using `.env.example` as a template.

Required configuration includes:

```env
PORT=3000
MONGODB_URI=YOUR_MONGODB_CONNECTION_STRING
JWT_SECRET=YOUR_SECURE_JWT_SECRET
JWT_EXPIRES_IN=7d
GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID
FIREBASE_SERVICE_ACCOUNT=YOUR_BASE64_FIREBASE_SERVICE_ACCOUNT
```

Start the server:

```bash
npm start
```

Verify the backend using:

```text
GET /health
```

A successful response contains:

```json
{
  "status": "ok"
}
```

**Security:** Database credentials, JWT secrets, Firebase private keys and real environment files must never be uploaded to GitHub.

### 8.4 Android Setup

1. Open the project in Android Studio.
2. Configure `API_BASE_URL` in `app/build.gradle.kts`.
3. Configure the Google Web OAuth Client ID.
4. Register the package `com.taskflow.app` in Firebase.
5. Add the correct `google-services.json` inside `app/`.
6. Synchronise Gradle.
7. Build and run the application.

The main Part 3 project targets modern Android versions. A separate Android 7.1 compatibility configuration was used for testing in BlueStacks.

## 9. Testing and Quality Assurance

Testing was performed using Android Studio, BlueStacks 5, backend logs, MongoDB Atlas and a physical Xiaomi smartphone running HyperOS.

### 9.1 Testing Summary

| Test case | Result |
|---|---|
| Android debug build | Passed |
| Launch on BlueStacks | Passed |
| Installation on physical Xiaomi | Passed |
| Email registration on Xiaomi | Passed |
| Backend health endpoint | Passed |
| MongoDB Atlas connection | Passed |
| Task creation and editing | Passed in development testing |
| Searching, filtering and sorting | Tested successfully |
| Pending offline changes | Passed |
| Synchronisation after backend restoration | Passed |
| Three-language interface | Implemented and exercised |
| Firebase Admin initialisation | Passed |
| Firebase device registration | Passed |
| Firebase notification test request | Passed |
| Firebase push notification received | Passed — reported successful |
| Google Sign-In | Unresolved |
| Google Play public publication | Not completed |

### 9.2 Physical Device Testing

TaskFlow was installed on a physical Xiaomi smartphone running HyperOS.

The application launched successfully, and account registration worked using the backend.

Because Android Studio and the Node.js backend were running inside a CloudLabs virtual machine, a temporary Cloudflare Tunnel provided HTTPS access to the backend from the phone.

This demonstrated the Android application communicating with the backend from real mobile hardware.

### 9.3 Offline Synchronisation Testing

The test procedure was:

1. Open TaskFlow in BlueStacks.
2. Stop the Node.js backend.
3. Create two tasks while the backend is unavailable.
4. Observe the **Waiting to sync** indicator.
5. Restart the backend.
6. Allow WorkManager to process pending changes.
7. Verify the synchronised tasks in MongoDB Atlas.

**Result: Passed.**

This demonstrated recovery from backend unavailability rather than a full airplane-mode test.

### 9.4 Firebase Notification Testing

The Firebase notification testing process included:

1. Configuring Firebase in the Android application.
2. Adding Firebase Admin credentials to the backend.
3. Registering Android device tokens.
4. Sending notifications through the backend.
5. Investigating initial Firebase token-registration failures.
6. Retesting the notification feature.

Following troubleshooting, the notification feature was reported working.

**Result: Passed — successful notification reception was reported during final testing.**

## 10. Part 3 Screenshot Evidence

The following screenshots were captured during development and testing.

### Figure 1: Firebase Test Notification

![TaskFlow Firebase notification test](docs/screenshots/TaskFlow_Firebase_Test_Notification.png)

**Description:** This screenshot shows the TaskFlow Settings screen in BlueStacks after the **Send test notification** option was selected.

The application displayed:

*“Test notification sent. It should arrive in a few seconds.”*

This screenshot provides visual evidence of the test request and application response.

Successful notification reception was reported separately during subsequent testing. This particular screenshot shows the confirmation message, not the Android notification panel.

### Figure 2: MongoDB Atlas Database

![TaskFlow MongoDB Atlas database](docs/screenshots/taskflow%20database.png)

**Description:** This screenshot shows the TaskFlow database in MongoDB Atlas.

The database contains:

- `tasklists`
- `tasks`
- `users`

These collections store information used by the TaskFlow backend.

Task records synchronised from Android were also verified in MongoDB Atlas during testing.

### Figure 3: Offline Task Synchronisation

![TaskFlow offline synchronisation](docs/screenshots/waitingforsync%281%29.png)

**Description:** This screenshot shows the TaskFlow dashboard in BlueStacks during synchronisation testing.

A task named **OFFLINE SYNC TEST** is marked:

**Waiting to sync**

The application also displays a **Syncing changes...** banner.

This demonstrates how TaskFlow identifies local task changes awaiting synchronisation.

After the backend was restarted, the pending tasks were successfully synchronised.

### Additional Evidence

A video recording of the Firebase test notification request was captured during development.

The TaskFlow application was also installed and tested on a physical Xiaomi device, including successful email-based account registration.

## 11. Automated Tests and CI/CD

TaskFlow includes Android and backend tests.

### Android Tests

```bash
./gradlew :app:testDebugUnitTest
```

Test areas include:

- Input validation.
- Date calculations.
- Data mapping.
- Synchronisation reconciliation.
- Recurring-task calculations.
- Task filtering.
- Notification message logic.

### Backend Tests

```bash
cd backend
npm test
```

Backend tests cover selected API operations and business logic.

GitHub Actions workflows are located under:

```text
.github/workflows/
```

These workflows support automated building and testing. Their execution results can be checked in GitHub's Actions tab.

## 12. Google Play Store Preparation

Part 3 included preparation for Google Play distribution.

The repository contains:

- Play Store application icon.
- Feature graphic.
- Release notes.
- Privacy policy draft.
- Publishing instructions.
- Release workflow configuration.

Additional information is available in:

`docs/PUBLISHING.md`

**Publication status:** TaskFlow was not publicly published on Google Play during this assessment.

Public publication still requires a developer account, release signing, a signed Android App Bundle, store compliance checks, applicable testing requirements and stable backend hosting.

The temporary Cloudflare Tunnel was used only for development and testing.

## 13. Known Limitations

### 13.1 Google Sign-In

Google authentication was configured in Firebase and the Android application, but a successful Google Sign-In session was not verified.

Email-and-password authentication remained functional.

### 13.2 Production Backend Hosting

The backend ran inside CloudLabs, and physical-device testing used a temporary HTTPS Cloudflare Tunnel.

A stable production backend is required for long-term public deployment.

### 13.3 Google Play Publication

The application has not yet been publicly released on Google Play.

### 13.4 Additional Testing

Further testing is recommended for:

- Google Sign-In.
- Full airplane-mode functionality.
- Long-term notification reliability across multiple devices.
- Release-build testing.
- Production deployment.

## 14. Security and Privacy

TaskFlow uses authenticated API requests and password hashing.

The repository includes example environment configuration but excludes operational credentials.

The following information must remain private:

- MongoDB passwords.
- JWT secrets.
- Firebase service-account private keys.
- Production signing keys.
- User passwords.

The privacy policy draft is located at:

`docs/PRIVACY_POLICY.md`

## 15. Use of Artificial Intelligence

AI tools were used as supporting resources during selected stages of development.

Claude and ChatGPT assisted with technical explanations, selected implementation tasks, troubleshooting and documentation.

The group remained responsible for evaluating solutions, reviewing code, configuring services, conducting practical tests and preparing the final submission.

The full AI-use reflection and Harvard-style references are provided in:

`docs/AI_USE_WRITEUP.md`

## 16. Future Improvements

Planned improvements include:

1. Completing and verifying Google Sign-In.
2. Moving the backend to permanent HTTPS hosting.
3. Expanding notification reliability testing across more devices.
4. Conducting full airplane-mode tests.
5. Completing release signing and Google Play publication.
6. Expanding automated test coverage.

## 17. Conclusion

TaskFlow demonstrates the development of a native Android application with offline-first task management, REST API integration, MongoDB Atlas and Firebase Cloud Messaging.

Part 2 established the application's core authentication, task management and synchronisation functionality.

Part 3 extended the application through recurring tasks, advanced searching and filtering, improved localisation, push notification integration, additional testing and release preparation.

Practical testing demonstrated successful installation on a physical Xiaomi device, email-based registration, task synchronisation following backend restoration and reported successful Firebase notification delivery.

Google Sign-In and public Google Play publication remain outstanding.

---

**Module:** Open-Source Coding (OPSC6312)  
**Assessment:** Portfolio of Evidence — Parts 2 and 3  
**Repository:** https://github.com/st10439344/TaskFlow  
**Submission date:** 5 November 2026
