package com.core.webhook.repository;

import com.core.webhook.constant.WebhookEventStatusEnum;
import com.core.webhook.entity.WebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEventEntity, Long> {

    @Query(value = """
            SELECT *
            FROM webhook_event
            WHERE status = 'FOR_REVIEW'
            AND retry_count < 10
            """, nativeQuery = true)
    List<WebhookEventEntity> findWebhookEvents();

    @Modifying
    @Query(value = """
            UPDATE WebhookEventEntity e
            SET e.status = :status
            WHERE e.id = :id
            """)
    void updateStatus(Long id, WebhookEventStatusEnum status);

    @Modifying
    @Query("""
            UPDATE WebhookEventEntity e
            SET e.status = 'PROCESSING'
            WHERE e.id = :id
            AND e.status = 'FOR_REVIEW'
            AND e.retryCount < 10
            """)
    int markAsProcessing(Long id);


    @Modifying
    @Query("""
    UPDATE WebhookEventEntity e
    SET e.retryCount = e.retryCount + :retry,
        e.status = CASE
            WHEN (e.retryCount + :retry) > 10 THEN :deadStatus
            ELSE :status
        END
    WHERE e.id = :id
    """)
    void updateStatus(Long id, WebhookEventStatusEnum status, int retry, WebhookEventStatusEnum deadStatus);

}
