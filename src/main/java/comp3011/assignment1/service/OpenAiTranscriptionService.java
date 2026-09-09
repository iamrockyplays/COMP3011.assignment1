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
@Profile("!dev")   // active by default whenever 'dev' is NOT set, including on TITAN
public class OpenAiTranscriptionService implements TranscriptionService {

    @Value("${openai.api.key}")
    private String apiKey; // Read from env at runtime

    private static final String OPENAI_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final RestClient restClient;

    private final ServerStatsService statsService;

    public OpenAiTranscriptionService(ServerStatsService statsService) {
        this.statsService = statsService;
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

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri(OPENAI_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

            // Extract and record token usage, if present in the response.
            // VERIFY: check OpenAI's current docs for the exact field names -
            // this assumes a "usage" object with "input_tokens"/"output_tokens".
        if (response.get("usage") instanceof Map<?, ?> usage) {
            Object inputVal = usage.get("input_tokens");
            Object outputVal = usage.get("output_tokens");

            long inputTokens = (inputVal instanceof Number n) ? n.longValue() : 0L;
            long outputTokens = (outputVal instanceof Number n) ? n.longValue() : 0L;

            statsService.recordTokenUsage(inputTokens, outputTokens);
        }

        return (String) response.get("text");

    } catch (Exception e) {
        throw new TranscriptionException("Transcription failed: " + e.getMessage(), e);
    }
}}