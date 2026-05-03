package com.core.webhook.webhook.mercadopago;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.service.ConnectorService;
import com.core.webhook.service.PaymentService;
import com.core.webhook.utils.Utils;
import com.core.webhook.webhook.mercadopago.model.OrderMercadoPagoResponse;
import com.core.webhook.webhook.mercadopago.model.OrderWebhookEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class MercadoPagoCheckoutApi implements ConnectorService {

    private final Utils utils;
    private final PaymentService paymentService;
    private final String webhookSecret;
    private final boolean validateSignature;

    public MercadoPagoCheckoutApi(Utils utils,
                                  PaymentService paymentService,
                                  @Value("${mercadopago.webhook.secret}") String webhookSecret,
                                  @Value("${mercadopago.webhook.validate-signature:true}") boolean validateSignature) {
        this.utils = utils;
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
        this.validateSignature = validateSignature;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_API;
    }

    @Override
    public ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload) {
        String dataId = request.getParameter("data.id");

        if (dataId == null) {
            log.info("[MercadoPagoCheckoutApi] Skipping: missing data.id");
            return ResponseEntity.ok().build();
        }

        if (validateSignature && !MercadoPagoSignatureValidator.isValid(request, webhookSecret)) {
            log.warn("[MercadoPagoCheckoutApi] Invalid signature for data.id={}", dataId);
            return ResponseEntity.ok().build();
        }

        return processTransaction(utils.extractHeaders(request), payload);
    }

    @Override
    public ResponseEntity<Void> processTransaction(Map<String, String> headers, String payload) {
        OrderWebhookEvent event = utils.fromJson(payload, OrderWebhookEvent.class);

        if (!"order".equalsIgnoreCase(event.getType()) || event.getData() == null) {
            log.info("[MercadoPagoCheckoutApi] Skipping: type={} is not 'order' or data is null", event.getType());
            return ResponseEntity.ok().build();
        }

        OrderWebhookEvent.DataNode data = event.getData();
        String orderId = data.getId();

        Optional<OrderMercadoPagoResponse.Payment> approvedPayment = findApprovedPayment(data);

        if (approvedPayment.isPresent()) {
            log.info("[MercadoPagoCheckoutApi] orderId={} status={} → APPROVED (paymentType={})",
                    orderId, data.getStatus(), approvedPayment.get().getPaymentTypeId());
            paymentService.updatePaymentStatusByExternalReference(
                    orderId,
                    PaymentStatusEnum.APPROVED,
                    approvedPayment.get().getPaymentTypeId()
            );
        } else if ("failed".equalsIgnoreCase(data.getStatus())) {
            log.info("[MercadoPagoCheckoutApi] orderId={} status={} statusDetail={} → CANCELLED",
                    orderId, data.getStatus(), data.getStatusDetail());
            paymentService.updatePaymentStatusByExternalReference(
                    orderId,
                    PaymentStatusEnum.CANCELLED,
                    null
            );
        } else {
            log.info("[MercadoPagoCheckoutApi] orderId={} status={} — no action taken, waiting for final status",
                    orderId, data.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    private Optional<OrderMercadoPagoResponse.Payment> findApprovedPayment(OrderWebhookEvent.DataNode data) {
        if (data.getTransactions() == null) {
            log.debug("[MercadoPagoCheckoutApi] orderId={} has no transactions yet", data.getId());
            return Optional.empty();
        }
        List<OrderMercadoPagoResponse.Payment> payments = data.getTransactions().getPayments();
        if (payments == null || payments.isEmpty()) {
            log.debug("[MercadoPagoCheckoutApi] orderId={} transactions list is empty", data.getId());
            return Optional.empty();
        }
        return payments.stream()
                .filter(p -> "processed".equalsIgnoreCase(p.getStatus())
                        && "accredited".equalsIgnoreCase(p.getStatusDetail()))
                .findFirst();
    }

}
