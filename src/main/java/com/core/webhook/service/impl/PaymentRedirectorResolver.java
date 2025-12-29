package com.core.webhook.service.impl;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.exception.NotFoundException;
import com.core.webhook.service.ConnectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentRedirectorResolver {

    private final Map<ConnectorEnum, ConnectorService> redirectors;

    public PaymentRedirectorResolver(List<ConnectorService> beans) {
        log.debug("[PaymentRedirectorResolver] List of PaymentRedirector {}", beans);
        this.redirectors = beans.stream()
                .collect(Collectors.toMap(
                        ConnectorService::getConnector,
                        Function.identity()
                ));

        log.debug("[PaymentRedirectorResolver] Map of PaymentRedirector {}", redirectors);
    }

    public ConnectorService resolve(ConnectorEnum connector) {
        ConnectorService redirector = redirectors.get(connector);

        if (redirector == null) {
            throw new NotFoundException("No redirector found for connector: " + connector);
        }

        return redirector;
    }

}
