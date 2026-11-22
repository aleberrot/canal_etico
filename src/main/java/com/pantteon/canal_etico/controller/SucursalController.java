package com.pantteon.canal_etico.controller;
import com.pantteon.canal_etico.dto.*;
import com.pantteon.canal_etico.service.SucursalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequiredArgsConstructor
public class SucursalController {
 private final SucursalService service;
 @GetMapping("/api/empresas/{empresa}/sucursales")
 public List<SucursalResponse> publicas(@PathVariable String empresa){return service.listar(empresa,true);}
 @GetMapping("/api/admin/empresas/{empresa}/sucursales")
 public List<SucursalResponse> admin(@PathVariable String empresa){return service.listar(empresa,false);}
 @PostMapping("/api/admin/empresas/{empresa}/sucursales") @ResponseStatus(HttpStatus.CREATED)
 public SucursalResponse crear(@PathVariable String empresa,@Valid @RequestBody SucursalRequest r){return service.crear(empresa,r);}
 @PutMapping("/api/admin/empresas/{empresa}/sucursales/{codigo}")
 public SucursalResponse actualizar(@PathVariable String empresa,@PathVariable String codigo,@Valid @RequestBody SucursalRequest r){return service.actualizar(empresa,codigo,r);}
}
