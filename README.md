# MOLIDO PHONE AI

Lightweight offline-first Android phone companion.

## Included
- Offline local assistant brain (no API key / VPS required)
- Local memory and notes
- Persian / English UI
- Dark / light mode
- Chat history for the current app session
- GitHub Actions debug APK build
- Android 26+ / Java 17 / Kotlin / Jetpack Compose

## Build
The GitHub Actions workflow installs Gradle and Android SDK components automatically. No VPS is required.

After pushing to `main`:
`Actions -> Android APK Build -> Artifacts -> molido-debug-apk`

## Important
This base release intentionally does not send user text to external AI services. Cloud AI, voice, phone automation, and other integrations can be added later behind explicit permissions and configuration.
