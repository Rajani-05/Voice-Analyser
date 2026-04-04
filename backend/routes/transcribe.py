"""Transcription route — POST /transcribe"""
import logging
from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from fastapi.responses import JSONResponse

from models.schemas import TranscribeResponse
from services.stt_service import transcribe_audio_file, transcribe_audio_base64

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/transcribe", response_model=TranscribeResponse)
async def transcribe_audio(file: UploadFile = File(...)):
    """
    Accept an uploaded audio file (WAV / WebM) and return transcribed text.
    The frontend sends the MediaRecorder blob directly.
    """
    try:
        audio_bytes = await file.read()
        if not audio_bytes:
            raise HTTPException(status_code=400, detail="Empty audio file received.")

        result = transcribe_audio_file(audio_bytes)
        return TranscribeResponse(**result)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Transcription endpoint error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/transcribe-b64", response_model=TranscribeResponse)
async def transcribe_base64(payload: dict):
    """Alternative endpoint accepting base64-encoded audio."""
    audio_b64 = payload.get("audio_base64", "")
    if not audio_b64:
        raise HTTPException(status_code=400, detail="audio_base64 field is required.")
    result = transcribe_audio_base64(audio_b64)
    return TranscribeResponse(**result)
