import { useState, useRef, useEffect } from "react";

const WAVEFORM_BARS = 40;

export default function VoiceRecorder({ onTranscript, onStatus }) {
  const [recording, setRecording] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [bars, setBars] = useState(Array(WAVEFORM_BARS).fill(4));
  const mediaRecorderRef = useRef(null);
  const chunksRef = useRef([]);
  const analyserRef = useRef(null);
  const animFrameRef = useRef(null);
  const streamRef = useRef(null);

  // Animate waveform bars from AnalyserNode
  const animateWave = () => {
    if (!analyserRef.current) return;
    const data = new Uint8Array(analyserRef.current.frequencyBinCount);
    analyserRef.current.getByteFrequencyData(data);
    const step = Math.floor(data.length / WAVEFORM_BARS);
    const newBars = Array.from({ length: WAVEFORM_BARS }, (_, i) => {
      const val = data[i * step] || 0;
      return Math.max(4, (val / 255) * 60);
    });
    setBars(newBars);
    animFrameRef.current = requestAnimationFrame(animateWave);
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      // Set up audio analyser for waveform
      const ctx = new AudioContext();
      const source = ctx.createMediaStreamSource(stream);
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 256;
      source.connect(analyser);
      analyserRef.current = analyser;

      const mr = new MediaRecorder(stream, { mimeType: "audio/webm" });
      mediaRecorderRef.current = mr;
      chunksRef.current = [];

      mr.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data);
      };

      mr.onstop = async () => {
        cancelAnimationFrame(animFrameRef.current);
        setBars(Array(WAVEFORM_BARS).fill(4));
        setProcessing(true);
        onStatus("transcribing");

        const blob = new Blob(chunksRef.current, { type: "audio/webm" });

        // Import transcribeAudio lazily to avoid circular deps
        const { transcribeAudio } = await import("../services/api.js");
        try {
          const result = await transcribeAudio(blob);
          if (result.success && result.transcript) {
            onTranscript(result.transcript);
            onStatus("done");
          } else {
            onStatus("error", result.error || "Could not understand audio.");
          }
        } catch (err) {
          onStatus("error", "Backend connection failed. Please type your answer instead.");
        } finally {
          setProcessing(false);
        }

        // Clean up stream
        stream.getTracks().forEach((t) => t.stop());
      };

      mr.start(100);
      setRecording(true);
      onStatus("recording");
      animateWave();
    } catch (err) {
      onStatus("error", "Microphone access denied. Please allow microphone access and try again.");
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && recording) {
      mediaRecorderRef.current.stop();
      setRecording(false);
    }
  };

  useEffect(() => {
    return () => {
      cancelAnimationFrame(animFrameRef.current);
      streamRef.current?.getTracks().forEach((t) => t.stop());
    };
  }, []);

  return (
    <div className="recorder-card">
      <div className="waveform-container">
        {bars.map((h, i) => (
          <div
            key={i}
            className={`waveform-bar ${recording ? "active" : ""}`}
            style={{ height: `${recording ? h : 4}px`, animationDelay: `${i * 30}ms` }}
          />
        ))}
      </div>

      <div className="recorder-controls">
        {!recording && !processing && (
          <button className="btn-record" onClick={startRecording} id="btn-start-recording">
            <span className="btn-icon">🎙️</span>
            Start Recording
          </button>
        )}
        {recording && (
          <button className="btn-stop" onClick={stopRecording} id="btn-stop-recording">
            <span className="btn-icon pulse-dot">⏹</span>
            Stop Recording
          </button>
        )}
        {processing && (
          <div className="processing-indicator">
            <span className="spinner" />
            Transcribing your answer…
          </div>
        )}
      </div>

      {recording && (
        <p className="recording-hint">🔴 Recording — speak clearly, then click Stop</p>
      )}
    </div>
  );
}
