package com.pantteon.canal_etico.repository;
import com.pantteon.canal_etico.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SucursalRepository extends JpaRepository<Sucursal,UUID> {
 List<Sucursal> findByEmpresaCodigoOrderByNombreAsc(String empresa);
 Optional<Sucursal> findByEmpresaCodigoAndCodigo(String empresa,String codigo);
}
