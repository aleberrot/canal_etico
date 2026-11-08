package com.pantteon.canal_etico.infra;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@Configuration
@ConditionalOnProperty(name = "app.demo-companies.enabled", havingValue = "true")
class DemoCompanies {
    @Bean
    ApplicationRunner seedDemoCompanies(JdbcTemplate jdbc) {
        return args -> {
            insert(jdbc, "emp-01", "Empresa 1", "/empresas/emp-01/logo.webp", "/empresas/emp-01/banner.webp");
            insert(jdbc, "emp-02", "Empresa 2", null, null);
        };
    }

    private void insert(JdbcTemplate jdbc, String code, String name, String logo, String banner) {
        jdbc.update("""
                INSERT INTO empresas (id, codigo, nombre, logo, banner, correo_notificacion, activa, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (codigo) DO NOTHING
                """, UUID.randomUUID(), code, name, logo, banner, code + "@example.invalid");
    }
}
