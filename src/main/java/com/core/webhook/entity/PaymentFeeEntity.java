package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_fee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_fee_id")
    private Long id;

    @Column(name = "gross_amount")
    private BigDecimal grossAmount;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    private String currency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_mf_merchant_id", nullable = false)
    private Merchant merchant;

}
