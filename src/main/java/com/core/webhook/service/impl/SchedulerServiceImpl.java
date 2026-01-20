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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(WebhookEventEntity event) {
        try {
            int claimed = webhookEventRepository.markAsProcessing(event.getId());
            if (claimed != 0) {
                log.info("[WebhookResilienceScheduler] Processing webhook event ID: {}", event.getId());
                paymentRedirectorResolver.resolve(event.getConnector()).processTransaction(utils.jsonToMap(event.getHeaders()),
                        event.getPayload());
                webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.COMPLETED);
            }
        } catch (CallNotPermittedException ex) {
            webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.FOR_REVIEW, 0, WebhookEventStatusEnum.DEAD);
        } catch (Exception ex) {
            log.error("[WebhookResilienceScheduler] Event {} failed. Incrementing retry count and checking if it should be DEAD",
                    event.getId(), ex);
            webhookEventRepository.updateStatus(event.getId(), WebhookEventStatusEnum.FOR_REVIEW, 1, WebhookEventStatusEnum.DEAD);
        }
    }
}
