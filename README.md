# Lost & Found (Findly) 🔍📱

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

An intuitive and modern Android mobile application built with **Jetpack Compose** and **Google Firebase** to help university students and campus communities report, track, and recover lost and found belongings effortlessly.

---

## ✨ Key Features

- **🔐 Campus Authentication**: Fast sign-up, sign-in, and password reset powered by Firebase Authentication with student details (Roll Number, Batch, Semester, Student ID).
- **📋 Real-time Feed**: Live timeline of lost and found items powered by Cloud Firestore real-time snapshots.
- **🏷️ Dynamic Filtering & Search**: Instant keyword search across post titles and descriptions with category filter pills (Electronics, Wallets, IDs, Keys, Books, Accessories, etc.).
- **📸 Photo Uploads & Inspector**: Attach pictures to item posts with base64 compression and interactive full-screen photo viewer dialog.
- **💬 Real-Time In-App Messaging**: Instant 1-on-1 private messaging between finders and owners with live typing indicators and presence updates.
- **💬 Post Comments**: Community members can leave comments and helpful tips directly under any listing.
- **🤝 Claims & Resolution Tracking**: Mark items as "Claimed", "Found", or "Resolved" with color-coded status badges.
- **👤 Profile Management & Activity**: View personal post history, claim metrics, update details, or log out securely.
- **🎨 Modern Material 3 UI**: Polished brand themes with animated pink gradients, edge-to-edge layout, responsive dark/light theme support, and haptic feedback.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Asynchronous & Reactive:** Kotlin Coroutines & `StateFlow` / `callbackFlow`
- **Backend as a Service (BaaS):**
  - **Firebase Authentication:** Secure email/password user authentication & session management
  - **Cloud Firestore:** Real-time NoSQL cloud database for post feeds, comments, and direct chat messages
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Gradle Version Catalog (`libs.versions.toml`)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Hedgehog 2023.1.1 or newer recommended)
- JDK 11 or higher
- Android SDK 34+
- Android device or emulator running API 24 (Android 7.0) or higher
- A configured Firebase project with Authentication and Firestore enabled

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/rajsinghpiyush/Lost-Found.git
   cd Lost-Found
   ```

2. **Open in Android Studio:**
   - Open Android Studio and select `Open...`
   - Select the `Lost-Found` folder and allow Gradle to sync.

3. **Firebase Configuration:**
   - Place your `google-services.json` inside the `app/` directory (starter configuration included).
   - Ensure Email/Password sign-in and Cloud Firestore are enabled in your Firebase console.

4. **Run the Project:**
   - Connect an Android device with USB debugging enabled, or launch an Android emulator.
   - Click **Run 'app'** (`Shift + F10`) or run from terminal:
     ```bash
     ./gradlew installDebug
     ```

---

## 📂 Project Structure

```
Lost-Found/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/iktiarnazib/lostandfoundv2/
│   │   │   │   ├── MainActivity.kt        # App navigator, ViewModels, screen composables & logic
│   │   │   │   └── ui/theme/              # Material 3 colors, typography, theme definition
│   │   │   ├── res/                       # Drawables, mipmaps, XML strings & app themes
│   │   │   └── AndroidManifest.xml        # Application declarations & permissions
│   │   └── test/                          # Unit and instrumentation tests
│   ├── build.gradle.kts                   # App-level dependencies & build rules
│   └── google-services.json               # Firebase configuration
├── gradle/
│   └── libs.versions.toml                 # Version catalog for dependencies & plugins
├── build.gradle.kts                       # Root build configuration
├── settings.gradle.kts                    # Project & repository settings
└── README.md                              # Project documentation
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
