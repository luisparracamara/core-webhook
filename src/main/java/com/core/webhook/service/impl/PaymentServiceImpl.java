package com.core.webhook.service.impl;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.constant.PaymentFeeMerchantEnum;
import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.constant.WebhookEventStatusEnum;
import com.core.webhook.entity.MerchantLedgerEntity;
import com.core.webhook.entity.PaymentCashinEntity;
import com.core.webhook.entity.PaymentEntity;
import com.core.webhook.entity.WebhookEventEntity;
import com.core.webhook.repository.MerchantLedgerRepository;
import com.core.webhook.repository.PaymentCashinRepository;
import com.core.webhook.repository.PaymentRepository;
import com.core.webhook.repository.WebhookEventRepository;
import com.core.webhook.service.PaymentService;
import com.core.webhook.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentCashinRepository paymentCashinRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantLedgerRepository merchantLedgerRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final Utils utils;

    @Override
    @Transactional
    public void updatePaymentStatusByExternalReference(String externalReference, PaymentStatusEnum status, String paymentType) {
        PaymentCashinEntity paymentCashin = paymentCashinRepository.findByExternalReference(externalReference)
                .orElse(PaymentCashinEntity.builder().build());

        PaymentEntity payment = paymentCashin.getPaymentEntity();

        if (payment == null) {
            log.warn("[PaymentService] No payment found for externalReference={}", externalReference);
        } else if (!payment.getStatus().equals(PaymentStatusEnum.APPROVED)) {
            PaymentStatusEnum previousStatus = payment.getStatus();
            paymentRepository.save(updatePaymentStatus(payment, status, paymentType));
            log.info("[PaymentService] paymentId={} externalReference={} status {} -> {} paymentType={}",
                    payment.getId(), externalReference, previousStatus, status, paymentType);
            if (PaymentStatusEnum.APPROVED.equals(status)) {
                BigDecimal lastBalance = merchantLedgerRepository.findLastBalanceForUpdate(payment.getMerchant().getId())
                        .orElse(BigDecimal.ZERO);
                merchantLedgerRepository.save(buildMerchantLedger(payment, lastBalance));
                log.info("[PaymentService] Ledger created for paymentId={} merchantId={}", payment.getId(), payment.getMerchant().getId());
            }
        } else {
            log.info("[PaymentService] paymentId={} externalReference={} already APPROVED, skipping",
                    payment.getId(), externalReference);
        }
    }

    @Override
    public void createWebhookEvent(ConnectorEnum connector, String transactionId, String payload, Map<String, String> headers) {
        LocalDateTime now = LocalDateTime.now();
        WebhookEventEntity webhookEventEntity = WebhookEventEntity.builder()
                .connector(connector)
                .transactionId(transactionId)
                .payload(payload)
                .headers(utils.toJson(headers))
                .retryCount(0)
                .status(WebhookEventStatusEnum.FOR_REVIEW)
                .createdAt(now)
                .updatedAt(now)
                .build();
        webhookEventRepository.save(webhookEventEntity);
    }

    private PaymentEntity updatePaymentStatus(PaymentEntity payment, PaymentStatusEnum status, String paymentType) {
        payment.setStatus(status);
        payment.setPaymentType(paymentType);
        return payment;
    }

    private MerchantLedgerEntity buildMerchantLedger(PaymentEntity payment, BigDecimal lastBalance) {
        BigDecimal feeAmount = payment.getPaymentFeeEntity().getFeeAmount();
        lastBalance = lastBalance.add(feeAmount);
        return MerchantLedgerEntity.builder()
                .merchantId(payment.getMerchant().getId())
                .amount(feeAmount)
                .balanceAfter(lastBalance)
                .createdAt(LocalDateTime.now())
                .operation(PaymentFeeMerchantEnum.CREDIT)
                .paymentFee(payment.getPaymentFeeEntity())
                .merchantPaymentEntity(null)
                .build();
    }

}
