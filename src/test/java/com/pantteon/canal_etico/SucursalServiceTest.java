package com.pantteon.canal_etico;
import com.pantteon.canal_etico.service.*;
import com.pantteon.canal_etico.repository.SucursalRepository;
import com.pantteon.canal_etico.model.Sucursal;
import com.pantteon.canal_etico.dto.SucursalRequest;
import com.pantteon.canal_etico.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SucursalServiceTest {
 final SucursalRepository repository=mock(SucursalRepository.class);
 final EmpresaService empresas=mock(EmpresaService.class);
 final SucursalService service=new SucursalService(repository,empresas);
 Sucursal branch(boolean active){var s=new Sucursal();s.setCodigo("chile");s.setNombre("Mathiesen Chile");s.setActiva(active);return s;}
 @Test void rejectsAnotherCompanyAndUnknownBranch(){
  when(repository.findByEmpresaCodigoAndCodigo("emp-01","chile")).thenReturn(Optional.empty());
  assertThrows(BusinessException.class,()->service.validar("emp-01","chile"));
 }
 @Test void requiresBranchWhenCompanyHasActiveBranches(){
  when(repository.findByEmpresaCodigoOrderByNombreAsc("emp-01")).thenReturn(List.of(branch(true)));
  assertThrows(BusinessException.class,()->service.validar("emp-01",null));
 }
 @Test void mathiesenNeverSilentlySkipsLocation(){assertThrows(BusinessException.class,()->service.validar("mathiesen",null));}
 @Test void preservesCompatibilityForCompanyWithoutBranches(){
  when(repository.findByEmpresaCodigoOrderByNombreAsc("emp-01")).thenReturn(List.of());assertNull(service.validar("emp-01",null));
 }
 @Test void inactiveBranchCannotBeSelected(){
  when(repository.findByEmpresaCodigoAndCodigo("mathiesen","chile")).thenReturn(Optional.of(branch(false)));
  assertThrows(BusinessException.class,()->service.validar("mathiesen","chile"));
 }
 @Test void returnsActiveBranchOnlyWithinCompany(){var s=branch(true);
  when(repository.findByEmpresaCodigoAndCodigo("mathiesen","chile")).thenReturn(Optional.of(s));assertSame(s,service.validar("mathiesen","chile"));
 }
 @Test void publicCatalogOmitsInactiveWhileAdminCanSeeBoth(){
  when(repository.findByEmpresaCodigoOrderByNombreAsc("mathiesen")).thenReturn(List.of(branch(true),branch(false)));
  assertEquals(1,service.listar("mathiesen",true).size());verify(empresas).obtenerEmpresaPublica("mathiesen");
  assertEquals(2,service.listar("mathiesen",false).size());
 }
 @Test void duplicateCodeRejectedBeforeSave(){
  when(repository.findByEmpresaCodigoAndCodigo("mathiesen","chile")).thenReturn(Optional.of(branch(true)));
  assertThrows(BusinessException.class,()->service.crear("mathiesen",new SucursalRequest("chile","Chile",true)));
  verify(repository,never()).save(any());
 }
 @Test void renameAndDeactivatePreservesStableCode(){var s=branch(true);
  when(repository.findByEmpresaCodigoAndCodigo("mathiesen","chile")).thenReturn(Optional.of(s));when(repository.save(s)).thenReturn(s);
  var result=service.actualizar("mathiesen","chile",new SucursalRequest("chile","Nuevo nombre",false));
  assertEquals("chile",result.codigo());assertFalse(result.activa());assertEquals("Nuevo nombre",result.nombre());
 }
}
