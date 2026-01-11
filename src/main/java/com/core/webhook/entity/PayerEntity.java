package com.core.webhook.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payer_id")
    @JsonIgnore
    private Long id;

    private String document;

    private String documentType;

    private String firstName;

    private String lastName;

    private String email;

}
