import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { enhanceRouter } from './routes/enhance';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all incoming client requests
app.use(cors());

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Health Check Endpoint
app.get('/health', (_req: Request, res: Response) => {
  res.status(200).json({ status: 'ok' });
});

// Primary Photographic Enhancement Route
app.use('/api/enhance', enhanceRouter);

// Global Error Handling Middleware
app.use((err: any, _req: Request, res: Response, _next: NextFunction) => {
  if (err && err.name === 'MulterError') {
    if (err.code === 'LIMIT_FILE_SIZE') {
      res.status(413).json({ error: 'Image file too large. Maximum supported size is 25MB.' });
      return;
    }
    res.status(400).json({ error: `Upload error: ${err.message}` });
    return;
  }

  if (err) {
    res.status(400).json({ error: err.message || 'Bad Request' });
    return;
  }

  res.status(500).json({ error: 'Internal Server Error' });
});

app.listen(PORT, () => {
  console.log(`[RealShot AI Camera Backend] Server running on port ${PORT}`);
  console.log(`[RealShot AI Camera Backend] Model: ${process.env.OPENAI_IMAGE_MODEL || 'gpt-image-2'}`);
});

export default app;
