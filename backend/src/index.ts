import express, { Request, Response } from 'express';
import cors from 'cors';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import dotenv from 'dotenv';
import { enhanceImageWithOpenAi } from './openaiService';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const uploadDir = path.join(__dirname, '../uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, uploadDir);
  },
  filename: (_req, file, cb) => {
    const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1e9)}`;
    cb(null, `upload-${uniqueSuffix}${path.extname(file.originalname) || '.png'}`);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 25 * 1024 * 1024 }, // 25MB max
  fileFilter: (_req, file, cb) => {
    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/jpg'];
    if (allowed.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only JPEG and PNG images are supported.'));
    }
  }
});

app.get('/api/health', (_req: Request, res: Response) => {
  res.json({
    status: 'ok',
    service: 'RealShot AI Camera Backend',
    model: process.env.OPENAI_IMAGE_MODEL || 'dall-e-2',
    timestamp: new Date().toISOString()
  });
});

app.post('/api/enhance', upload.single('image'), async (req: Request, res: Response): Promise<void> => {
  const file = req.file;

  if (!file) {
    res.status(400).json({ error: 'No image provided. Please upload an image with field "image".' });
    return;
  }

  const apiKey = process.env.OPENAI_API_KEY || '';
  const model = process.env.OPENAI_IMAGE_MODEL || 'dall-e-2';

  if (!apiKey) {
    // Clean up temp file
    fs.unlink(file.path, () => {});
    res.status(500).json({
      error: 'OPENAI_API_KEY is not configured on the server. Please set OPENAI_API_KEY environment variable.'
    });
    return;
  }

  try {
    const { buffer, mimeType } = await enhanceImageWithOpenAi(file.path, apiKey, model);

    res.setHeader('Content-Type', mimeType);
    res.setHeader('Content-Disposition', 'inline; filename="realshot_enhanced.png"');
    res.send(buffer);
  } catch (error: any) {
    res.status(500).json({
      error: error.message || 'Failed to enhance image with OpenAI.'
    });
  } finally {
    // Clean up temporary uploaded file safely
    fs.unlink(file.path, (err) => {
      if (err) {
        // Ignore deletion error
      }
    });
  }
});

app.listen(PORT, () => {
  console.log(`[RealShot AI Camera] Backend running on port ${PORT}`);
});
