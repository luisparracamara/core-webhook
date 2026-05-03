package com.core.webhook.repository;

import com.core.webhook.constant.WebhookEventStatusEnum;
import com.core.webhook.entity.WebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEventEntity, Long> {

    @Query(value = """
            SELECT * FROM webhook_event
            WHERE status = 'FOR_REVIEW'
            AND retry_count < 10
            ORDER BY webhook_event_id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<WebhookEventEntity> findWebhookEvents(@Param("limit") int limit);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE webhook_event
            SET status = :status
            WHERE webhook_event_id = :id
            """, nativeQuery = true)
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE webhook_event
            SET status = 'PROCESSING'
            WHERE webhook_event_id = :id
            AND status = 'FOR_REVIEW'
            AND retry_count < 10
            """, nativeQuery = true)
    int markAsProcessing(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
            UPDATE webhook_event
            SET retry_count = retry_count + :retry,
                status = CASE
                    WHEN (retry_count + :retry) > 10 THEN :deadStatus
                    ELSE :status
                END
            WHERE webhook_event_id = :id
            """, nativeQuery = true)
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("retry") int retry, @Param("deadStatus") String deadStatus);

}
