# Use of AI tools — Portfolio of Evidence

**Tool:** Claude (Anthropic, 2026), a large language model used through the claude.ai chat. *(Check the exact model name shown in your chat and update the reference below.)*

## How AI was used
The group produced the Part 1 research and design: requirements, UI wireframes, API table, UML and data dictionary. Claude was then used as a coding assistant to implement that design across Parts 2 and 3:

* **Backend:** the first version of the Node.js/Express API (routes, validation, streak and sync logic) and, for the PoE, Firebase Cloud Messaging push notifications, device registration and account deletion.
* **Android (Kotlin):** the Room/Retrofit/WorkManager structure, the seven screens and, for the PoE, push handling, repeating tasks, search/filter/sort, translated error messages and edge-to-edge layouts for API 36.
* **Testing and CI:** unit tests (validators, dates, sync reconciliation, recurrence, filters) and the GitHub Actions workflows.
* **Documentation and assets:** drafts of the README, release notes, privacy policy, Play Store text and demo script, and the YouTube thumbnail and Play Store graphics (generated with a Python imaging library).
* **Research check:** a web search confirmed that Google Play now requires apps to target API 36 (Google, 2026), so the build was upgraded.

## Example
Repeating tasks are calculated from the original due date so a monthly task due on the 31st does not drift to the 28th after February:

```kotlin
// Generated with AI assistance (Anthropic, 2026); reviewed and unit-tested by the group
"monthly" -> start.plusMonths(n)   // plusMonths clamps to the last day of short months
```

Prompts were iterative: for example, we pasted the assignment rubric and the Part 2 lecturer feedback and asked for the missing PoE features, then asked follow-up questions about build errors.

## Verification and ownership
Claude could not run Android Studio, so its output was not trusted blindly. The pure Kotlin logic was compiled and run with 37 extra checks, the backend's 40 automated tests pass, and the group built the app in Android Studio, **[describe the errors you fixed]**, and tested every feature on a **[phone model]**. Translations were checked by **[name of isiZulu / Afrikaans speaker]**. Firebase, Google Cloud, MongoDB Atlas and Render were configured by the group, who also recorded the video, made the GitHub commits and take responsibility for the submitted work.

## Limitations
AI-written code can look correct and still contain mistakes, and its isiZulu and Afrikaans text needs a human check. Every feature was therefore tested by hand and by automated tests before submission.

## References
Anthropic (2026) *Claude [Large language model]*. Available at: https://claude.ai (Accessed: 31 October 2026).  
Google (2026) *Target API level requirements for Google Play apps*. Available at: https://support.google.com/googleplay/android-developer/answer/11926878 (Accessed: 31 October 2026).
