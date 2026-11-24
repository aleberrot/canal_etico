package com.pantteon.canal_etico.repository;
import com.pantteon.canal_etico.model.NotificacionDenuncia;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface NotificacionRepository extends JpaRepository<NotificacionDenuncia,UUID> {
 boolean existsByEvento(String evento);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select n from NotificacionDenuncia n where n.estado=:estado and n.proximoIntento<=:ahora order by n.ocurridoAt,n.id")
 List<NotificacionDenuncia> pendientes(@Param("estado") NotificacionDenuncia.Estado estado,@Param("ahora") Instant ahora,Pageable page);
}

