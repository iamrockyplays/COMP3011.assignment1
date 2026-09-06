package comp3011.assignment1.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!prod") // active whenever prod is NOT the active profile
public class MockTranscriptionService implements TranscriptionService {

    @Override
    public String transcribe(MultipartFile audioFile) {
        // No real API call
        return "This is a mock transcription. Received file: "
                + audioFile.getOriginalFilename()
                + " (" + audioFile.getSize() + " bytes)";
    }
}