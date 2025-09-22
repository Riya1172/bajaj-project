
package com.bajaj.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service to handle the startup workflow: generate webhook, solve SQL, and submit the result.
 */
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

    /**
     * Main entry point for the startup flow.
     */
    public void executeStartupFlow() {
        try {
            String webhookUrl;
            String accessToken;
            // Step 1: Generate webhook
            Map<String, Object> webhookResponse = generateWebhook();
            if (webhookResponse == null) return;
            webhookUrl = (String) webhookResponse.get("webhook");
            accessToken = (String) webhookResponse.get("accessToken");
            if (webhookUrl == null || accessToken == null) {
                System.err.println("Missing webhook or accessToken; aborting submission.");
                return;
            }

            // Step 2: Read SQL query
            String finalQuery = readFinalQueryFile().orElse("SELECT 'REPLACE_WITH_FINAL_SQL' AS finalQuery;");

            // Step 3: Submit solution
            submitSolution(webhookUrl, accessToken, finalQuery);
        } catch (Exception ex) {
            System.err.println("[ERROR] Startup flow failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sends POST request to generate the webhook and get access token.
     */
    private Map<String, Object> generateWebhook() {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("regNo", regNo);
            body.put("email", email);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForEntity(generateUrl, request, (Class<Map<String, Object>>)(Class<?>)Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                System.err.println("Failed to generate webhook. Status: " + resp.getStatusCode());
                return null;
            }
            return resp.getBody();
        } catch (Exception ex) {
            System.err.println("[ERROR] Webhook generation failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Reads the final SQL query from the resource file.
     */
    private Optional<String> readFinalQueryFile() {
        try {
            Path p = Path.of("src/main/resources/final-query.txt");
            if (!Files.exists(p)) return Optional.empty();
            String content = Files.readString(p).trim();
            if (content.isEmpty()) return Optional.empty();
            return Optional.of(content);
        } catch (Exception e) {
            System.err.println("[ERROR] Could not read final-query.txt: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Submits the final SQL query to the webhook URL using the JWT token.
     */
    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        try {
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken);
            Map<String, Object> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);
            HttpEntity<Map<String, Object>> submitRequest = new HttpEntity<>(submitBody, submitHeaders);
            ResponseEntity<String> submitResp = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);
            System.out.println("[INFO] Submit response status: " + submitResp.getStatusCode());
            System.out.println("[INFO] Submit response body: " + submitResp.getBody());
        } catch (Exception ex) {
            System.err.println("[ERROR] Solution submission failed: " + ex.getMessage());
        }
    }
}
