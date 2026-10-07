import fs from 'fs';

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

export async function enhanceImageWithOpenAi(
  imagePath: string,
  apiKey: string,
  modelName: string = 'dall-e-2'
): Promise<{ buffer: Buffer; mimeType: string }> {
  if (!apiKey || apiKey.trim() === '') {
    throw new Error('OPENAI_API_KEY is not set on the server.');
  }

  const imageBuffer = await fs.promises.readFile(imagePath);
  const blob = new Blob([imageBuffer], { type: 'image/png' });

  const formData = new FormData();
  formData.append('image', blob, 'image.png');
  formData.append('prompt', PROFESSIONAL_ENHANCEMENT_PROMPT);
  formData.append('model', modelName || 'dall-e-2');
  formData.append('n', '1');
  formData.append('size', '1024x1024');
  formData.append('response_format', 'b64_json');

  const response = await fetch('https://api.openai.com/v1/images/edits', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${apiKey.trim()}`
    },
    body: formData
  });

  if (!response.ok) {
    const errorText = await response.text();
    let message = `OpenAI API error (${response.status})`;
    try {
      const errJson = JSON.parse(errorText);
      message = errJson.error?.message || message;
    } catch {
      message = `${message}: ${errorText}`;
    }
    throw new Error(message);
  }

  const json = (await response.json()) as any;
  if (!json.data || json.data.length === 0) {
    throw new Error('OpenAI returned no image output.');
  }

  const b64Data = json.data[0]?.b64_json;
  if (b64Data) {
    const buffer = Buffer.from(b64Data, 'base64');
    return { buffer, mimeType: 'image/png' };
  }

  const url = json.data[0]?.url;
  if (url) {
    const imgResp = await fetch(url);
    const arrayBuffer = await imgResp.arrayBuffer();
    return { buffer: Buffer.from(arrayBuffer), mimeType: 'image/png' };
  }

  throw new Error('No valid image data found in OpenAI response.');
}
