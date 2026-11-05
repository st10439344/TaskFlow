# Publishing TaskFlow to Google Play

This is the evidence of preparation for publication. Tick each item and keep a screenshot as proof.

## 1. Requirements already met in the code
| Requirement | Status |
|---|---|
| Targets **Android 16 (API 36)** (mandatory for new apps and updates since 31 Aug 2026 — Google, 2026) | ✅ `targetSdk = 36` |
| Android App Bundle (`.aab`) | ✅ `./gradlew :app:bundleRelease` |
| Release signing | ✅ configured, needs your keystore (step 2) |
| App icon (adaptive + themed) | ✅ |
| Privacy policy | ✅ `docs/PRIVACY_POLICY.md` (host it, step 5) |
| In-app account deletion | ✅ Settings → Delete account |
| 512×512 icon and 1024×500 feature graphic | ✅ `docs/play-store/` |
| Permissions kept minimal | ✅ internet, network state, notifications |

> **Application ID:** Google Play needs a package name nobody else has used. `com.taskflow.app` may be taken. If you change it, also change it in `app/build.gradle.kts` (`applicationId` and `namespace` can stay), in the Firebase app, and in the Google Cloud Android OAuth client.

## 2. Create the signing key (once) and build
```bash
keytool -genkeypair -v -keystore taskflow-release.jks -alias taskflow \
        -keyalg RSA -keysize 2048 -validity 10000
```
Create `keystore.properties` next to `settings.gradle.kts` (it is git-ignored — **never commit the keystore or this file**):
```
storeFile=taskflow-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=taskflow
keyPassword=YOUR_KEY_PASSWORD
```
Back up the keystore somewhere safe: if you lose it you cannot update the app.

Build (or use Android Studio → *Build → Generate Signed App Bundle / APK*):
```bash
./gradlew :app:assembleRelease    # app/build/outputs/apk/release/app-release.apk   (signed APK)
./gradlew :app:bundleRelease      # app/build/outputs/bundle/release/app-release.aab (upload to Play)
```
Verify the APK is signed: `apksigner verify --verbose app-release.apk` (in the Android SDK `build-tools` folder).

📸 **Evidence:** the signed APK/AAB in the file explorer, and the `apksigner verify` output.

## 3. Screenshots for the store listing (take them on your phone)
Phone screenshots, portrait, at least 2 (use 6–8): login · home with tasks · task form with repeat · task details · search/filters · settings · home in isiZulu · offline banner. Save copies in `docs/screenshots/` (the README uses them).

## 4. Play Console checklist
1. Create a developer account and **Create app** (name, default language English, App, Free).
2. **Store listing** — paste the text below, upload icon, feature graphic and screenshots.
3. **App content:** privacy policy URL · Ads: *No* · App access: *all functionality available* (or give a test login) · Content rating questionnaire · Target audience: 13+ (not designed for children) · **Data safety** (below) · **Account deletion:** in-app path *Settings → Delete account*, plus the privacy-policy page.
4. **Release → Testing → Internal testing** → upload the `.aab`.
5. New personal developer accounts may have to run a closed test with a minimum number of testers for a set period before applying for production — check the current rules in Play Console. Publishing is optional for the PoE; the **upload screenshot** is the evidence.

📸 **Evidence:** the Play Console screen showing the uploaded bundle / completed store listing.

### Data safety answers
| Data type | Collected | Shared | Purpose | Optional? |
|---|---|---|---|---|
| Name, e-mail | Yes | No | Account management | No |
| User-generated content (tasks, lists) | Yes | No | App functionality | No |
| Device or other IDs (notification token) | Yes | No | App functionality | Yes (notifications) |
* Data is encrypted in transit: **Yes**. Users can request deletion: **Yes** (in app).

### Store listing text
**Title (≤ 30):** TaskFlow: To-Do & Tasks  
**Short description (≤ 80):** Offline-first to-do lists with reminders, streaks and isiZulu / Afrikaans.  
**Full description:**
> TaskFlow is a simple, fast task manager that works even when you have no data or Wi-Fi.
>
> • Works offline — add, edit and complete tasks anywhere; changes sync automatically when you are back online
> • Lists, subtasks, priorities and due dates
> • Repeating tasks (daily, weekly, monthly)
> • Search, plus Today, Overdue and Upcoming views
> • Reminders and real-time notifications
> • Daily streaks and weekly progress to keep you motivated
> • Available in English, isiZulu and Afrikaans
> • Sign in with e-mail or Google; dark theme included
>
> Plan your day, your way.

## References
Android Developers (n.d.) *Sign your app*. Available at: https://developer.android.com/studio/publish/app-signing  
Google (2026) *Target API level requirements for Google Play apps*. Available at: https://support.google.com/googleplay/android-developer/answer/11926878
