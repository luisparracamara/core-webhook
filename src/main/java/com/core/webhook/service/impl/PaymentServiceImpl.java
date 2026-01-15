package com.core.webhook.service.impl;

import com.core.webhook.constant.PaymentFeeMerchantEnum;
import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.entity.MerchantLedgerEntity;
import com.core.webhook.entity.PaymentCashinEntity;
import com.core.webhook.entity.PaymentEntity;
import com.core.webhook.repository.MerchantLedgerRepository;
import com.core.webhook.repository.PaymentCashinRepository;
import com.core.webhook.repository.PaymentRepository;
import com.core.webhook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentCashinRepository paymentCashinRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantLedgerRepository merchantLedgerRepository;

    @Override
    @Transactional
    public void updatePaymentStatusByExternalReference(String externalReference, PaymentStatusEnum status, String paymentType) {
        PaymentCashinEntity paymentCashin = paymentCashinRepository.findByExternalReference(externalReference)
                .orElse(PaymentCashinEntity.builder().build());

        PaymentEntity payment = paymentCashin.getPaymentEntity();

        if (payment == null) {
            log.debug("[PaymentService] No payment was found with external reference: {}", externalReference);
        } else if (!payment.getStatus().equals(PaymentStatusEnum.APPROVED)) {
            paymentRepository.save(updatePaymentStatus(payment, status, paymentType));
            BigDecimal lastBalance = merchantLedgerRepository.findLastBalanceForUpdate(payment.getMerchant().getId())
                    .orElse(BigDecimal.ZERO);
            merchantLedgerRepository.save(buildMerchantLedger(payment, lastBalance));
            log.info("[PaymentService] Payment status updated and ledger created");
        }
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
