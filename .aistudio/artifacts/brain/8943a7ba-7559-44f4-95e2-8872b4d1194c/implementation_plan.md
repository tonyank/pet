# Implementation Plan - U的桌宠 (Android Floating Desktop Pet)

An Android floating desktop pet app developed in Kotlin with Jetpack Compose (Material 3) and Kotlin DSL, featuring the character "小傻子" who sleeps on the home screen, speaks funny lines on tap, or kisses the woman with authentic video audio, supported by full character customization and a GitHub Actions CI/CD release pipeline.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were aligned based on your preferences:
> - **Kissing Animation & Audio**: Lightweight frame-by-frame sprite sequence and extracted audio track, providing silky-smooth overlay performance without video surface lag or background battery drain.
> - **Screen Interaction**: Freely draggable overlay with smooth touch physics, smart screen-edge docking, and reactive speech bubbles.
> - **Character System**: Full Jetpack Compose character manager allowing character switching, custom quotes, avatar selection, and sound controls.

---

## 1. Overview & Core Concept

- **What It Does**: Runs an interactive floating desktop pet directly on Android devices via an overlay foreground service. The pet floats on top of other apps, reacts to taps, displays humorously timed dialogue bubbles, and triggers a full kissing animation with video audio.
- **Target Audience**: Users seeking a funny, personalized, and interactive companion on their Android home screen and daily app workflow.
- **Core Value**: Zero-distraction entertainment, seamless background execution across Android 7.0 (API 24) to Android 14+ (API 34/35), and extensible character customization.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First Launch & Permission Flow**:
   - Welcome screen with Material 3 card explaining the Overlay (`SYSTEM_ALERT_WINDOW`) and Notification permissions.
   - One-tap button jumping directly to Android system settings, automatically updating permission badges on return.
2. **Floating Pet Activation & Control**:
   - Master switch to launch or dismiss the floating pet.
   - Quick preview panel to test dialogue bubbles and the kiss animation directly in-app.
   - Drag-to-move, tap-to-interact, and fling-to-snap on the floating desktop overlay.
3. **Character Manager & Customization**:
   - Default character: "小傻子" (with default snoring animation, 5 speech bubbles, and the couple kiss scene).
   - "New Character" sheet: configure custom pet name, sleep icon, reaction dialogs, and audio triggers.
   - Quick active character switcher.
4. **Pet Behavior Settings**:
   - Pet scale slider (50% to 150%), idle sleep timeout, speech bubble duration, and audio volume.

### Visual Identity & Theme
- **Theme**: Modern Material Design 3 with dynamic color theming, playful rounded surfaces, and pastel accents.
- **Color Palette**:
  - Primary: Warm Coral (`#FF6F61`) & Rose Petal (`#FCE4EC`)
  - Surface: Pure White / Dark Slate (`#121212` in dark mode)
  - Accent / Speech Bubble: Warm Amber (`#FFF8E1`) with subtle drop shadow
- **Overlay Visuals**:
  - Resting State: "小傻子" avatar breathing gently with floating "zZZ" particles.
  - Interactive State: Expressive tap reaction + dynamic speech bubble floating above the character with tail pointing to the pet.
  - Kiss Sequence: Woman appears beside the pet, smooth transition into the kiss animation, accompanied by video audio, followed by a romantic blush effect.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Floating Window Architecture (Service + WindowManager)**
  - *Chosen Approach*: Android `Service` managing a `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` view with touch interception.
  - *Why*: Follows the battle-tested architecture of GooseDroid while modernizing for Android 14 foreground service types (`specialUse` / `mediaPlayback`) and memory efficiency.
- **Decision 2: Animation Rendering Strategy**
  - *Chosen Approach*: Pre-extracted optimized frame sequence and raw audio rather than heavyweight ExoPlayer video overlay.
  - *Why*: Instantaneous startup, zero memory leaks, full alpha transparency support on desktop, and immediate touch responsiveness.
- **Decision 3: Storage & State Management**
  - *Chosen Approach*: Android Jetpack DataStore / Room for character profiles, user dialogues, and preferences.
  - *Why*: Asynchronous, reliable, crash-resilient storage adhering to modern Android development standards.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      Android System                         │
