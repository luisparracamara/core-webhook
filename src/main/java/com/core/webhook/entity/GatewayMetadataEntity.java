package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gateway_metadata")
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gateway_metadata_id")
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_gm_gateway_id", nullable = false)
    private Gateway gateway;

    @ToString.Include
    @Column(name = "key")
    private String metaKey;

    @ToString.Include
    @Column(name = "value")
    private String metaValue;

}
