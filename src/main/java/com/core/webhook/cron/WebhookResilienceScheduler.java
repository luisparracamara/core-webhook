package com.core.webhook.cron;

import com.core.webhook.entity.WebhookEventEntity;
import com.core.webhook.repository.WebhookEventRepository;
import com.core.webhook.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookResilienceScheduler {

    private final WebhookEventRepository webhookEventRepository;
    private final SchedulerService schedulerService;

    @Scheduled(cron = "${jobs.webhook-event.cron}")
    public void webhookEventResilience() {
        log.info("[WebhookResilienceScheduler] Resilience webhook event scheduler started");
        List<WebhookEventEntity> events = webhookEventRepository.findWebhookEvents();
        events.forEach(schedulerService::processSingleEvent);
    }



}
