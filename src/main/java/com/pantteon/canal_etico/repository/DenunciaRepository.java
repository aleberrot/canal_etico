package com.pantteon.canal_etico.repository;

import com.pantteon.canal_etico.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.Optional;
import java.util.UUID;

public interface DenunciaRepository extends JpaRepository<Denuncia, UUID> {
    Optional<Denuncia> findByCodigoUnico(String codigoUnico);

}
