package com.core.webhook.service;

import com.core.webhook.constant.ConnectorEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ConnectorService {

    ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload);

    ConnectorEnum getConnector();

    ResponseEntity<Void> processTransaction(Map<String, String> headers, String payload);

}
