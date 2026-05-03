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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        PaymentEntity payment = paymentCashinRepository.findByExternalReference(externalReference)
                .map(PaymentCashinEntity::getPaymentEntity)
                .orElse(null);

        if (payment == null) {
            log.warn("[PaymentService] No payment_cashin found for externalReference={} — skipping update", externalReference);
            return;
        }

        int updated = paymentRepository.updateStatusIfNotApproved(payment.getId(), status, paymentType, LocalDateTime.now());

        if (updated == 0) {
            log.info("[PaymentService] paymentId={} externalReference={} already APPROVED — skipping duplicate update",
                    payment.getId(), externalReference);
            return;
        }

        log.info("[PaymentService] paymentId={} externalReference={} status → {} paymentType={}",
                payment.getId(), externalReference, status, paymentType);

        if (PaymentStatusEnum.APPROVED.equals(status)) {
            creditMerchantLedger(payment);
        }
    }

    @Override
    public void createWebhookEvent(ConnectorEnum connector, String transactionId, String payload, Map<String, String> headers) {
        log.info("[PaymentService] Queuing webhook event for retry — connector={} transactionId={}", connector, transactionId);
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
        log.debug("[PaymentService] WebhookEvent saved id={} connector={} transactionId={}",
                webhookEventEntity.getId(), connector, transactionId);
    }

    private void creditMerchantLedger(PaymentEntity payment) {
        merchantLedgerRepository.save(MerchantLedgerEntity.builder()
                .merchantId(payment.getMerchant().getId())
                .amount(payment.getPaymentFeeEntity().getFeeAmount())
                .createdAt(LocalDateTime.now())
                .operation(PaymentFeeMerchantEnum.CREDIT)
                .paymentFee(payment.getPaymentFeeEntity())
                .merchantPaymentEntity(null)
                .build());
        log.info("[PaymentService] Ledger credited for paymentId={} merchantId={} amount={}",
                payment.getId(), payment.getMerchant().getId(), payment.getPaymentFeeEntity().getFeeAmount());
    }

}
