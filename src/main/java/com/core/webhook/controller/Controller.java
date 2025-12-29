package com.core.webhook.controller;

import com.core.webhook.service.RoutingService;
import com.core.webhook.utils.Utils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
@Slf4j
public class Controller {

    private final RoutingService routingService;

    private final Utils utils;

    public Controller(RoutingService routingService, Utils utils) {
        this.routingService = routingService;
        this.utils = utils;
    }

    @PostMapping(path = "/v1/webhook/{provider}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE} )
    public void webhook(HttpServletRequest request, HttpServletResponse response, @PathVariable @NotNull String provider,
                        @RequestBody @Nullable String payload) {
        log.debug("Request CONTROLLER provider: {} payload: {}", provider, payload);
        routingService.processWebhook(request, response, payload, provider);
    }



}
