package com.core.webhook.repository;

import com.core.webhook.entity.MerchantLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface MerchantLedgerRepository extends JpaRepository<MerchantLedgerEntity, Long> {

    @Query("SELECT COALESCE(SUM(ml.amount), 0) FROM MerchantLedgerEntity ml WHERE ml.merchantId = :merchantId")
    BigDecimal getBalanceByMerchantId(@Param("merchantId") Long merchantId);

}
