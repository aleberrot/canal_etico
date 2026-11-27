package com.pantteon.canal_etico;

import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.exception.BusinessException;
import com.pantteon.canal_etico.exception.InvalidCredentialsException;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.*;
import com.pantteon.canal_etico.service.*;
import com.pantteon.canal_etico.util.CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrgentCorrectionsTest {
    DenunciaRepository reports = mock(DenunciaRepository.class);
    EmpresaRepository companies = mock(EmpresaRepository.class);
    AporteDenunciaRepository contributions = mock(AporteDenunciaRepository.class);
    FileStorageService storage = mock(FileStorageService.class);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    SucursalService branches = mock(SucursalService.class);
    DenunciaService service = new DenunciaService(contributions, mock(StorageKeyGenerator.class),
            storage, reports, companies, mock(EmpresaService.class), branches, mock(NotificationQueue.class), new BusinessTime(java.time.Clock.systemUTC()), encoder);
    Empresa company;

    @BeforeEach void prepare() {
        company = new Empresa();
        company.setCodigo("emp-01");
        company.setNombre("Empresa prueba");
        company.setLogo("/cliente.png");
        company.setActiva(true);
        when(companies.findByCodigo("emp-01")).thenReturn(Optional.of(company));
        when(reports.findByCodigoUnico(anyString())).thenReturn(Optional.empty());
    }
    CrearDenunciaRequest request(TipoDenuncia type, String phone, String password) {
        return new CrearDenunciaRequest("emp-01", type, phone, "prueba@example.invalid",
                "Persona sintética", null, "DEL-001", "Prueba", "Descripción prueba",
                RelacionEmpresa.TRABAJADOR, "Descripción sintética suficientemente larga",
                null, null, password, true, "webetiko-2026-11-15");
    }
    @Test void independentReportsAndPasswords() {
        var first = service.crearDenuncia(request(TipoDenuncia.GENERAL, "111111111", "primera-clave"), null);
        var second = service.crearDenuncia(request(TipoDenuncia.LEY_KARIN, "222222222", "segunda-clave"), null);
        assertNotEquals(first.codigo(), second.codigo());
        var captor = ArgumentCaptor.forClass(Denuncia.class);
        verify(reports, times(2)).save(captor.capture());
        var saved = captor.getAllValues();
        assertNotSame(saved.get(0), saved.get(1));
        assertEquals("222222222", saved.get(1).getTelefonoDenunciante());
        assertTrue(encoder.matches("segunda-clave", saved.get(1).getPasswordHash()));
        assertFalse(encoder.matches("primera-clave", saved.get(1).getPasswordHash()));
        assertNull(saved.get(1).getArchivoPath());
    }
    @Test void selectedBranchIsPersistedWithHistoricalName() {
        var branch = new Sucursal(); branch.setCodigo("chile"); branch.setNombre("Mathiesen Chile");
        when(branches.validar("emp-01", null)).thenReturn(branch);
        service.crearDenuncia(request(TipoDenuncia.GENERAL,"111111111","clave-prueba"),null);
        var captor = ArgumentCaptor.forClass(Denuncia.class); verify(reports).save(captor.capture());
        branch.setNombre("Nombre posterior");
        assertSame(branch,captor.getValue().getSucursal());
        assertEquals("Mathiesen Chile",captor.getValue().getSucursalNombre());
    }
    @Test void invalidBranchStopsStorageAndPersistence() {
        when(branches.validar("emp-01",null)).thenThrow(new BusinessException("Sucursal ajena"));
        assertThrows(BusinessException.class,()->service.crearDenuncia(request(TipoDenuncia.GENERAL,"111111111","clave-prueba"),null));
        verifyNoInteractions(storage); verify(reports,never()).save(any());
    }
    @Test void karinRequiresPhoneBeforePersisting() {
        assertThrows(BusinessException.class, () ->
                service.crearDenuncia(request(TipoDenuncia.LEY_KARIN, "", "clave-prueba"), null));
        verify(reports, never()).save(any());
        verifyNoInteractions(storage);
    }
    @Test void incorrectPasswordCannotExposeTracking() {
        var report = new Denuncia();
        report.setPasswordHash(encoder.encode("clave-correcta"));
        when(reports.findByCodigoUnico("DEN-PRUEBA")).thenReturn(Optional.of(report));
        assertThrows(InvalidCredentialsException.class, () ->
                service.consultarDenuncia("DEN-PRUEBA", "clave-incorrecta"));
        verifyNoInteractions(contributions);
    }
    @Test void collisionRegeneratesCodeBeforeSaving() {
        try (var codes = mockStatic(CodeGenerator.class)) {
            codes.when(CodeGenerator::generarCodigo).thenReturn("DEN-TAKEN1", "DEN-FRESH1");
            when(reports.findByCodigoUnico("DEN-TAKEN1")).thenReturn(Optional.of(new Denuncia()));
            var created = service.crearDenuncia(request(TipoDenuncia.GENERAL, "", "clave-prueba"), null);
            assertEquals("DEN-FRESH1", created.codigo());
        }
    }
    @Test void missingConsentCannotBypassTerms() {
        var request = new CrearDenunciaRequest("emp-01", TipoDenuncia.GENERAL,
                "", null, null, null, "DEL-001", "Prueba", "Prueba",
                RelacionEmpresa.TRABAJADOR, "Descripción sintética suficientemente larga",
                null, null, "clave-prueba", null, null);
        assertThrows(BusinessException.class, () -> service.crearDenuncia(request, null));
        verify(reports, never()).save(any());
        verifyNoInteractions(storage);
    }

    Denuncia trackingReport() {
        var report = new Denuncia();
        report.setId(UUID.randomUUID());
        report.setCodigoUnico("DEN-PRUEBA");
        report.setPasswordHash(encoder.encode("clave-correcta"));
        report.setEmpresa(company);
        report.setTipoDenuncia(TipoDenuncia.GENERAL);
        report.setEstado(EstadoDenuncia.RECIBIDA);
        when(reports.findByCodigoUnico("DEN-PRUEBA")).thenReturn(Optional.of(report));
        return report;
    }

    @Test void trackingReturnsBrandingFromAuthenticatedReport() {
        trackingReport();
        var response = service.consultarDenuncia("DEN-PRUEBA", "clave-correcta");
        assertEquals("emp-01", response.empresa().codigo());
        assertEquals("/cliente.png", response.empresa().logo());
    }

    @Test void attachmentFromAnotherReportIsNotReturned() {
        var report = trackingReport();
        var foreignContribution = UUID.randomUUID();
        when(contributions.findByIdAndDenunciaId(foreignContribution, report.getId()))
                .thenReturn(Optional.empty());
        assertThrows(com.pantteon.canal_etico.exception.ResourceNotFoundException.class,
                () -> service.descargarAportePublico("DEN-PRUEBA", "clave-correcta",
                        foreignContribution.toString()));
        verifyNoInteractions(storage);
    }

    org.springframework.mock.web.MockMultipartFile oversizedFile() {
        return new org.springframework.mock.web.MockMultipartFile("archivo", "synthetic.txt", "text/plain", new byte[]{1}) {
            @Override public long getSize() { return 50L * 1024 * 1024 + 1; }
        };
    }
    @Test void oversizedInitialFileIsRejectedBeforeStorageOrPersistence() {
        assertThrows(org.springframework.web.multipart.MaxUploadSizeExceededException.class,
                () -> service.crearDenuncia(request(TipoDenuncia.GENERAL, "", "clave-prueba"), oversizedFile()));
        verifyNoInteractions(storage); verify(reports, never()).save(any());
    }
    @Test void oversizedContributionsAreRejectedForBothTypesWithoutAppendingHistory() {
        var report = trackingReport();
        for (var type : TipoDenuncia.values()) {
            report.setTipoDenuncia(type);
            assertThrows(org.springframework.web.multipart.MaxUploadSizeExceededException.class,
                    () -> service.agregarInformacionAdicional("DEN-PRUEBA", "clave-correcta", "Prueba", oversizedFile()));
        }
        verifyNoInteractions(storage); verify(contributions, never()).saveAndFlush(any());
    }

}
