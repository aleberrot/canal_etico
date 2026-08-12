package com.pantteon.canal_etico.service;

import com.pantteon.canal_etico.dto.ActualizarDetalleAdminRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.dto.DenunciaAdminResponse;
import com.pantteon.canal_etico.dto.EstadisticasEmpresaResponse;
import com.pantteon.canal_etico.dto.SeguimientoDenunciaResponse;
import com.pantteon.canal_etico.exception.BusinessException;
import com.pantteon.canal_etico.exception.DenunciaNotFoundException;
import com.pantteon.canal_etico.exception.InvalidCredentialsException;
import com.pantteon.canal_etico.exception.ResourceNotFoundException;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.model.Empresa;
import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.model.TipoDenuncia;
import com.pantteon.canal_etico.repository.DenunciaRepository;
import com.pantteon.canal_etico.repository.EmpresaRepository;
import com.pantteon.canal_etico.util.CodeGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DenunciaService {
    private final FileStorageService fileStorageService;
    private final DenunciaRepository denunciaRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaService empresaService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CrearDenunciaResponse crearDenuncia(
            CrearDenunciaRequest request,
            MultipartFile archivo
    ) {
        validarSegunTipo(request);

        Empresa empresa = empresaRepository.findByCodigo(request.codigoEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        if (!empresa.isActiva()) {
            throw new BusinessException("La empresa se encuentra inactiva y no acepta denuncias");
        }

        String codigoUnico = generarCodigoUnico();

        String archivoPath = null;

        if (archivo != null && !archivo.isEmpty()) {
            archivoPath = fileStorageService.guardarArchivo(
                    archivo,
                    empresa.getCodigo(),
                    codigoUnico
            );
        }

        Denuncia denuncia = new Denuncia();

        String hashedPassword = passwordEncoder.encode(request.password());

        denuncia.setPasswordHash(hashedPassword);
        denuncia.setCodigoUnico(codigoUnico);
        denuncia.setTipoDenuncia(request.tipoDenuncia());
        denuncia.setDelitoCodigo(request.delitoCodigo());
        denuncia.setDelitoTipificacion(request.delitoTipificacion());
        denuncia.setDelitoDescripcion(request.delitoDescripcion());
        denuncia.setRelacionEmpresa(request.relacionEmpresa());
        denuncia.setDescripcion(request.descripcion());
        denuncia.setPersonasInvolucradas(request.personasInvolucradas());
        denuncia.setNombreDenunciante(request.nombreDenunciante());
        denuncia.setEmailDenunciante(request.emailDenunciante());
        denuncia.setTelefonoDenunciante(request.telefonoDenunciante());
        denuncia.setTestigos(request.testigos());
        denuncia.setEstado(EstadoDenuncia.valueOf(String.valueOf(EstadoDenuncia.RECIBIDA)));
        denuncia.setArchivoPath(archivoPath);
        denuncia.setEmpresa(empresa);
        denuncia.setCreatedAt(LocalDate.now());
        denuncia.setUpdatedAt(LocalDate.now());
        denuncia.setFechaIncidente(request.fechaIncidente());

        denunciaRepository.save(denuncia);

        try {
            emailService.enviarNuevaDenuncia(denuncia);
        } catch (Exception e) {
            log.error(
                    "La denuncia {} fue guardada, pero falló el envío del correo",
                    denuncia.getCodigoUnico(),
                    e
            );
        }
        return mapToResponse(denuncia);
    }

    private String generarCodigoUnico() {

        String codigo;

        do {
            codigo = CodeGenerator.generarCodigo();
        } while (denunciaRepository.findByCodigoUnico(codigo).isPresent());

        return codigo;
    }

    /*
    private String  generarCodigoUnico(){
        String caracteres = "ABCDEFGHIJKLMNOPRSTUVWXYZ0123456789";

        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }
    */
    public SeguimientoDenunciaResponse consultarDenuncia(String codigo, String password) {
        var denuncia = denunciaRepository
                .findByCodigoUnico(codigo)
                .orElseThrow(() -> new DenunciaNotFoundException("Codigo o clave incorrectos"));
        validarPasswordDenuncia(denuncia, password);
        return mapToSeguimientoResponse(denuncia);
    }

    public Resource descargarArchivo(String codigo) {
        Denuncia denuncia = denunciaRepository.findByCodigoUnico(codigo)
                .orElseThrow(() -> new DenunciaNotFoundException("Denuncia no encontrada"));

        if (denuncia.getArchivoPath() == null || denuncia.getArchivoPath().isBlank()) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }
        return fileStorageService.cargarArchivo(denuncia.getArchivoPath());
    }

    @Transactional
    public DenunciaAdminResponse cambiarEstado(String codigoUnico, EstadoDenuncia estado) {
        var denuncia = denunciaRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() ->
                        new DenunciaNotFoundException("Denuncia no encontrada")
                );

        denuncia.setEstado(EstadoDenuncia.valueOf(String.valueOf(estado)));

        denunciaRepository.save(denuncia);

        return mapToAdminResponse(denuncia);
    }

    @Transactional(readOnly = true)
    public DenunciaAdminResponse obtenerPorCodigoUnico(String codigoUnico) {
        var denuncia = denunciaRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() ->
                        new DenunciaNotFoundException("Denuncia no encontrada")
                );

        return mapToAdminResponse(denuncia);
    }

    @Transactional(readOnly = true)
    public List<DenunciaAdminResponse> listarDenuncias() {
        return denunciaRepository.findAll()
                .stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DenunciaAdminResponse> listarDenunciasPorEmpresa(String codigoEmpresa) {
        empresaService.buscarPorCodigo(codigoEmpresa);
        return denunciaRepository.findByEmpresaCodigo(codigoEmpresa).stream().map(this::mapToAdminResponse).toList();
    }

    @Transactional(readOnly = true)
    public EstadisticasEmpresaResponse obtenerEstadisticasEmpresa(String codigoEmpresa) {
        empresaService.buscarPorCodigo(codigoEmpresa);
        return new EstadisticasEmpresaResponse(
                denunciaRepository.countByEmpresaCodigo(codigoEmpresa),
                denunciaRepository.countByEmpresaCodigoAndTipoDenuncia(codigoEmpresa, TipoDenuncia.LEY_KARIN),
                denunciaRepository.countByEmpresaCodigoAndTipoDenuncia(codigoEmpresa, TipoDenuncia.GENERAL),
                denunciaRepository.countByEmpresaCodigoAndEstado(codigoEmpresa, EstadoDenuncia.EN_REVISION),
                denunciaRepository.countByEmpresaCodigoAndEstado(codigoEmpresa, EstadoDenuncia.CERRADA)
        );
    }

    private CrearDenunciaResponse mapToResponse(Denuncia denuncia) {

        return new CrearDenunciaResponse(
                denuncia.getCodigoUnico(),
                denuncia.getTipoDenuncia().name(),
                denuncia.getDescripcion(),
                denuncia.getEstado(),
                denuncia.getFechaIncidente()
        );
    }

    private void validarSegunTipo(CrearDenunciaRequest request) {
        if (request.tipoDenuncia() == TipoDenuncia.LEY_KARIN) {

            if (request.nombreDenunciante() == null
                    || request.nombreDenunciante().isBlank()) {
                throw new BusinessException(
                        "El nombre del denunciante es obligatorio para Ley Karin"
                );
            }

            if (request.emailDenunciante() == null
                    || request.emailDenunciante().isBlank()) {
                throw new BusinessException(
                        "El correo del denunciante es obligatorio para Ley Karin"
                );
            }

            if (request.telefonoDenunciante() == null
                    || request.telefonoDenunciante().isBlank()) {
                throw new BusinessException(
                        "El teléfono del denunciante es obligatorio para Ley Karin"
                );
            }
        }
    }
    @Transactional
    public DenunciaAdminResponse actualizarDetalleAdmin(String codigo, @Valid ActualizarDetalleAdminRequest request) {
        Denuncia denuncia = denunciaRepository.findByCodigoUnico(codigo)
                .orElseThrow(() ->
                        new DenunciaNotFoundException(
                                "No existe una denuncia con el código indicado"
                        )
                );

        if (request.nombreDenunciado() != null) {
            denuncia.setNombreDenunciado(
                    request.nombreDenunciado().trim()
            );
        }

        if (request.fechaDescargo() != null) {
            denuncia.setFechaDescargo(
                    request.fechaDescargo()
            );
        }

        if (request.descargo() != null) {
            denuncia.setDescargo(
                    request.descargo().trim()
            );
        }

        if (request.conclusionesMedidas() != null) {
            denuncia.setConclusionesMedidas(
                    request.conclusionesMedidas().trim()
            );
        }

        denuncia.setUpdatedAt(LocalDate.now());

        Denuncia actualizada = denunciaRepository.save(denuncia);

        return mapToAdminResponse(actualizada);
    }

    private void validarPasswordDenuncia(
            Denuncia denuncia,
            String password
    ) {
        if (password == null || password.isBlank()) {
            throw new InvalidCredentialsException(
                    "Código o contraseña incorrectos"
            );
        }

        boolean passwordValida = passwordEncoder.matches(
                password,
                denuncia.getPasswordHash()
        );

        if (!passwordValida) {
            throw new InvalidCredentialsException(
                    "Código o contraseña incorrectos"
            );
        }
    }

    @Transactional
    public SeguimientoDenunciaResponse agregarInformacionAdicional(
            String codigo,
            String password,
            String informacion,
            MultipartFile archivo
    ) {
        Denuncia denuncia = denunciaRepository.findByCodigoUnico(codigo)
                .orElseThrow(() ->
                        new DenunciaNotFoundException(
                                "Código o contraseña incorrectos"
                        )
                );

        validarPasswordDenuncia(denuncia, password);

        if (denuncia.getTipoDenuncia() == TipoDenuncia.LEY_KARIN) {
            throw new BusinessException(
                    "La información adicional solo está disponible para denuncias generales"
            );
        }

        boolean informacionVacia =
                informacion == null || informacion.isBlank();

        boolean archivoVacio =
                archivo == null || archivo.isEmpty();

        if (informacionVacia && archivoVacio) {
            throw new BusinessException(
                    "Debes ingresar información adicional o adjuntar un documento"
            );
        }

        if (!informacionVacia) {
            denuncia.setInformacionAdicional(
                    informacion.trim()
            );
        }

        if (!archivoVacio) {
            String documentoPath = fileStorageService.guardarArchivo(
                    archivo,
                    denuncia.getEmpresa().getCodigo(),
                    denuncia.getCodigoUnico()
            );

            denuncia.setDocumentoAdicionalPath(documentoPath);
        }

        denuncia.setUpdatedAt(LocalDate.now());

        Denuncia actualizada =
                denunciaRepository.save(denuncia);

        return mapToSeguimientoResponse(actualizada);
    }

    private DenunciaAdminResponse mapToAdminResponse(
            Denuncia denuncia
    ) {
        return new DenunciaAdminResponse(
                denuncia.getCodigoUnico(),
                denuncia.getTipoDenuncia(),
                denuncia.getDelitoCodigo(),
                denuncia.getDelitoTipificacion(),
                denuncia.getDelitoDescripcion(),
                denuncia.getRelacionEmpresa(),
                denuncia.getDescripcion(),
                denuncia.getPersonasInvolucradas(),
                denuncia.getFechaIncidente(),
                denuncia.getEstado(),
                denuncia.getNombreDenunciante(),
                denuncia.getEmailDenunciante(),
                denuncia.getTelefonoDenunciante(),
                denuncia.getTestigos(),
                denuncia.getNombreDenunciado(),
                denuncia.getFechaDescargo(),
                denuncia.getDescargo(),
                denuncia.getConclusionesMedidas(),
                denuncia.getInformacionAdicional(),
                denuncia.getCreatedAt(),
                denuncia.getUpdatedAt()
        );
    }

    private SeguimientoDenunciaResponse mapToSeguimientoResponse(
            Denuncia denuncia
    ) {
        return new SeguimientoDenunciaResponse(
                denuncia.getCodigoUnico(),
                denuncia.getTipoDenuncia(),
                denuncia.getDelitoTipificacion(),
                denuncia.getEstado(),
                denuncia.getDescripcion(),
                denuncia.getInformacionAdicional(),
                denuncia.getConclusionesMedidas(),
                denuncia.getCreatedAt(),
                denuncia.getUpdatedAt(),
                denuncia.getFechaIncidente()
        );
    }
}
