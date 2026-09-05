package comp3011.assignment1.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    @Value("${openai.api.key}")
    private String apiKey; // Read from env var - never log or print this

    public String transcribe(MultipartFile audioFile) {
        // TODO: send audioFile bytes to https://api.openai.com/v1/audio/transcriptions
        // using apiKey as Bearer token, model = gpt-4o-mini-transcribe
        // Consider: which HTTP client, async vs blocking, error handling
        throw new UnsupportedOperationException("Not implemented yet");
    }
}