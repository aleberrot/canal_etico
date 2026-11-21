package com.pantteon.canal_etico.infra;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Registers the real client separately from optional demo companies. */
@Configuration
@ConditionalOnProperty(name = "app.clients.mathiesen.enabled", havingValue = "true", matchIfMissing = true)
class MathiesenCompany {
    @Bean
    ApplicationRunner registerMathiesen(JdbcTemplate jdbc,
            @Value("${app.notifications.denuncias-recipient}") String recipient) {
        return args -> {
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalStateException("The configured notification recipient must not be blank");
            }
            jdbc.update("""
                    INSERT INTO empresas (id, codigo, nombre, logo, banner, correo_notificacion, activa, created_at, updated_at)
                    VALUES (?, ?, ?, ?, NULL, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    ON CONFLICT (codigo) DO NOTHING
                    """, UUID.randomUUID(), "mathiesen", "Grupo Mathiesen",
                    "/empresas/mathiesen/logo.png", recipient);
        };
    }
}
