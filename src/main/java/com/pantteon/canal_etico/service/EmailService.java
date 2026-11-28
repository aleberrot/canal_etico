package com.pantteon.canal_etico.service;
import com.pantteon.canal_etico.model.NotificacionDenuncia;
import com.pantteon.canal_etico.model.TipoDenuncia;
import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
 private final ObjectProvider<JavaMailSender> senders;
 private final BusinessTime time;
 private final String[] recipients;
 private final String adminUrl;
 private final String from;
 public EmailService(ObjectProvider<JavaMailSender> senders,BusinessTime time,
   @Value("${app.notifications.recipients:}") String recipients,
   @Value("${app.notifications.admin-url:https://canaletico.absoftwareworks.com/admin}") String adminUrl,
   @Value("${app.notifications.from:}") String from){
  this.senders=senders;this.time=time;this.adminUrl=adminUrl;
  this.from=from.trim();
  this.recipients=Arrays.stream(recipients.split(",")).map(String::trim).filter(s->!s.isBlank())
    .map(s->s.toLowerCase(Locale.ROOT)).distinct().toArray(String[]::new);
  for(String recipient:this.recipients)
   if(!recipient.matches("[^\\s@,;]+@[^\\s@,;]+\\.[^\\s@,;]+"))
    throw new IllegalArgumentException("Invalid notification recipient configuration");
  var url=java.net.URI.create(adminUrl);
  if(!"https".equals(url.getScheme())||url.getHost()==null||url.getUserInfo()!=null||url.getQuery()!=null)
   throw new IllegalArgumentException("Notification admin URL must be HTTPS without credentials or query parameters");
 }
 public boolean configurada(){
  return problemaConfiguracion()==null;
 }
 /** Sanitized operator status, never addresses or credentials. */
 public String problemaConfiguracion(){
  var sender=senders.getIfAvailable();
  if(sender==null)return "SMTP_PROVIDER_MISSING";
  if(sender instanceof com.pantteon.canal_etico.infra.GmailApiMailSender gmail){
   if(gmail.configurationProblem()!=null)return gmail.configurationProblem();
   if(!from.isBlank()&&!from.equalsIgnoreCase(gmail.getUsername()))return "GMAIL_API_FROM_MISMATCH";
  }else if(sender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl && (impl.getHost()==null||impl.getHost().isBlank()))return "SMTP_HOST_MISSING";
  if(recipients.length==0)return "NOTIFICATION_RECIPIENTS_MISSING";
  if(remitente(sender).isBlank())return "NOTIFICATION_FROM_INVALID";
  return null;
 }
 public int cantidadDestinatarios(){return recipients.length;}
 private String remitente(JavaMailSender sender){
  String value=from;
  if(value.isBlank()&&sender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl)
   value=impl.getUsername()==null?"":impl.getUsername().trim();
  if(value.isBlank()||value.contains("\r")||value.contains("\n"))return "";
  try{
   var address=new jakarta.mail.internet.InternetAddress(value,true);address.validate();
   return address.getAddress().contains("@")?value:"";
  }catch(jakarta.mail.internet.AddressException invalid){return "";}
 }
 public String asunto(NotificacionDenuncia n){
  String tipo=n.getTipo()==TipoDenuncia.LEY_KARIN?"Karin":"General";
  return n.isAporte()?"Nuevos antecedentes agregados a una Denuncia "+tipo:"Detalle de la Denuncia "+tipo;
 }
 public void enviar(NotificacionDenuncia n) throws jakarta.mail.MessagingException {
  if(!configurada())throw new IllegalStateException("Notification delivery is not configured");
  var sender=senders.getObject();
  var message=sender.createMimeMessage();
  var helper=new MimeMessageHelper(message,true,"UTF-8");helper.setTo(recipients);helper.setSubject(asunto(n));
  helper.setFrom(remitente(sender));
  String text="WEBETIKO — Canal de Denuncias\n\n"+asunto(n)+"\n\n"
   +"Evento: "+(n.isAporte()?"Nuevos antecedentes":"Nueva denuncia")
   +"\nEmpresa: "+n.getEmpresa()+"\nSucursal: "+(n.getSucursal()==null?"No informada":n.getSucursal())
   +"\nCódigo de denuncia: "+n.getCodigo()
   +"\nTipo de denuncia: "+(n.getTipo()==TipoDenuncia.LEY_KARIN?"Ley Karin":"General")
   +"\nFecha y hora: "+time.format(n.getOcurridoAt())
   +"\n\nRevise los antecedentes únicamente en el panel administrativo autenticado:\n"+adminUrl
   +"\n\nEsta notificación no incluye contraseñas, identidad del denunciante ni documentos.\nMensaje automático de WEBETIKO.";
  String html="<html><body style=\"margin:0;background:#f3f5fb;font-family:Arial,sans-serif;color:#17243c\"><div style=\"max-width:620px;margin:24px auto;background:white;padding:24px\">"
   +"<p style=\"color:#4432a5;font-weight:bold\">WEBETIKO · Canal de Denuncias</p><h1 style=\"font-size:23px;background:#243e78;color:white;padding:18px\">"+escape(asunto(n))+"</h1>"
   +"<p>Se ha registrado "+(n.isAporte()?"información adicional para una denuncia existente.":"una nueva denuncia.")+"</p>"
   +"<table style=\"border-collapse:collapse;width:100%;font-size:14px\"><thead><tr style=\"background:#e7f2ef\"><th style=\"padding:12px;text-align:left\">Campo</th><th style=\"padding:12px;text-align:left\">Valor</th></tr></thead><tbody>"
   +row("Evento",n.isAporte()?"Nuevos antecedentes":"Nueva denuncia")
   +row("Empresa",n.getEmpresa())+row("Sucursal",n.getSucursal()==null?"No informada":n.getSucursal())
   +row("Código de denuncia",n.getCodigo())+row("Tipo",n.getTipo()==TipoDenuncia.LEY_KARIN?"Ley Karin":"General")
   +row("Fecha y hora",time.format(n.getOcurridoAt()))+"</tbody></table>"
   +"<p><a href=\""+escape(adminUrl)+"\" style=\"color:#243e78\">Abrir panel administrativo seguro</a></p>"
   +"<p style=\"font-size:12px;color:#546176\">Esta notificación no incluye contraseñas, identidad del denunciante ni documentos. No responda a este mensaje automático.</p></div></body></html>";
  helper.setText(text,html);
  message.saveChanges();
  message.setHeader("Message-ID","<"+n.getId()+"@"+java.net.URI.create(adminUrl).getHost()+">");
  message.setHeader("X-WEBETIKO-Event-ID",n.getId().toString());
  sender.send(message);
 }
 private static String escape(String value){return value==null?"":value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
 private static String row(String label,String value){return "<tr><td style=\"padding:12px;border-bottom:1px solid #e1e6ef\">"+escape(label)+"</td><td style=\"padding:12px;border-bottom:1px solid #e1e6ef\">"+escape(value)+"</td></tr>";}
}

