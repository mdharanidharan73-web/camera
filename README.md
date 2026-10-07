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
  - Test scene presets (`Low-Light Portrait`, `Golden Hour Landscape`, `Indoor Studio`) for testing without hardware cameras.

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
  - `OPENAI_API_KEY` remains strictly on the backend and is never embedded in the Android APK.
  - The APK connects to `BACKEND_URL` (`https://realshot-ai-backend.onrender.com`).

---

## How to Build the APK Locally

From the root project directory:

```bash
# Clean project
./gradlew clean

# Build Debug APK
./gradlew assembleDebug --stacktrace
```

On Windows:

```cmd
gradlew.bat clean
gradlew.bat assembleDebug
```

The compiled APK will be created at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## How to Run the GitHub Actions Workflow

1. Push your repository to GitHub:
   ```bash
   git push origin main
   ```
2. Navigate to your GitHub repository in your web browser.
3. Click the **Actions** tab at the top.
4. Select the **Build APK** workflow.
5. You can also run it manually at any time by clicking **Run workflow** → **Run workflow**.

---

## Where to Download the APK from GitHub Actions

1. Open the successful **Build APK** workflow run.
2. Scroll to the bottom to the **Artifacts** section.
3. Click **RealShot-AI-Camera-APK** to download the ZIP file.
4. Extract the ZIP file to get `RealShot-AI-Camera.apk`.

---

## How to Install the APK on Android

1. Transfer `RealShot-AI-Camera.apk` to your phone via USB, Google Drive, or direct download.
2. Open the file on your device using the **Files** app.
3. When prompted, enable **"Allow from this source"** in your Android security settings.
4. Tap **Install** and open **RealShot AI Camera**.
5. Grant camera permissions and start capturing!

---

## How to Configure the Backend & OpenAI API Key

### Deploying to Render:
1. Go to **[dashboard.render.com](https://dashboard.render.com)**.
2. Click **New +** → **Web Service**.
3. Select your repository: `RealShot-AI-Camera`.
4. Configure:
   - **Root Directory:** `backend`
   - **Runtime:** `Node`
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
5. In **Environment Variables**, add:
   - `OPENAI_API_KEY`: *(Your secret OpenAI key, e.g. `sk-proj-...`)*
   - `OPENAI_IMAGE_MODEL`: `gpt-image-2` (or `dall-e-2`)
   - `PORT`: `3000`
   - `NODE_ENV`: `production`
6. Click **Create Web Service**. Your public URL will be:
   `https://realshot-ai-backend.onrender.com`

### Verifying Backend Health:
```bash
curl https://realshot-ai-backend.onrender.com/health
```
Returns:
```json
{"status":"ok"}
```

---

## How to Configure BACKEND_URL in the Android App

The app is pre-configured with the default backend URL `https://realshot-ai-backend.onrender.com`.

If you deploy a new backend:
1. Open the app on your phone.
2. Tap the **Settings** icon (top right of the viewfinder).
3. Scroll to **Secure Backend URL** and enter your backend URL.
4. Tap **Save URL**.

---

## License

Copyright © 2026 RealShot AI Camera. All rights reserved.
