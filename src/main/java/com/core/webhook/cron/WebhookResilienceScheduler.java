package com.core.webhook.cron;

import com.core.webhook.entity.WebhookEventEntity;
import com.core.webhook.repository.WebhookEventRepository;
import com.core.webhook.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookResilienceScheduler {

    private final WebhookEventRepository webhookEventRepository;
    private final SchedulerService schedulerService;

    @Value("${jobs.webhook-event.batch-size:50}")
    private int batchSize;

    @Scheduled(cron = "${jobs.webhook-event.cron}")
    @SchedulerLock(name = "webhookEventResilience", lockAtLeastFor = "30s", lockAtMostFor = "5m")
    public void webhookEventResilience() {
        List<WebhookEventEntity> events = webhookEventRepository.findWebhookEvents(batchSize);
        if (events.isEmpty()) return;
        log.info("[WebhookResilienceScheduler] Processing {} webhook events", events.size());
        events.forEach(schedulerService::processSingleEvent);
    }



}
