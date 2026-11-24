package com.pantteon.canal_etico.service;
import com.pantteon.canal_etico.model.Denuncia;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
@Component
public class BusinessTime {
 public static final ZoneId ZONE=ZoneId.of("America/Santiago");
 private static final DateTimeFormatter DISPLAY=DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss XXX").withZone(ZONE);
 private final Clock clock;
 public BusinessTime(Clock clock){this.clock=clock;}
 public Instant now(){return clock.instant();}
 public LocalDate date(Instant instant){return instant.atZone(ZONE).toLocalDate();}
 public String format(Instant instant){return DISPLAY.format(instant)+" (America/Santiago)";}
 public Instant registrationInstant(Denuncia d){
  // Previous version captured consent in the creation transaction. Preserve the DATE column.
  return d.getRegisteredAt()!=null?d.getRegisteredAt():d.getTerminosAceptadosAt();
 }
 public LocalDate registrationDate(Denuncia d){var i=registrationInstant(d);return i==null?d.getCreatedAt():date(i);}
 public LocalDate updatedDate(Denuncia d){return d.getLastActivityAt()==null?d.getUpdatedAt():date(d.getLastActivityAt());}
}

