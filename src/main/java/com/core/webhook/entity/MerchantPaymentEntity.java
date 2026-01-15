package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_payment_id")
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private String reference;

}
