package com.core.webhook.service;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.constant.PaymentStatusEnum;

import java.util.Map;

public interface PaymentService {

    void updatePaymentStatusByExternalReference(String externalReference, PaymentStatusEnum status, String paymentType);

    void createWebhookEvent(ConnectorEnum connector, String transactionId, String payload, Map<String, String> headers);

}
