package comp3011.assignment1.service;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@Profile("prod") // only active when the 'prod' profile is enabled
public class OpenAiTranscriptionService implements TranscriptionService {

    @Value("${openai.api.key}")
    private String apiKey; // Read from env at runtime

    private static final String OPENAI_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final RestClient restClient;

    public OpenAiTranscriptionService() {
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public String transcribe(MultipartFile audioFile) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioFile.getResource());
            body.add("model", "gpt-4o-mini-transcribe");


            Map<String, Object> response = restClient.post()
                    .uri(OPENAI_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("text");

        } catch (Exception e) {
            throw new RuntimeException("Transcription failed", e);
        }
    }
}