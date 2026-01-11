package com.core.webhook.webhook.mercadopago.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MercadoPagoWebhookEvent {

    private String topic;
    private String resource;

    private String type;
    private String action;

    private DataNode data;

    @Data
    public static class DataNode {
        private String id;
    }
}
