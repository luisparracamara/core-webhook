package com.core.webhook.webhook.mercadopago.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentMercadoPagoResponse {

    private Long id;
    private String status;
    private String statusDetail;
    private String paymentMethodId;
    private String paymentTypeId;
    private BigDecimal transactionAmount;
    private OffsetDateTime dateApproved;
    private String externalReference;

}
