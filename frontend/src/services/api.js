import axios from "axios";

// In production (Render), set VITE_API_URL to your backend URL.
// Locally it falls back to http://localhost:8000
const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "",
  timeout: 30000,
});

/**
 * Send audio blob to backend for transcription.
 * @param {Blob} audioBlob  - MediaRecorder audio blob (webm/wav)
 * @returns {Promise<{transcript: string, success: boolean, error: string|null}>}
 */
export async function transcribeAudio(audioBlob) {
  const formData = new FormData();
  formData.append("file", audioBlob, "recording.wav");
  const { data } = await API.post("/transcribe", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

/**
 * Evaluate a transcript against a job description.
 * @param {string} transcript
 * @param {string} jobDescription
 * @param {string} question
 * @returns {Promise<EvaluationResult>}
 */
export async function evaluateAnswer(transcript, jobDescription, question) {
  const { data } = await API.post("/evaluate", {
    transcript,
    job_description: jobDescription,
    question,
  });
  return data;
}