│   ┌─────────────────────────────────────────────────────┐   │
│   │           WindowManager (Overlay Layer)             │   │
│   │   ┌───────────────────────────┐                     │   │
│   │   │     FloatingPetView       │◄─── Touch & Drag    │   │
│   │   │  - Sleeping Avatar (zZZ)  │                     │   │
│   │   │  - Dialogue Bubble Window │                     │   │
│   │   │  - Kiss Animation Sprite  │                     │   │
│   │   └─────────────┬─────────────┘                     │   │
│   └─────────────────┼───────────────────────────────────┘   │
└─────────────────────┼───────────────────────────────────────┘
                      │ Controlled by
┌─────────────────────▼───────────────────────────────────────┐
│              PetFloatingService (Foreground Service)         │
│  - WindowManager touch listener & screen snapping           │
│  - SoundPool / MediaPlayer audio playback engine            │
│  - Random action dispatcher (5 dialogues + kiss animation)  │
└─────────────────────▲───────────────────────────────────────┘
                      │ IPC / Shared State
┌─────────────────────┴───────────────────────────────────────┐
│             MainActivity (Single Activity Architecture)     │
│  - Jetpack Compose with Material 3 Navigation               │
│  ├── DashboardScreen (Service toggle, overlay preview)      │
│  ├── CharacterManagerScreen (Preset & custom pets)          │
│  └── SettingsScreen (Scale, opacity, audio volume)          │
└─────────────────────────────────────────────────────────────┘
```

### Random Dialogue Pool & Actions for "小傻子"
1. `“我想酱酱了”` (Speech Bubble)
2. `“我想阳阳了”` (Speech Bubble)
3. `“我想圈圈了”` (Speech Bubble)
4. `“我是扬州小傻子”` (Speech Bubble)
5. `“我想去嫖娼”` (Speech Bubble)
6. **Special Action: Kiss Scene** (Left woman appears, couple kissing animation plays, matching audio plays).

### Project Configuration Details
- **Package Name**: `ytstudio.pet`
- **Application Name**: `U的桌宠`
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Language**: Kotlin 2.0+
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`)
- **UI Toolkit**: Jetpack Compose BOM + Material 3

### GitHub Actions Workflow Specification
- **Path**: `.github/workflows/build-apk.yml`
- **Trigger**: `workflow_dispatch` with input parameter:
  - `build_type`: Choice (`release` or `debug`), default: `release`
- **Environment**:
  - `actions/checkout@v4`
  - `actions/setup-java@v4` with Java 17 Temurin
  - `gradle/actions/setup-gradle@v4` for caching
  - Android Keystore decode from `${{ secrets.KEYSTORE_BASE64 }}`
  - Signing with apksigner using `${{ secrets.KEYSTORE_PASSWORD }}`, `${{ secrets.KEY_ALIAS }}`, `${{ secrets.KEY_PASSWORD }}`
  - Upload signed APK via `actions/upload-artifact@v5` with 30-day retention.

---

## 5. Implementation Roadmap & Verification

1. **Asset Processing**: Extract high-quality frame sequences and audio track from `/public/assets/aistudio/5-____-10_27__dc505bf2.mp4` into Android drawable/raw resources.
2. **Android Project Scaffolding**: Setup root and app `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, and Gradle wrapper.
3. **Core Services**:
   - `PetFloatingService.kt`: WindowManager lifecycle, touch gesture handling, edge magnet snapping.
   - `PetActionManager.kt`: Speech bubble rendering and audio playback.
   - `CharacterRepository.kt`: Persistent storage for characters and dialogues.
4. **Jetpack Compose UI**:
   - `DashboardScreen.kt`, `CharacterManagerScreen.kt`, `SettingsScreen.kt`, `Navigation.kt`.
5. **CI/CD Pipeline**: Write `.github/workflows/build-apk.yml`.
6. **Compilation & Testing**: Run Gradle build in the cloud environment to verify APK compilation.
