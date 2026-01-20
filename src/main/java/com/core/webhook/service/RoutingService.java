package com.core.webhook.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface RoutingService {

    ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload, String provider);

}
