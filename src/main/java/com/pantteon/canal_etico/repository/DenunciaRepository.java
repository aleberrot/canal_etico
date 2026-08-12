package com.pantteon.canal_etico.repository;

import com.pantteon.canal_etico.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {
    Optional<Denuncia> findByCodigoUnico(String codigoUnico);
    List<Denuncia> findByEmpresaCodigo(String codigoEmpresa);
    long countByEmpresaCodigo(String codigoEmpresa);
    long countByEmpresaCodigoAndEstado(String codigoEmpresa, com.pantteon.canal_etico.model.EstadoDenuncia estado);
    long countByEmpresaCodigoAndTipoDenuncia(String codigoEmpresa, com.pantteon.canal_etico.model.TipoDenuncia tipoDenuncia);

}
