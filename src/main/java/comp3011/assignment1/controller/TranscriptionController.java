package comp3011.assignment1.controller;

import comp3011.assignment1.service.TranscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    // Constructor injection - Spring wires this automatically
    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam("audio") MultipartFile audioFile) {
        String result = transcriptionService.transcribe(audioFile);
        return ResponseEntity.ok(result);
    }
}