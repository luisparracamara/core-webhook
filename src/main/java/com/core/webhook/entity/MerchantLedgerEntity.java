package com.core.webhook.entity;

import com.core.webhook.constant.PaymentFeeMerchantEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_ledger")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_ledger_id")
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PaymentFeeMerchantEnum operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_ml_payment_fee_id")
    private PaymentFeeEntity paymentFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_ml_merchant_payment_id")
    private MerchantPaymentEntity merchantPaymentEntity;

}
