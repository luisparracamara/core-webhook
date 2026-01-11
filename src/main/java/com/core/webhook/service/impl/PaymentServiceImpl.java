package com.core.webhook.service.impl;

import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.entity.PaymentCashinEntity;
import com.core.webhook.entity.PaymentEntity;
import com.core.webhook.exception.NotFoundException;
import com.core.webhook.repository.PaymentCashinRepository;
import com.core.webhook.repository.PaymentRepository;
import com.core.webhook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentCashinRepository paymentCashinRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void updatePaymentStatusByExternalReference(String externalReference, PaymentStatusEnum status, String paymentType) {
        PaymentCashinEntity paymentCashin = paymentCashinRepository.findByExternalReference(externalReference)
                .orElse(PaymentCashinEntity.builder().build());

        PaymentEntity payment = paymentCashin.getPaymentEntity();
        if (payment == null) {
            log.debug("No payment was found with the external reference: {}", externalReference);
        } else {
            payment.setStatus(status);
            payment.setPaymentType(paymentType);
            paymentRepository.save(payment);
        }
    }

}
