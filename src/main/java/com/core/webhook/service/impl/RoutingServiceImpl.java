package com.core.webhook.service.impl;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final PaymentRedirectorResolver paymentRedirectorResolver;

    @Override
    public ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload, String provider) {
        log.info("[RoutingService] Incoming webhook provider={} remoteAddr={}", provider, request.getRemoteAddr());
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(provider);
        return paymentRedirectorResolver
                .resolve(connector)
                .processWebhook(request, payload);
    }

}
