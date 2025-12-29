package com.core.webhook.service;

import com.core.webhook.constant.ConnectorEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ConnectorService {

    void processWebhook(HttpServletRequest request, HttpServletResponse response, String payload);

    ConnectorEnum getConnector();

}
