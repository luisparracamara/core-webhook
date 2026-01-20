package com.core.webhook.entity;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.constant.WebhookEventStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "webhook_event_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConnectorEnum connector;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "retry_count")
    private Integer retryCount;
    private String payload;
    private String headers;

    @Enumerated(EnumType.STRING)
    private WebhookEventStatusEnum status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
