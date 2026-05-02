package com.core.webhook.webhook.mercadopago.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderMercadoPagoResponse {

    private String id;
    private String status;
    private String statusDetail;
    private String externalReference;
    private Transactions transactions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transactions {
        private List<Payment> payments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Payment {
        private String status;
        private String statusDetail;
        private PaymentMethod paymentMethod;

        public String getPaymentMethodId() {
            return paymentMethod != null ? paymentMethod.getId() : null;
        }

        public String getPaymentTypeId() {
            return paymentMethod != null ? paymentMethod.getType() : null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentMethod {
        private String id;
        private String type;
    }
}
