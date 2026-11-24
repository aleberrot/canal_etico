package com.pantteon.canal_etico;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.service.BusinessTime;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BusinessTimeTest {
 BusinessTime time=new BusinessTime(Clock.fixed(Instant.parse("2026-11-24T00:30:00Z"),ZoneOffset.UTC));
 @Test void chileanEveningDoesNotBecomeTomorrow(){
  assertEquals(LocalDate.of(2026,11,23),time.date(time.now()));
  assertEquals("23/11/2026 21:30:00 -03:00 (America/Santiago)",time.format(time.now()));
 }
 @Test void bothSidesOfMidnight(){
  assertEquals(LocalDate.of(2026,11,23),time.date(Instant.parse("2026-11-24T02:59:59Z")));
  assertEquals(LocalDate.of(2026,11,24),time.date(Instant.parse("2026-11-24T03:00:00Z")));
 }
 @Test void dstUsesTimezoneRulesInsteadOfManualSubtraction(){
  assertEquals("05/09/2026 23:59:59 -04:00 (America/Santiago)",time.format(Instant.parse("2026-09-06T03:59:59Z")));
  assertEquals("06/09/2026 01:00:00 -03:00 (America/Santiago)",time.format(Instant.parse("2026-09-06T04:00:00Z")));
  assertTrue(time.format(Instant.parse("2026-06-01T12:00:00Z")).contains("08:00:00 -04:00"));
 }
 @Test void historicalConsentCorrectsPresentationWithoutChangingDate(){
  var d=new Denuncia();d.setCreatedAt(LocalDate.of(2026,11,24));d.setTerminosAceptadosAt(time.now());
  assertEquals(LocalDate.of(2026,11,23),time.registrationDate(d));
  assertEquals(LocalDate.of(2026,11,24),d.getCreatedAt());
 }
 @Test void unknownHistoricalTimeIsNeverInvented(){
  var d=new Denuncia();d.setCreatedAt(LocalDate.of(2025,1,1));
  assertNull(time.registrationInstant(d));assertEquals(d.getCreatedAt(),time.registrationDate(d));
 }
}
