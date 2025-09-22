package com.bajaj.webhook;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    /**
     * Health check endpoint to verify the service is running.
     */
    @GetMapping("/status")
    public String status() {
        return "Service is running";
    }
}
