package com.core.webhook.service.impl;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class RoutingServiceImpl implements RoutingService {

    private final PaymentRedirectorResolver paymentRedirectorResolver;

    public RoutingServiceImpl(PaymentRedirectorResolver paymentRedirectorResolver) {
        this.paymentRedirectorResolver = paymentRedirectorResolver;
    }

    @Override
    public void processWebhook(HttpServletRequest request, HttpServletResponse response, String payload, String provider) {
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(provider);
        routeWebhook(connector, request, response, payload);
    }

    private void routeWebhook(ConnectorEnum connector, HttpServletRequest request, HttpServletResponse response, String payload) {
        paymentRedirectorResolver
                .resolve(connector)
                .processWebhook(request, response, payload);
    }

}
