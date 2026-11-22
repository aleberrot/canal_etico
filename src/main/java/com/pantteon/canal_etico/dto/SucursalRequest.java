package com.pantteon.canal_etico.dto;
import jakarta.validation.constraints.*;
public record SucursalRequest(@NotBlank @Size(max=80) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String codigo,
 @NotBlank @Size(max=200) String nombre, @NotNull Boolean activa) {}
