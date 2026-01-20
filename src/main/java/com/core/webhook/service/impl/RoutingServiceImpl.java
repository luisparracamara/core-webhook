package com.core.webhook.service.impl;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoutingServiceImpl implements RoutingService {

    private final PaymentRedirectorResolver paymentRedirectorResolver;

    public RoutingServiceImpl(PaymentRedirectorResolver paymentRedirectorResolver) {
        this.paymentRedirectorResolver = paymentRedirectorResolver;
    }

    @Override
    public ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload, String provider) {
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(provider);
        return routeWebhook(connector, request, payload);
    }

    private ResponseEntity<Void> routeWebhook(ConnectorEnum connector, HttpServletRequest request, String payload) {
        return paymentRedirectorResolver
                .resolve(connector)
                .processWebhook(request, payload);
    }

}
