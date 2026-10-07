import { Router, Request, Response, NextFunction } from 'express';
import multer, { FileFilterCallback } from 'multer';
import path from 'path';
import fs from 'fs';

export const enhanceRouter = Router();

// Ensure upload directory exists
const uploadDir = path.join(__dirname, '../../uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Multer storage configuration
const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, uploadDir);
  },
  filename: (_req, file, cb) => {
    const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1e9)}`;
    const ext = path.extname(file.originalname).toLowerCase() || '.png';
    cb(null, `capture-${uniqueSuffix}${ext}`);
  }
});

// File validation: JPEG, PNG, WEBP, max 25MB
const fileFilter = (_req: Request, file: Express.Multer.File, cb: FileFilterCallback) => {
  const allowedMimeTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/jpg'];
  if (allowedMimeTypes.includes(file.mimetype.toLowerCase())) {
    cb(null, true);
  } else {
    cb(new Error('Invalid image format. Only JPEG, PNG, and WebP are supported.'));
  }
};

const upload = multer({
  storage,
  limits: {
    fileSize: 25 * 1024 * 1024 // 25 MB max limit
  },
  fileFilter
});

export const PROFESSIONAL_ENHANCEMENT_PROMPT = `Edit this exact photograph as a high-end professional photographer would.

The input photograph is the source of truth.

Improve only photographic quality.

Return the SAME photograph, not a newly invented scene.

Preserve the exact:
- person
- identity
- face
- facial structure
- facial proportions
- expression
- skin tone
- skin texture
- hair
- clothing
- objects
- environment
- background
- composition
- perspective
- photographic content

Do not add anything.

Do not remove anything.

Do not replace anything.

Do not invent anything.

Do not regenerate unclear details.

Do not change facial features.

Do not reshape the face.

Do not change the person's identity.

Do not change age.

Do not change body shape.

Do not whiten skin.

Do not apply beauty filters.

Do not smooth skin excessively.

Preserve realistic pores and natural skin texture.

Correct exposure, highlights, shadows, white balance, contrast, color, noise, sharpness and clarity according to the original photograph.

For low light, naturally improve exposure and reduce noise while preserving the original atmosphere.

For harsh light, balance highlights and shadows.

For flat lighting, add subtle professional tonal depth.

Use realistic environmental color correction.

Outdoor photographs should remain naturally colored.

Indoor photographs should have accurate white balance.

Night photographs should remain nighttime photographs.

Only improve background separation when it is naturally supported by the photograph.

Do not create artificial background blur if it would look like a cutout.

Do not create fake details.

Do not create artificial HDR.

Do not oversaturate colors.

Do not oversharpen.

Do not make the image look AI-generated.

The final image must look like the SAME photograph captured with a significantly better professional camera and edited naturally by a human photographer.

REAL.
NATURAL.
PHOTOGRAPHIC.
SUBTLE.
PROFESSIONAL.

The image must remain faithful to the original photograph.`;

/**
 * POST /api/enhance
 * Accepts multipart/form-data with field: image
 */
enhanceRouter.post(
  '/',
  upload.single('image'),
  async (req: Request, res: Response): Promise<void> => {
    const file = req.file;

    if (!file) {
      res.status(400).json({
        error: 'Missing image file. Please provide an image using form-data field "image".'
      });
      return;
    }

    const apiKey = (process.env.OPENAI_API_KEY || '').trim();
    const model = (process.env.OPENAI_IMAGE_MODEL || 'gpt-image-2').trim();

    if (!apiKey) {
      // Safe cleanup of temporary upload
      fs.unlink(file.path, () => {});
      res.status(500).json({
        error: 'OPENAI_API_KEY is not configured on the backend server. Please configure OPENAI_API_KEY in Render environment variables.'
      });
      return;
    }

    try {
      const imageBytes = await fs.promises.readFile(file.path);
      const blob = new Blob([imageBytes], { type: file.mimetype || 'image/png' });

      const formData = new FormData();
      formData.append('image', blob, 'image.png');
      formData.append('prompt', PROFESSIONAL_ENHANCEMENT_PROMPT);
      formData.append('model', model);
      formData.append('n', '1');
      formData.append('size', '1024x1024');
      formData.append('response_format', 'b64_json');

      const openAiResponse = await fetch('https://api.openai.com/v1/images/edits', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${apiKey}`
        },
        body: formData
      });

      if (!openAiResponse.ok) {
        const errorText = await openAiResponse.text();
        let message = `OpenAI API returned status ${openAiResponse.status}`;
        try {
          const errObj = JSON.parse(errorText);
          message = errObj.error?.message || message;
        } catch {
          message = `${message}: ${errorText}`;
        }
        res.status(502).json({ error: message });
        return;
      }

      const json = (await openAiResponse.json()) as any;
      if (!json.data || json.data.length === 0) {
        res.status(502).json({ error: 'OpenAI returned an empty image response.' });
        return;
      }

      const b64 = json.data[0]?.b64_json;
      if (b64) {
        const enhancedBuffer = Buffer.from(b64, 'base64');
        res.setHeader('Content-Type', 'image/png');
        res.setHeader('Content-Disposition', 'inline; filename="realshot_enhanced.png"');
        res.send(enhancedBuffer);
        return;
      }

      const url = json.data[0]?.url;
      if (url) {
        const fetchImg = await fetch(url);
        const arrayBuf = await fetchImg.arrayBuffer();
        const buffer = Buffer.from(arrayBuf);
        res.setHeader('Content-Type', 'image/png');
        res.setHeader('Content-Disposition', 'inline; filename="realshot_enhanced.png"');
        res.send(buffer);
        return;
      }

      res.status(502).json({ error: 'No image data found in OpenAI response.' });
    } catch (err: any) {
      res.status(500).json({
        error: err.message || 'An unexpected error occurred during photographic enhancement.'
      });
    } finally {
      // Always remove temporary file
      fs.unlink(file.path, () => {});
    }
  }
);
