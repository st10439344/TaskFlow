# Use of Artificial Intelligence Tools — Portfolio of Evidence

## Introduction

Artificial intelligence (AI) tools were used as supplementary resources during the development of TaskFlow, an Android task management application. The group used Claude, developed by Anthropic, and ChatGPT, developed by OpenAI, to assist with selected coding challenges, technical explanations, troubleshooting and documentation (Anthropic, 2026; OpenAI, 2026). These tools supported the development process but did not replace the group's involvement in planning, configuring, testing and evaluating the application.

## How AI Tools Were Used

### 1. Research and Planning

The initial requirements, application objectives, interface designs and system structure were established during the earlier stages of the Portfolio of Evidence. AI tools were subsequently used to clarify technical concepts and explore possible implementation approaches. The group reviewed these suggestions against the project's requirements before deciding which solutions to use.

### 2. Application Development

Claude assisted with portions of the Kotlin Android application and Node.js backend, including suggestions for task management, database integration, offline synchronisation, recurring tasks and notification handling.

For example, AI assistance was used when implementing recurring-task calculations. The group reviewed how recurring dates should behave when months have different numbers of days and included unit tests to check the resulting behaviour.

AI-generated suggestions were not treated as automatically correct. Code was integrated into the project, reviewed and adjusted where necessary.

### 3. Troubleshooting and Testing

ChatGPT was used during Part 3 to help interpret error messages and identify possible solutions to technical problems encountered during implementation.

This included troubleshooting Firebase configuration, Google authentication settings, Android build errors and communication between the application and backend.

The group performed the practical testing. TaskFlow was installed and tested using BlueStacks and a physical Xiaomi Android device. The group also tested offline synchronisation by temporarily stopping the backend, creating tasks and checking whether the pending changes were uploaded to MongoDB Atlas after the backend restarted.

These tests provided practical evidence of the application's behaviour rather than relying only on AI explanations.

### 4. Documentation

AI tools assisted with organising and improving the wording of technical documentation, including README content, setup instructions and testing descriptions.

The group remained responsible for checking that the documentation accurately reflected the implemented functionality and actual test results. Features that were not fully working, including Google Sign-In and reliable Firebase push notification delivery, were identified as limitations rather than reported as successful.

## Critical Reflection

AI assistance was useful for understanding unfamiliar technologies, interpreting error messages and exploring alternative solutions. However, some suggestions required correction or additional testing before they could be used successfully.

This demonstrated the importance of independent verification, particularly when working with authentication, databases and external services.

The group learned that AI-generated code should be treated as a starting point requiring human judgement, testing and refinement. Overall, AI supported the development process, while the group retained responsibility for the project's implementation decisions, practical evaluation and final submission.

## References

Anthropic (2026) *Claude*. Available at: https://claude.ai/ (Accessed: 31 October  2026).

OpenAI (2026) *ChatGPT*. Available at: https://chatgpt.com/ (Accessed: 31 October 2026).
```

Prompts were iterative: for example, we pasted the assignment rubric and the Part 2 lecturer feedback and asked for the missing PoE features, then asked follow-up questions about build errors.

## Verification and ownership
Claude could not run Android Studio, so its output was not trusted blindly. The pure Kotlin logic was compiled and run with 37 extra checks, the backend's 40 automated tests pass, and the group built the app in Android Studio, **[describe the errors you fixed]**, and tested every feature on a **[phone model]**. Translations were checked by **[name of isiZulu / Afrikaans speaker]**. Firebase, Google Cloud, MongoDB Atlas and Render were configured by the group, who also recorded the video, made the GitHub commits and take responsibility for the submitted work.

## Limitations
AI-written code can look correct and still contain mistakes, and its isiZulu and Afrikaans text needs a human check. Every feature was therefore tested by hand and by automated tests before submission.

## References
Anthropic (2026) *Claude [Large language model]*. Available at: https://claude.ai (Accessed: 31 October 2026).  
Google (2026) *Target API level requirements for Google Play apps*. Available at: https://support.google.com/googleplay/android-developer/answer/11926878 (Accessed: 31 October 2026).
