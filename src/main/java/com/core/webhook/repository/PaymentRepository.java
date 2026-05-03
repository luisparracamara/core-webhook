package com.core.webhook.repository;

import com.core.webhook.constant.PaymentStatusEnum;
import com.core.webhook.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PaymentEntity p SET p.status = :status, p.paymentType = :paymentType, p.updatedAt = :updatedAt " +
           "WHERE p.id = :paymentId AND p.status <> com.core.webhook.constant.PaymentStatusEnum.APPROVED")
    int updateStatusIfNotApproved(@Param("paymentId") Long paymentId,
                                  @Param("status") PaymentStatusEnum status,
                                  @Param("paymentType") String paymentType,
                                  @Param("updatedAt") LocalDateTime updatedAt);

}
