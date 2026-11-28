package com.pantteon.canal_etico.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Sends the existing MIME notification over HTTPS. Never connects to SMTP. */
public final class GmailApiMailSender extends JavaMailSenderImpl {
 private final String clientId, clientSecret, refreshToken;
 private final ObjectMapper json;
 private final HttpClient http;
 private final Clock clock;
 private final URI tokenEndpoint, sendEndpoint;
 private String accessToken;
 private Instant tokenExpiry = Instant.EPOCH;

 public GmailApiMailSender(String clientId,String clientSecret,String refreshToken,String mailbox,ObjectMapper json){
  this(clientId,clientSecret,refreshToken,mailbox,json,
   HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER).build(),
   Clock.systemUTC(),URI.create("https://oauth2.googleapis.com/token"),
   URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"));
 }

 // Endpoint injection is package-private and used only by loopback tests.
 GmailApiMailSender(String clientId,String clientSecret,String refreshToken,String mailbox,ObjectMapper json,
   HttpClient http,Clock clock,URI tokenEndpoint,URI sendEndpoint){
  this.clientId=clientId.trim();this.clientSecret=clientSecret.trim();this.refreshToken=refreshToken.trim();
  this.json=json;this.http=http;this.clock=clock;this.tokenEndpoint=tokenEndpoint;this.sendEndpoint=sendEndpoint;
  setUsername(mailbox.trim());
 }

 public String configurationProblem(){
  if(clientId.isBlank()||clientSecret.isBlank())return "GMAIL_API_CLIENT_MISSING";
  if(refreshToken.isBlank())return "GMAIL_API_AUTHORIZATION_MISSING";
  if(getUsername().isBlank())return "GMAIL_API_MAILBOX_MISSING";
  return null;
 }

 @Override protected synchronized void doSend(MimeMessage[] messages,Object[] originalMessages){
  if(configurationProblem()!=null)throw new DeliveryException(configurationProblem());
  for(var message:messages){
   try{
    String bearer=token();
    var bytes=new ByteArrayOutputStream();message.writeTo(bytes);
    var payload=json.writeValueAsString(Map.of("raw",Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray())));
    var response=http.send(HttpRequest.newBuilder(sendEndpoint).timeout(Duration.ofSeconds(20))
     .header("Authorization","Bearer "+bearer).header("Content-Type","application/json")
     .POST(HttpRequest.BodyPublishers.ofString(payload,StandardCharsets.UTF_8)).build(),HttpResponse.BodyHandlers.ofString());
    if(response.statusCode()==401){accessToken=null;throw new DeliveryException("GMAIL_API_AUTHENTICATION_FAILED");}
    if(response.statusCode()==403)throw new DeliveryException("GMAIL_API_FORBIDDEN");
    if(response.statusCode()==429)throw new DeliveryException("GMAIL_API_RATE_LIMITED");
    if(response.statusCode()<200||response.statusCode()>=300)throw new DeliveryException("GMAIL_API_REJECTED");
    if(json.readTree(response.body()).path("id").asText().isBlank())throw new DeliveryException("GMAIL_API_INVALID_RESPONSE");
   }catch(DeliveryException error){throw error;}
   catch(java.net.http.HttpTimeoutException error){throw new DeliveryException("GMAIL_API_TIMEOUT");}
   catch(InterruptedException error){Thread.currentThread().interrupt();throw new DeliveryException("GMAIL_API_INTERRUPTED");}
   catch(Exception error){throw new DeliveryException("GMAIL_API_DELIVERY_FAILED");}
  }
 }

 private String token() throws Exception {
  if(accessToken!=null&&clock.instant().isBefore(tokenExpiry))return accessToken;
  String form="grant_type=refresh_token&client_id="+encode(clientId)+"&client_secret="+encode(clientSecret)+"&refresh_token="+encode(refreshToken);
  var response=http.send(HttpRequest.newBuilder(tokenEndpoint).timeout(Duration.ofSeconds(15))
   .header("Content-Type","application/x-www-form-urlencoded")
   .POST(HttpRequest.BodyPublishers.ofString(form,StandardCharsets.UTF_8)).build(),HttpResponse.BodyHandlers.ofString());
  if(response.statusCode()!=200){
   // Only allowlisted codes escape this boundary: never an OAuth response body or secret.
   String code="GMAIL_API_TOKEN_FAILED";
   if(response.statusCode()==400){
    String error=json.readTree(response.body()).path("error").asText();
    if("invalid_grant".equals(error))code="GMAIL_API_AUTHORIZATION_REQUIRED";
    if("invalid_client".equals(error))code="GMAIL_API_AUTHENTICATION_FAILED";
   }
   if(response.statusCode()==429)code="GMAIL_API_RATE_LIMITED";
   throw new DeliveryException(code);
  }
  var data=json.readTree(response.body());String value=data.path("access_token").asText();
  long expires=data.path("expires_in").asLong(0);
  if(value.isBlank()||value.contains("\r")||value.contains("\n")||expires<=0||!"Bearer".equalsIgnoreCase(data.path("token_type").asText()))
   throw new DeliveryException("GMAIL_API_INVALID_TOKEN_RESPONSE");
  accessToken=value;tokenExpiry=clock.instant().plusSeconds(Math.max(0,Math.min(expires,3600)-60));
  return value;
 }
 private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}

 public static final class DeliveryException extends MailSendException {
  private final String code;
  private DeliveryException(String code){super(code);this.code=code;}
  public String code(){return code;}
 }
}
