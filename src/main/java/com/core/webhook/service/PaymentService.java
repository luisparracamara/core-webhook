package com.core.webhook.service;

import com.core.webhook.constant.PaymentStatusEnum;

public interface PaymentService {

    void updatePaymentStatusByExternalReference(String externalReference, PaymentStatusEnum status, String paymentType);

}
