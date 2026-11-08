package com.pantteon.canal_etico.repository;

import com.pantteon.canal_etico.model.AporteDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AporteDenunciaRepository extends JpaRepository<AporteDenuncia, UUID> {
    List<AporteDenuncia> findByDenunciaIdOrderByCreatedAtAscIdAsc(UUID denunciaId);
    Optional<AporteDenuncia> findByIdAndDenunciaId(UUID id, UUID denunciaId);
}
