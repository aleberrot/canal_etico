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
            @Value("${MATHIESEN_NOTIFICATION_EMAIL:${app.notifications.recipients:}}") String recipientConfig) {
        return args -> {
            String recipient = recipientConfig == null ? "" : recipientConfig.split(",")[0].trim();
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalStateException("The configured notification recipient must not be blank");
            }
            jdbc.update("""
                    INSERT INTO empresas (id, codigo, nombre, logo, banner, correo_notificacion, activa, created_at, updated_at)
                    VALUES (?, ?, ?, ?, NULL, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    ON CONFLICT (codigo) DO NOTHING
                    """, UUID.randomUUID(), "mathiesen", "Grupo Mathiesen",
                    "/empresas/mathiesen/logo.png", recipient);
            // PPT 15/11/2026, slide 9. Preserve administrative edits on subsequent starts.
            for (String[] branch : new String[][] {
                {"mathiesen-argentina", "Mathiesen Argentina"},
                {"mathiesen-bolivia", "Mathiesen Bolivia"},
                {"mathiesen-cam", "Mathiesen CAM"},
                {"mathiesen-colombia", "Mathiesen Colombia"},
                {"mathiesen-espana", "Mathiesen España"},
                {"mathiesen-mexico", "Mathiesen Mexico"},
                {"mathiesen-paraguay", "Mathiesen Paraguay"},
                {"mathiesen-peru", "Mathiesen Perú"},
                {"mathiesen-uruguay", "Mathiesen Uruguay"},
                {"mathiesen-chile", "Mathiesen Chile"},
                {"mathiesen-brasil", "Mathiesen Brasil"},
                {"empresa-argentina-cdv", "Empresa Argentina CDV S.A."},
                {"soolimpex", "Soolimpex"},
                {"sercoin", "Sercoin"},
                {"luval", "Luval"},
                {"quimatic", "Quimatic"},
                {"agrovet", "Agrovet"}
            }) {
                jdbc.update("""
                    INSERT INTO sucursales (id,empresa_id,codigo,nombre,activa)
                    SELECT ?,id,?,?,TRUE FROM empresas WHERE codigo='mathiesen'
                    ON CONFLICT (empresa_id,codigo) DO NOTHING
                    """, UUID.randomUUID(),branch[0],branch[1]);
            }
        };
    }
}
