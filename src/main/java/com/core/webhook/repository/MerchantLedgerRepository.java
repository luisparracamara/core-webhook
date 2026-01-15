package com.core.webhook.repository;

import com.core.webhook.entity.MerchantLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MerchantLedgerRepository extends JpaRepository<MerchantLedgerEntity, Long> {

    @Query(value = """
                SELECT balance_after
                FROM merchant_ledger
                WHERE merchant_id = :merchantId
                ORDER BY merchant_ledger_id DESC
                LIMIT 1
                FOR UPDATE
            """, nativeQuery = true)
    Optional<BigDecimal> findLastBalanceForUpdate(Long merchantId);

}
