# RealShot AI Camera

> **"Capture real. Enhance naturally."**

A commercial-grade, native Android camera application and companion proxy backend that enhances photographs using OpenAI's image editing technology while **strictly preserving** the original person, identity, facial structure, skin tone, pores, clothing, and scene composition.

---

## Architecture Overview

```
[Android Phone]                          [Node.js Backend]                     [OpenAI API]
  CameraX Preview                                :3000                                 
        │                                          │                                         
   Capture Photo                                   │                                         
        │                                          │                                         
  Preserve Original                                │                                         
        │                                          │                                         
  POST /api/enhance (multipart) ─────────────────► │                                         
                                             Validate Image                                  
                                                   │                                         
                                          POST /v1/images/edits ──────────────►              
                                          (Strict Prompt + Photo)          AI Technical Edit 
                                                   │ ◄───────────────────────────────┘       
                                          Clean Temp Files                                   
  ◄─────────────────────────────────────── Return Enhanced PNG                               
        │                                                                                    
Before/After Slider                                                                          
        │                                                                                    
Auto-Save Gallery                                                                            
(RealShot_YYYYMMDD_HHMMSS.jpg)                                                               
```

---

## Features

- **Full-Screen Camera Viewfinder**:
  - Edge-to-edge CameraX preview with tap-to-focus animation reticle.
  - Flash modes (`Off`, `Auto`, `On`).
  - Native zoom selectors (`0.5x`, `1x`, `2x`).
  - Photography modes (`PHOTO`, `PORTRAIT`, `NIGHT`).
  - Large tactile circular shutter with DSLR feel.
  - Test scene presets (`Low-Light Portrait`, `Golden Hour Landscape`, `Indoor Studio`) for emulator testing.

- **Strict Image & Identity Preservation**:
  - The captured photograph is the absolute source of truth.
  - No new people, objects, artificial backgrounds, or hallucinated items.
  - 100% natural facial geometry, pores, skin textures, and lighting directions preserved.
  - No plastic beauty filters or artificial smoothing.

- **Dual-Engine Architecture**:
  - **OpenAI Image Editing**: Sends image via secure proxy backend to OpenAI Image Edits API with detailed professional photographic instructions.
  - **On-Device DSLR RAW Computational Engine**: Instant local fallback for offline usage, featuring histogram tone mapping, shadow recovery, and Laplacian micro-contrast filters.

- **Result Screen & Interactive Comparison**:
  - Draggable `ORIGINAL ↔ ENHANCED` before/after split slider.
  - Direct actions: **Save**, **Share**, and **Retake**.
  - Error recovery: **Retry** and **Use Original** buttons if network is unavailable.

- **Gallery & CSV Export**:
  - Automatically saves enhanced photos to `Pictures/RealShot AI/` as `RealShot_YYYYMMDD_HHMMSS.jpg`.
  - Optional unedited backup as `RealShot_Original_YYYYMMDD_HHMMSS.jpg`.
  - In-app **Export CSV** button in Gallery to export photo records.

- **Enterprise Security**:
  - `OPENAI_API_KEY` remains on the backend and is never embedded in the Android APK.
  - Masked API key display (`sk-proj-••••••••`) in Settings.

---

## Project Structure

```
RealShot-AI-Camera/
├── app/                              # Android application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/     # Kotlin & Jetpack Compose source code
│   │   │   │   ├── ai/               # BackendApiClient, OpenAiEnhancer, DslrPhotoEngine
│   │   │   │   ├── data/             # Models, SettingsRepository, PhotoStorage
│   │   │   │   ├── ui/               # CameraScreen, EnhancedResultScreen, GalleryScreen
│   │   │   │   └── viewmodel/        # CameraViewModel
│   │   │   └── res/                  # Drawables, mipmaps, strings, colors
│   │   └── test/                     # Robolectric & unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── backend/                          # Node.js + TypeScript Express backend proxy
│   ├── src/
│   │   ├── index.ts                  # POST /api/enhance endpoint
│   │   └── openaiService.ts          # OpenAI Image Edits API integration
│   ├── package.json
│   ├── tsconfig.json
│   └── .env.example
│
├── .github/
│   └── workflows/
│       └── build-apk.yml             # GitHub Actions CI/CD to build & upload APK artifact
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew                           # Linux/macOS Gradle wrapper executable
├── gradlew.bat                       # Windows Gradle wrapper executable
├── .gitignore
├── .env.example
└── README.md
```

---

## Requirements

- **Android App**:
  - Android 8.0+ (API level 24+)
  - JDK 17
  - Android SDK with platform 36
- **Backend**:
  - Node.js 18+ or 20+
  - npm or yarn

---

## Quick Start & Local Setup

### 1. Clone Repository

```bash
git clone YOUR_REPOSITORY_URL
cd RealShot-AI-Camera
```

### 2. Configure Backend & OpenAI API Key

```bash
cd backend
cp .env.example .env
```

Open `backend/.env` and insert your OpenAI credentials:

```properties
OPENAI_API_KEY=sk-proj-YOUR_ACTUAL_OPENAI_KEY
OPENAI_IMAGE_MODEL=dall-e-2
PORT=3000
```

Install dependencies and start the backend:

```bash
npm install
npm run dev
```

The backend starts at `http://localhost:3000`. (Android Emulator connects to `http://10.0.2.2:3000`).

---

## How to Build the APK

### Local Command Line

From the root project directory:

```bash
./gradlew assembleDebug
```

On Windows:

```cmd
gradlew.bat assembleDebug
```

The compiled APK will be located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## How to Download APK from GitHub Actions

1. Push your code to your GitHub repository:
   ```bash
   git push origin main
   ```
2. Navigate to your GitHub repository in your web browser.
3. Click on the **Actions** tab at the top.
4. Select the **Build RealShot AI Camera APK** workflow run.
5. Once the run completes with **BUILD SUCCESSFUL**, scroll down to the **Artifacts** section.
6. Click **RealShot-AI-Camera-APK** to download the ZIP file.
7. Extract the ZIP file to get `RealShot-AI-Camera-debug.apk`.

---

## How to Install the APK on Android

1. Transfer `RealShot-AI-Camera-debug.apk` to your Android device via USB, Google Drive, or direct download.
2. Open the file on your device using the **Files** app.
3. If prompted, enable **"Allow from this source"** in your Android security settings.
4. Tap **Install** and open **RealShot AI Camera**.
5. Grant camera permissions and capture photographs with professional AI enhancement!

---

## License

Copyright © 2026 RealShot AI. All rights reserved.
