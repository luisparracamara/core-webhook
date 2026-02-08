package com.core.webhook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "gateway")
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gateway {

    @Id
    @Column(name = "gateway_id")
    @ToString.Include
    private Long id;

    @ToString.Include
    private String name;

    @ToString.Include
    @Column(name = "connector_name")
    private String connectorName;

    @ToString.Include
    private Long merchantId;

    @OneToMany(mappedBy = "gateway", fetch = FetchType.LAZY)
    private List<GatewayMetadataEntity> metadata;

}