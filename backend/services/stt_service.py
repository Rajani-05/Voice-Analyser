"""Speech-to-Text via Google Web Speech API (no heavy local model)."""
import base64
import io
import logging
import speech_recognition as sr

logger = logging.getLogger(__name__)
_recognizer = sr.Recognizer()


def _do_recognize(audio_data: sr.AudioData) -> dict:
    try:
        transcript = _recognizer.recognize_google(audio_data)
        logger.info("Transcription OK: %.60s", transcript)
        return {"transcript": transcript, "success": True, "error": None}
    except sr.UnknownValueError:
        return {"transcript": "", "success": False, "error": "Could not understand audio. Please speak clearly."}
    except sr.RequestError as e:
        return {"transcript": "", "success": False, "error": f"Speech service unavailable: {e}"}
    except Exception as e:
        return {"transcript": "", "success": False, "error": f"Audio processing error: {e}"}


def transcribe_audio_bytes(audio_bytes: bytes) -> dict:
    """Transcribe raw audio bytes (WAV / WebM)."""
    try:
        with sr.AudioFile(io.BytesIO(audio_bytes)) as src:
            _recognizer.adjust_for_ambient_noise(src, duration=0.3)
            audio_data = _recognizer.record(src)
        return _do_recognize(audio_data)
    except Exception as e:
        logger.error("STT error: %s", e)
        return {"transcript": "", "success": False, "error": f"Audio processing error: {e}"}


def transcribe_audio_base64(audio_b64: str) -> dict:
    """Transcribe base64-encoded audio."""
    try:
        return transcribe_audio_bytes(base64.b64decode(audio_b64))
    except Exception as e:
        return {"transcript": "", "success": False, "error": f"Base64 decode error: {e}"}


# Kept for backward-compat (routes/transcribe.py calls this)
transcribe_audio_file = transcribe_audio_bytes
