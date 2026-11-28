package com.pantteon.canal_etico.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="app.notifications.transport",havingValue="gmail-api")
public class GmailApiConfiguration {
 @Bean
 GmailApiMailSender gmailApiMailSender(ObjectMapper json,
   @Value("${app.notifications.gmail.client-id:}") String clientId,
   @Value("${app.notifications.gmail.client-secret:}") String clientSecret,
   @Value("${app.notifications.gmail.refresh-token:}") String refreshToken,
   @Value("${app.notifications.gmail.mailbox:}") String mailbox){
  return new GmailApiMailSender(clientId,clientSecret,refreshToken,mailbox,json);
 }
}
