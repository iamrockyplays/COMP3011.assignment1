package comp3011.assignment1.service;

import org.springframework.web.multipart.MultipartFile;

public interface TranscriptionService {
    String transcribe(MultipartFile audioFile);
}