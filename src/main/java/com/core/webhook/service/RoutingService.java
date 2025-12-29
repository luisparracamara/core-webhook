package com.core.webhook.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RoutingService {

    void processWebhook(HttpServletRequest request, HttpServletResponse response, String payload, String provider);

}
