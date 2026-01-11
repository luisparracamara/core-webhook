package com.core.webhook.repository;

import com.core.webhook.entity.PaymentCashinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentCashinRepository extends JpaRepository<PaymentCashinEntity, Long> {
    
    Optional<PaymentCashinEntity> findByExternalReference(String externalReference);
    
}
