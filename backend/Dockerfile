# Stage 1: Build TypeScript
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json tsconfig.json ./
RUN npm ci

COPY src ./src
RUN npm run build

# Stage 2: Production Runtime
FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production

COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY --from=builder /app/dist ./dist

# Create uploads directory for temporary multipart processing
RUN mkdir -p uploads && chown -R node:node /app

USER node

EXPOSE 3000

ENV PORT=3000
ENV OPENAI_IMAGE_MODEL=gpt-image-2

CMD ["node", "dist/server.js"]
