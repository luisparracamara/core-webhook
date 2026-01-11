package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cashin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCashinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_cashin_id")
    private Long id;

    private String data;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "fk_pc_payment_id")
    private PaymentEntity paymentEntity;

}
