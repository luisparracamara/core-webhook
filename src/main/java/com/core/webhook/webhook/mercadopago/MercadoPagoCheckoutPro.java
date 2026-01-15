package com.core.webhook.webhook.mercadopago;

import com.core.webhook.client.MercadoPagoClient;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MercadoPagoCheckoutPro implements ConnectorService {

    private final Utils utils;

    private final MercadoPagoClient mercadoPagoClient;

    private final MetadataService metadataService;

    private final PaymentService paymentService;

    public MercadoPagoCheckoutPro(Utils utils, MercadoPagoClient mercadoPagoClient, MetadataService metadataService, PaymentService paymentService) {
        this.utils = utils;
        this.mercadoPagoClient = mercadoPagoClient;
        this.metadataService = metadataService;
        this.paymentService = paymentService;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public void processWebhook(HttpServletRequest request, HttpServletResponse response, String payload) {

        if (request.getParameter("data.id") == null) {
            log.info("Skipping signature validation (no data.id)");
            return;
        }

        log.debug("[MercadoPagoCheckoutPro] Request: {}", request);
        log.debug("[MercadoPagoCheckoutPro] Response: {}", response);
        log.debug("[MercadoPagoCheckoutPro] Payload: {}", payload);

        MercadoPagoWebhookEvent mercadoPagoWebhookEvent = utils.fromJson(payload, MercadoPagoWebhookEvent.class);
        Optional<String> paymentId = extractPaymentId(mercadoPagoWebhookEvent);

        if (paymentId.isPresent()) {
            log.debug("Payment ID: {}", paymentId.get());

            //obtener metadata para el accestoken
            Map<String, String> gatewayMetadata = metadataService.getGatewayMetadata(getConnector().getName());
            log.debug("Gateway Metadata: {}", gatewayMetadata);

            String accessToken = "Bearer " +gatewayMetadata.get(GatewayMetadataEnum.ACCESS_TOKEN.name());
            PaymentMercadoPagoResponse payment = mercadoPagoClient.getPayment(Long.valueOf(paymentId.get()), accessToken,
                    UUID.randomUUID().toString());

            if(payment.getStatus().equalsIgnoreCase("approved") &&
                    payment.getStatusDetail().equalsIgnoreCase("accredited")) {
                log.info("Payment: {}", payment.getStatus());
                log.info("Payment: {}", payment.getStatusDetail());
                //visa
                log.info("Payment: {}", payment.getPaymentMethodId());
                //credit_card
                log.info("Payment: {}", payment.getPaymentTypeId());

                //hacer el cambio de estatus en bd y tambien guardar el getPaymentMethodId y el getPaymentTypeIdPaymentCashinEntity
                paymentService.updatePaymentStatusByExternalReference(payment.getExternalReference(),
                        PaymentStatusEnum.APPROVED, payment.getPaymentTypeId());
                log.info("[MercadoPagoCheckoutPro] Connector {} Payment updated", getConnector());
            }

        }

    }


    private Optional<String> extractPaymentId(MercadoPagoWebhookEvent mercadoPagoWebhookEvent) {
        if ("payment".equals(mercadoPagoWebhookEvent.getType()) && mercadoPagoWebhookEvent.getData() != null) {
            return Optional.of(mercadoPagoWebhookEvent.getData().getId());
        }
        return Optional.empty();
    }

    private static String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(keySpec);

        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(rawHmac);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static Map<String, String> parseSignature(String xSignature) {
        Map<String, String> map = new HashMap<>();

        String[] parts = xSignature.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }

    //esto para checkout api

    public static boolean isValid(HttpServletRequest request, String secret) {

        String xSignature = request.getHeader("x-signature");
        String xRequestId = request.getHeader("x-request-id");
        String dataId = request.getParameter("data.id");

        if (xSignature == null || xRequestId == null || dataId == null) {
            return false;
        }

        // 1️⃣ Parse x-signature
        Map<String, String> signatureParts = parseSignature(xSignature);

        String ts = signatureParts.get("ts");
        String originalHash = signatureParts.get("v1");

        if (ts == null || originalHash == null) {
            return false;
        }

        // 2️⃣ Build manifest
        String manifest = String.format(
                "id:%s;request-id:%s;ts:%s;",
                dataId.toLowerCase(),
                xRequestId,
                ts
        );

        log.debug("x-signature      = {}", xSignature);
        log.debug("x-request-id     = {}", xRequestId);
        log.debug("data.id (query)  = {}", dataId);
        log.debug("ts               = {}", ts);
        log.debug("manifest   = {}", manifest);

        // 3️⃣ Generate HMAC
        String generatedHash = null;
        try {
            generatedHash = hmacSha256(manifest, secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("generatedHash: " + generatedHash);
        System.out.println("originalHash: " + originalHash);

        // 4️⃣ Constant-time comparison (seguridad)
        return MessageDigest.isEqual(
                generatedHash.getBytes(StandardCharsets.UTF_8),
                originalHash.getBytes(StandardCharsets.UTF_8)
        );
    }

}
