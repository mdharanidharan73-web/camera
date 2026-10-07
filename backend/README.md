# RealShot AI Camera Backend

Production-ready Node.js + TypeScript + Express backend proxy for **RealShot AI Camera**.

This backend receives photographs from the Android client via `POST /api/enhance`, validates them, forwards them securely to the OpenAI Image Editing API using server-side credentials, and returns the enhanced image while safely cleaning up temporary files.

The Android APK **never** contains your `OPENAI_API_KEY`. The key exists exclusively in your backend environment.

---

## API Endpoints

### 1. Health Check
- **Method:** `GET /health`
- **Response:**
  ```json
  {
    "status": "ok"
  }
  ```

### 2. Photographic Enhancement
- **Method:** `POST /api/enhance`
- **Content-Type:** `multipart/form-data`
- **Body Fields:**
  - `image` *(Required)*: The captured photograph (JPEG or PNG, up to 25MB)
- **Response:**
  - Returns binary enhanced image with header `Content-Type: image/png`
- **Error Response:**
  ```json
  {
    "error": "Error description message"
  }
  ```

---

## Local Development

### 1. Install Dependencies
```bash
npm install
```

### 2. Set Up Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

Edit `.env`:
```properties
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_IMAGE_MODEL=gpt-image-2
PORT=3000
```

### 3. Build & Run
```bash
# Run in development mode with live reload
npm run dev

# Or build TypeScript and start production server
npm run build
npm start
```

---

## Step-by-Step Render Deployment Guide

Follow these exact steps to deploy this backend to Render and obtain a public HTTPS URL:

### Step 1: Create a Render Account
1. Visit [render.com](https://render.com) and sign in (using GitHub is recommended).

### Step 2: Create a New Web Service
1. Click **New +** → **Web Service**.
2. Connect your GitHub repository: `RealShot-AI-Camera`.
3. Configure the following fields:
   - **Name:** `realshot-ai-backend` (or your preferred name)
   - **Root Directory:** `backend`
   - **Runtime:** `Node`
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Instance Type:** `Free`

### Step 3: Configure Environment Variables in Render
In the **Environment Variables** section on Render, add:
- `OPENAI_API_KEY`: *(Paste your real OpenAI API key, e.g. `sk-proj-...`)*
- `OPENAI_IMAGE_MODEL`: `gpt-image-2`
- `NODE_ENV`: `production`
- `PORT`: `3000`

> ⚠️ **Security Guarantee**: Render encrypts your environment variables at rest. The key is never displayed in code or committed to GitHub.

### Step 4: Deploy the Web Service
Click **Create Web Service**. Render will:
1. Clone the `backend/` directory
2. Run `npm install` and `npm run build`
3. Launch `node dist/server.js` on port `3000`

### Step 5: Verify Deployment
Once Render marks the service as **Live**, your public HTTPS URL will be displayed at the top of the dashboard, for example:
```
https://realshot-ai-backend.onrender.com
```

Test your deployed health endpoint in your browser or terminal:
```bash
curl https://realshot-ai-backend.onrender.com/health
```
Expected response:
```json
{"status":"ok"}
```

---

## Configuring the Android App & GitHub

Once your Render backend is live:

1. **In the Android App**:
   - Open **Settings** (gear icon in the top right of the viewfinder).
   - Enter your public Render backend URL in the **Secure Backend URL** field:
     `https://realshot-ai-backend.onrender.com`
   - Tap **Save URL**.

2. **In GitHub Secrets / Variables** (Optional for CI/CD):
   - Navigate to your GitHub repository → **Settings** → **Secrets and variables** → **Actions**.
   - You can add:
     - `BACKEND_URL`: `https://realshot-ai-backend.onrender.com`
     - `OPENAI_IMAGE_MODEL`: `gpt-image-2`
