package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "business_unit")
    private String businessUnit;

    @Column(name = "status")
    private String status;

    @Column(name = "logo")
    private String logo;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime creationDate;

}
