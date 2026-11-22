package com.pantteon.canal_etico.service;
import com.pantteon.canal_etico.dto.*;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.SucursalRepository;
import com.pantteon.canal_etico.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional
public class SucursalService {
 private final SucursalRepository repository;
 private final EmpresaService empresas;
 @Transactional(readOnly=true)
 public List<SucursalResponse> listar(String empresa,boolean publico) {
  if(publico) empresas.obtenerEmpresaPublica(empresa); else empresas.buscarPorCodigo(empresa);
  return repository.findByEmpresaCodigoOrderByNombreAsc(empresa).stream().filter(s -> !publico || s.isActiva()).map(this::response).toList();
 }
 public SucursalResponse crear(String empresa,SucursalRequest r) {
  Empresa e=empresas.buscarPorCodigo(empresa);
  if(repository.findByEmpresaCodigoAndCodigo(empresa,r.codigo()).isPresent()) throw new BusinessException("Ya existe esa sucursal en la empresa");
  Sucursal s=new Sucursal();s.setEmpresa(e);s.setCodigo(r.codigo());return guardar(s,r);
 }
 public SucursalResponse actualizar(String empresa,String codigo,SucursalRequest r) {
  Sucursal s=repository.findByEmpresaCodigoAndCodigo(empresa,codigo).orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada"));
  if(!codigo.equals(r.codigo())) throw new BusinessException("El código de sucursal no se puede cambiar");return guardar(s,r);
 }
 private SucursalResponse guardar(Sucursal s,SucursalRequest r) {
  if(r.nombre().isBlank()) throw new BusinessException("Nombre obligatorio");
  s.setNombre(r.nombre().trim());s.setActiva(r.activa());return response(repository.save(s));
 }
 @Transactional(readOnly=true)
 public Sucursal validar(String empresa,String codigo) {
  if(codigo==null || codigo.isBlank()) {
   if("mathiesen".equals(empresa) || repository.findByEmpresaCodigoOrderByNombreAsc(empresa).stream().anyMatch(Sucursal::isActiva)) throw new BusinessException("Selecciona dónde ocurrió el caso");return null;
  }
  Sucursal s=repository.findByEmpresaCodigoAndCodigo(empresa,codigo).orElseThrow(() -> new BusinessException("Sucursal no válida para esta empresa"));
  if(!s.isActiva()) throw new BusinessException("La sucursal está inactiva");return s;
 }
 private SucursalResponse response(Sucursal s) {return new SucursalResponse(s.getCodigo(),s.getNombre(),s.isActiva());}
}
