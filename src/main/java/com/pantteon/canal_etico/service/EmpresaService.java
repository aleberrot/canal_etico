package com.pantteon.canal_etico.service;

import com.pantteon.canal_etico.dto.EmpresaRequest;
import com.pantteon.canal_etico.dto.EmpresaAdminResponse;
import com.pantteon.canal_etico.dto.EmpresaAdminResumenResponse;
import com.pantteon.canal_etico.dto.EmpresaPublicResponse;
import com.pantteon.canal_etico.exception.BusinessException;
import com.pantteon.canal_etico.exception.ResourceNotFoundException;
import com.pantteon.canal_etico.model.Empresa;
import com.pantteon.canal_etico.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaAdminResponse crearEmpresa(EmpresaRequest request) {
        if (empresaRepository.existsByCodigo(request.codigo())) {
            throw new BusinessException("Ya existe una empresa con el código indicado");
        }
        Empresa empresa = new Empresa();
        empresa.setCodigo(request.codigo());
        empresa.setNombre(request.nombre());
        empresa.setLogo(request.logo());
        empresa.setBanner(request.banner());
        empresa.setCorreoNotificacion(request.correoNotificacion());
        empresa.setActiva(true);
        return toAdminResponse(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public List<EmpresaAdminResumenResponse> listarEmpresas() {
        return empresaRepository.findAll().stream().map(this::toResumen).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaAdminResponse obtenerEmpresa(String codigo) { return toAdminResponse(buscarEntidad(codigo)); }

    @Transactional(readOnly = true)
    public Empresa buscarPorCodigo(String codigo) {
        return buscarEntidad(codigo);
    }

    @Transactional(readOnly = true)
    public EmpresaPublicResponse obtenerEmpresaPublica(String codigo) {
        Empresa empresa = buscarEntidad(codigo);
        if (!empresa.isActiva()) {
            throw new BusinessException("La empresa se encuentra inactiva");
        }
        return new EmpresaPublicResponse(
                empresa.getCodigo(),
                empresa.getNombre(),
                empresa.getLogo(),
                empresa.getBanner(),
                empresa.isActiva()
        );
    }

    public EmpresaAdminResponse desactivarEmpresa(String codigo) {
        Empresa empresa = buscarEntidad(codigo);
        empresa.setActiva(false);
        return toAdminResponse(empresaRepository.save(empresa));
    }

    public EmpresaAdminResponse activarEmpresa(String codigo) {
        Empresa empresa = buscarEntidad(codigo);
        empresa.setActiva(true);
        return toAdminResponse(empresaRepository.save(empresa));
    }

    private Empresa buscarEntidad(String codigo) {
        return empresaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    }

    private EmpresaAdminResponse toAdminResponse(Empresa e) {
        return new EmpresaAdminResponse(e.getCodigo(), e.getNombre(), e.getLogo(), e.getBanner(),
                e.getCorreoNotificacion(), e.isActiva(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private EmpresaAdminResumenResponse toResumen(Empresa e) {
        return new EmpresaAdminResumenResponse(e.getCodigo(), e.getNombre(), e.isActiva());
    }
}
