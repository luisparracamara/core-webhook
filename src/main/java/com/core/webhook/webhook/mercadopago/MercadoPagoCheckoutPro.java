package com.core.webhook.webhook.mercadopago;

import com.core.webhook.client.MercadoPagoApiGateway;
import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.constant.GatewayMetadataEnum;
import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.service.ConnectorService;
import com.core.webhook.service.MetadataService;
import com.core.webhook.service.PaymentService;
import com.core.webhook.utils.Utils;
import com.core.webhook.webhook.mercadopago.model.MercadoPagoWebhookEvent;
import com.core.webhook.webhook.mercadopago.model.PaymentMercadoPagoResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MercadoPagoCheckoutPro implements ConnectorService {

    private final Utils utils;
    private final MercadoPagoApiGateway mercadoPagoApiGateway;
    private final MetadataService metadataService;
    private final PaymentService paymentService;
    private final String webhookSecret;
    private final boolean validateSignature;

    public MercadoPagoCheckoutPro(Utils utils,
                                  MercadoPagoApiGateway mercadoPagoApiGateway,
                                  MetadataService metadataService,
                                  PaymentService paymentService,
                                  @Value("${mercadopago.webhook.secret}") String webhookSecret,
                                  @Value("${mercadopago.webhook.validate-signature:true}") boolean validateSignature) {
        this.utils = utils;
        this.mercadoPagoApiGateway = mercadoPagoApiGateway;
        this.metadataService = metadataService;
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
        this.validateSignature = validateSignature;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public ResponseEntity<Void> processWebhook(HttpServletRequest request, String payload) {
        String dataId = request.getParameter("data.id");
        if (dataId == null) {
            log.info("[MercadoPagoCheckoutPro] Skipping: no data.id");
            return ResponseEntity.ok().build();
        }

        if (validateSignature && !MercadoPagoSignatureValidator.isValid(request, webhookSecret)) {
            log.warn("[MercadoPagoCheckoutPro] Invalid signature for data.id={}", dataId);
            return ResponseEntity.ok().build();
        }

        return processTransaction(utils.extractHeaders(request), payload);
    }

    @Override
    public ResponseEntity<Void> processTransaction(Map<String, String> headers, String payload) {
        MercadoPagoWebhookEvent event = utils.fromJson(payload, MercadoPagoWebhookEvent.class);
        Optional<String> paymentId = extractPaymentId(event);

        if (paymentId.isEmpty()) {
            log.info("[MercadoPagoCheckoutPro] Skipping: type={} is not 'payment' or data is null", event.getType());
            return ResponseEntity.ok().build();
        }

        log.info("[MercadoPagoCheckoutPro] Processing paymentId={} userId={}", paymentId.get(), event.getUserId());

        String userId = String.valueOf(event.getUserId());
        String metadataConnector = ConnectorEnum.MERCADO_PAGO_CHECKOUT_API.getName();
        Map<String, String> gatewayMetadata = metadataService.retrieveGatewayMetadataByUserId(metadataConnector, userId);

        if (gatewayMetadata.isEmpty()) {
            log.error("[MercadoPagoCheckoutPro] No gateway metadata found for connector={} userId={} — cannot process paymentId={}",
                    metadataConnector, userId, paymentId.get());
            return ResponseEntity.ok().build();
        }

        String accessToken = "Bearer " + gatewayMetadata.get(GatewayMetadataEnum.ACCESS_TOKEN.name());

        Optional<PaymentMercadoPagoResponse> paymentResponse = mercadoPagoApiGateway.getPayment(
                Long.valueOf(paymentId.get()), accessToken, UUID.randomUUID().toString());

        if (paymentResponse.isEmpty()) {
            log.warn("[MercadoPagoCheckoutPro] Could not fetch payment paymentId={} — queuing for retry", paymentId.get());
            paymentService.createWebhookEvent(getConnector(), paymentId.get(), payload, headers);
            return ResponseEntity.ok().build();
        }

        PaymentMercadoPagoResponse payment = paymentResponse.get();
        log.info("[MercadoPagoCheckoutPro] paymentId={} externalRef={} status={} statusDetail={}",
                paymentId.get(), payment.getExternalReference(), payment.getStatus(), payment.getStatusDetail());

        if ("approved".equalsIgnoreCase(payment.getStatus()) &&
                "accredited".equalsIgnoreCase(payment.getStatusDetail())) {
            paymentService.updatePaymentStatusByExternalReference(
                    payment.getExternalReference(), PaymentStatusEnum.APPROVED, payment.getPaymentTypeId());
        } else if ("rejected".equalsIgnoreCase(payment.getStatus()) ||
                   "cancelled".equalsIgnoreCase(payment.getStatus())) {
            paymentService.updatePaymentStatusByExternalReference(
                    payment.getExternalReference(), PaymentStatusEnum.CANCELLED, null);
        } else {
            log.info("[MercadoPagoCheckoutPro] paymentId={} status={} — no action taken, waiting for final status",
                    paymentId.get(), payment.getStatus());
        }

        return ResponseEntity.ok().build();
    }

    private Optional<String> extractPaymentId(MercadoPagoWebhookEvent event) {
        if ("payment".equals(event.getType()) && event.getData() != null) {
            return Optional.of(event.getData().getId());
        }
        return Optional.empty();
    }

}
