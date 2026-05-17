# Memos Android Client

A native Android note-taking app for [Memos](https://usememos.com) — built with Kotlin, Jetpack Compose, and offline-first architecture.

## Features

- Connect to any Memos instance
- Create, edit, delete text notes with Markdown support
- Offline-first with automatic sync
- Search and filter notes locally
- Pin notes and toggle visibility (Private / Protected / Public)
- **Tag management** with chip input
- **Biometric unlock** with encrypted token storage
- **Attachment support** (images, files) via multipart upload
- **Home screen widget** showing recent notes
- Pull-to-refrash sync

## Tech Stack

- **UI**: Jetpack Compose (Material3)
- **Architecture**: MVVM with Repository pattern
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + Gson
- **Local DB**: Room
- **Settings**: DataStore Preferences
- **Markdown**: Markwon

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK (API 26+)

### Build Locally

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device

### GitHub Actions CI/CD

This repo includes a GitHub Actions workflow that automatically builds the debug APK on every push to `main` or `develop`.

**How to use:**

1. Push this repo to GitHub
2. Go to **Actions** tab in your repo
3. The workflow `Build APK` will run automatically
4. Once finished, download the APK from the **Artifacts** section

### Setup GitHub Actions

1. Create a new repo on GitHub
2. Push this code:
   ```bash
   cd android-memos-client
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin git@github.com:wzul/memos-android.git
   git push -u origin main
   ```
3. Go to **Actions → Build APK** and click **Run workflow** to test

## Configuration

The app requires:
- **Instance URL**: Your Memos server URL (e.g., `https://memos.example.com`)
- **Access Token**: Generate from Memos web app → Settings → Access Tokens

## Project Structure

```
app/src/main/java/com/example/memos/
├── data/
│   ├── api/          # Retrofit interface + DTOs
│   ├── db/           # Room entities + DAOs
│   ├── model/        # Domain models
│   └── repository/   # Repository interfaces + implementations
├── di/               # Hilt modules
├── sync/             # SyncManager
├── ui/
│   ├── components/   # Reusable UI components
│   ├── navigation/   # Compose Navigation
│   ├── screens/      # Login, MemoList, MemoEdit, Settings
│   └── theme/        # Material3 theme
└── MainActivity.kt
```

## License

MIT