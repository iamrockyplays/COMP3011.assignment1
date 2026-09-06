const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const statusEl = document.getElementById('status');
const resultEl = document.getElementById('result');

let mediaRecorder;
let audioChunks = [];

startBtn.addEventListener('click', async () => {
    try {
        // Ask the browser for mic access. This triggers the permission
        // popup the first time. Throws if the user denies access.
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

        // MediaRecorder wraps the stream so we can capture it in chunks.
        mediaRecorder = new MediaRecorder(stream);
        audioChunks = []; // reset from any previous recording

        // Fired periodically (and at least once at the end) with a chunk
        // of recorded audio data.
        mediaRecorder.addEventListener('dataavailable', (event) => {
            audioChunks.push(event.data);
        });

        // Fired once recording fully stops - this is where we assemble
        // the final audio file and kick off the upload.
        mediaRecorder.addEventListener('stop', () => {
            const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
            uploadAudio(audioBlob);

            // Stop the mic track so the browser's "recording" indicator
            // (and the mic itself) actually turns off.
            stream.getTracks().forEach(track => track.stop());
        });

        mediaRecorder.start();
        statusEl.textContent = 'Recording...';
        startBtn.disabled = true;
        stopBtn.disabled = false;

    } catch (err) {
        // Most likely cause: user denied mic permission, or no mic present
        statusEl.textContent = 'Microphone access denied or unavailable.';
        console.error(err);
    }
});

stopBtn.addEventListener('click', () => {
    mediaRecorder.stop(); // triggers the 'stop' listener above
    statusEl.textContent = 'Processing...';
    startBtn.disabled = false;
    stopBtn.disabled = true;
});

async function uploadAudio(audioBlob) {
    // Backend expects multipart/form-data with a field named 'audio',
    // matching @RequestParam("audio") in TranscriptionController.
    const formData = new FormData();
    formData.append('audio', audioBlob, 'recording.webm');

    try {
        const response = await fetch('/api/transcribe', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`Server responded with ${response.status}`);
        }

        const text = await response.text();
        resultEl.textContent = text;
        statusEl.textContent = 'Idle';

    } catch (err) {
        statusEl.textContent = 'Transcription failed.';
        console.error(err);
    }
}