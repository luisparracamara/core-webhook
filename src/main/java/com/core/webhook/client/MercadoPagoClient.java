package com.core.webhook.client;

import com.core.webhook.webhook.mercadopago.model.PaymentMercadoPagoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "mercadoPago")
public interface MercadoPagoClient {

    @GetMapping("/v1/payments/{id}")
    PaymentMercadoPagoResponse getPayment(@PathVariable("id") Long paymentId,
                                          @RequestHeader("Authorization") String authorization,
                                          @RequestHeader("x-idempotency-key") String idempotencyKey);

}
