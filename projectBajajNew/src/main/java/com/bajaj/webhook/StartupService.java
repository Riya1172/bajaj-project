package com.bajaj.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class StartupService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bajaj.hiring.name}")
    private String name;
    @Value("${bajaj.hiring.regNo}")
    private String regNo;
    @Value("${bajaj.hiring.email}")
    private String email;
    @Value("${bajaj.hiring.generate.url}")
    private String generateUrl;

    public void executeStartupFlow() {
        try {
            // 1. Send POST to generateWebhook
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("regNo", regNo);
            body.put("email", email);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(generateUrl, request, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                System.err.println("Failed to generate webhook. Status: " + resp.getStatusCode());
                return;
            }
            Map<String,Object> respBody = resp.getBody();
            String webhookUrl = (String) respBody.get("webhook");
            String accessToken = (String) respBody.get("accessToken");
            // 2. Decide question 1 or 2 based on regNo
            boolean isOdd = determineOddFromRegNo(regNo);
            String assignedQuestion = isOdd ? "Question1" : "Question2";
            // 3. Get final SQL from file
            String finalQuery = tryReadFinalQueryFile().orElse("SELECT 'REPLACE_WITH_FINAL_SQL' AS finalQuery;");
            // 4. Submit finalQuery to webhook
            if (webhookUrl == null || accessToken == null) {
                System.err.println("Missing webhook or accessToken; aborting submission.");
                return;
            }
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken);
            Map<String,Object> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);
            HttpEntity<Map<String,Object>> submitRequest = new HttpEntity<>(submitBody, submitHeaders);
            ResponseEntity<String> submitResp = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);
            System.out.println("Submit response status: " + submitResp.getStatusCode());
            System.out.println("Submit response body: " + submitResp.getBody());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private boolean determineOddFromRegNo(String reg) {
        try {
            String digits = reg.replaceAll("\\D+", "");
            if (digits.length() >= 2) {
                String lastTwo = digits.substring(digits.length()-2);
                int val = Integer.parseInt(lastTwo);
                return (val % 2) == 1;
            } else if (digits.length() == 1) {
                int val = Integer.parseInt(digits);
                return (val % 2) == 1;
            }
        } catch (Exception ignored) {}
        return true;
    }
    private java.util.Optional<String> tryReadFinalQueryFile() {
        try {
            Path p = Path.of("src/main/resources/final-query.txt");
            if (!Files.exists(p)) return java.util.Optional.empty();
            String content = Files.readString(p).trim();
            if (content.isEmpty()) return java.util.Optional.empty();
            return java.util.Optional.of(content);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
