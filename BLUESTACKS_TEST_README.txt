TASKFLOW - ANDROID 7.1 BLUESTACKS TEST COPY

Only use this folder for temporary testing with BlueStacks Nougat (API 25).
Do NOT submit this as your final release without reviewing and testing it.
The unchanged original project has minSdk 26, and should remain your final baseline.

Changes relative to the uploaded ZIP:
1. app/build.gradle.kts: minSdk 25 (test only), core library desugaring enabled and dependency added.
2. TaskFlowApp.kt: guard notification-channel creation behind Android 8+ check.

Open the TaskFlow folder within this ZIP as the Android Studio project.
Sync Gradle, Build > Assemble Project, then try Run on BlueStacks.
If there is a build or runtime error, capture the first error and share it.

NOTE: This project has not been built/tested in this environment.
The Render URL and OAuth client ID still need configuration. Firebase and server-dependent features are not proven operational.
