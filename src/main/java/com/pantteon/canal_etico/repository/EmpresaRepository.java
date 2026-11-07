package com.pantteon.canal_etico.repository;

import com.pantteon.canal_etico.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Optional<Empresa> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
