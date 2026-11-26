package com.pantteon.canal_etico.infra;
import com.pantteon.canal_etico.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
@Configuration @EnableScheduling @RequiredArgsConstructor
@ConditionalOnProperty(name="app.notifications.enabled",havingValue="true",matchIfMissing=true)
public class NotificationScheduler {
 private final NotificationDispatcher dispatcher;
 @Scheduled(fixedDelayString="${app.notifications.poll-ms:30000}",initialDelayString="${app.notifications.poll-ms:30000}")
 public void dispatch(){dispatcher.procesarPendientes();}
}

