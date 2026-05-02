package com.core.webhook.webhook.mercadopago.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderWebhookEvent {

    private String type;
    private String action;

    @JsonProperty("user_id")
    private Long userId;

    private DataNode data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DataNode {
        private String id;
        private String status;
        private String statusDetail;
        private String externalReference;
        private OrderMercadoPagoResponse.Transactions transactions;
    }
}
