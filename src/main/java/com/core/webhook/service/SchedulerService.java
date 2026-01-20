package com.core.webhook.service;

import com.core.webhook.entity.WebhookEventEntity;

public interface SchedulerService {

    void processSingleEvent(WebhookEventEntity event);

}
