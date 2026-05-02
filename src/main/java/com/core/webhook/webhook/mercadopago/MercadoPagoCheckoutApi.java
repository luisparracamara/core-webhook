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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
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

    public MercadoPagoCheckoutApi(Utils utils, PaymentService paymentService,
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
        String type = request.getParameter("type");

        if (dataId == null || !"order".equalsIgnoreCase(type)) {
            log.info("[MercadoPagoCheckoutApi] Skipping: missing data.id or type is not 'order'");
            return ResponseEntity.ok().build();
        }

        if (validateSignature && !isValid(request, webhookSecret)) {
            log.warn("[MercadoPagoCheckoutApi] Invalid signature for data.id={}", dataId);
            return ResponseEntity.ok().build();
        }

        return processTransaction(utils.extractHeaders(request), payload);
    }

    @Override
    public ResponseEntity<Void> processTransaction(Map<String, String> headers, String payload) {
        OrderWebhookEvent event = utils.fromJson(payload, OrderWebhookEvent.class);

        if (!"order".equalsIgnoreCase(event.getType()) || event.getData() == null) {
            log.info("[MercadoPagoCheckoutApi] Skipping: event type is not 'order'");
            return ResponseEntity.ok().build();
        }

        OrderWebhookEvent.DataNode data = event.getData();
        String orderId = data.getId();
        log.debug("[MercadoPagoCheckoutApi] Order ID: {}, status: {}", orderId, data.getStatus());

        Optional<OrderMercadoPagoResponse.Payment> approvedPayment = findApprovedPayment(data);

        if (approvedPayment.isPresent()) {
            log.info("[MercadoPagoCheckoutApi] Order {} approved, updating payment", orderId);
            paymentService.updatePaymentStatusByExternalReference(
                    orderId,
                    PaymentStatusEnum.APPROVED,
                    approvedPayment.get().getPaymentTypeId()
            );
        } else if ("failed".equalsIgnoreCase(data.getStatus())) {
            log.info("[MercadoPagoCheckoutApi] Order {} failed, marking payment as CANCELLED", orderId);
            paymentService.updatePaymentStatusByExternalReference(
                    orderId,
                    PaymentStatusEnum.CANCELLED,
                    null
            );
        }

        log.info("[MercadoPagoCheckoutApi] Connector {} payment updated", getConnector());
        return ResponseEntity.ok().build();
    }

    private Optional<OrderMercadoPagoResponse.Payment> findApprovedPayment(OrderWebhookEvent.DataNode data) {
        if (data.getTransactions() == null) {
            return Optional.empty();
        }
        List<OrderMercadoPagoResponse.Payment> payments = data.getTransactions().getPayments();
        if (payments == null || payments.isEmpty()) {
            return Optional.empty();
        }
        return payments.stream()
                .filter(p -> "processed".equalsIgnoreCase(p.getStatus())
                        && "accredited".equalsIgnoreCase(p.getStatusDetail()))
                .findFirst();
    }

    private static boolean isValid(HttpServletRequest request, String secret) {
        String xSignature = request.getHeader("x-signature");
        String xRequestId = request.getHeader("x-request-id");
        String dataId = request.getParameter("data.id");

        if (xSignature == null || xRequestId == null || dataId == null) {
            return false;
        }

        Map<String, String> signatureParts = parseSignature(xSignature);
        String ts = signatureParts.get("ts");
        String originalHash = signatureParts.get("v1");

        if (ts == null || originalHash == null) {
            return false;
        }

        String manifest = String.format("id:%s;request-id:%s;ts:%s;",
                dataId.toLowerCase(), xRequestId, ts);

        log.debug("[MercadoPagoCheckoutApi] x-signature ={}", xSignature);
        log.debug("[MercadoPagoCheckoutApi] x-request-id={}", xRequestId);
        log.debug("[MercadoPagoCheckoutApi] data.id(raw)={}", dataId);
        log.debug("[MercadoPagoCheckoutApi] ts          ={}", ts);
        log.debug("[MercadoPagoCheckoutApi] v1          ={}", originalHash);
        log.debug("[MercadoPagoCheckoutApi] manifest    ={}", manifest);

        try {
            String generatedHash = hmacSha256(manifest, secret);
            log.debug("[MercadoPagoCheckoutApi] generatedHash={}", generatedHash);
            log.debug("[MercadoPagoCheckoutApi] originalHash ={}", originalHash);
            return MessageDigest.isEqual(
                    generatedHash.getBytes(StandardCharsets.UTF_8),
                    originalHash.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("[MercadoPagoCheckoutApi] Error computing HMAC: {}", e.getMessage());
            return false;
        }
    }

    private static Map<String, String> parseSignature(String xSignature) {
        Map<String, String> map = new HashMap<>();
        for (String part : xSignature.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private static String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
