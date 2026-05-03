package com.core.webhook.service.impl;

import com.core.webhook.constant.WebhookEventStatusEnum;
import com.core.webhook.entity.WebhookEventEntity;
import com.core.webhook.repository.WebhookEventRepository;
import com.core.webhook.service.SchedulerService;
import com.core.webhook.utils.Utils;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final PaymentRedirectorResolver paymentRedirectorResolver;
    private final WebhookEventRepository webhookEventRepository;
    private final Utils utils;

    @Override
    public void processSingleEvent(WebhookEventEntity event) {
        int claimed = webhookEventRepository.markAsProcessing(event.getId());
        if (claimed == 0) {
            log.debug("[SchedulerService] Event id={} already claimed by another instance — skipping", event.getId());
            return;
        }

        log.info("[SchedulerService] Processing event id={} connector={} transactionId={} retryCount={}",
                event.getId(), event.getConnector(), event.getTransactionId(), event.getRetryCount());
        try {
            processInTransaction(event);
            webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.COMPLETED.name());
            log.info("[SchedulerService] Event id={} completed successfully", event.getId());
        } catch (CallNotPermittedException ex) {
            log.warn("[SchedulerService] Event id={} circuit breaker OPEN — returning to FOR_REVIEW without incrementing retry: {}",
                    event.getId(), ex.getMessage(), ex);
            webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.FOR_REVIEW.name(), 0, WebhookEventStatusEnum.DEAD.name());
        } catch (Exception ex) {
            log.error("[SchedulerService] Event id={} connector={} transactionId={} failed on retry #{} — {}",
                    event.getId(), event.getConnector(), event.getTransactionId(), event.getRetryCount(), ex.getMessage(), ex);
            webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.FOR_REVIEW.name(), 1, WebhookEventStatusEnum.DEAD.name());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processInTransaction(WebhookEventEntity event) {
        log.debug("[SchedulerService] Executing transaction for event id={}", event.getId());
        paymentRedirectorResolver.resolve(event.getConnector())
                .processTransaction(utils.jsonToMap(event.getHeaders()), event.getPayload());
    }

}
