package com.pantteon.canal_etico.infra;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MathiesenCompanyTest {
    @Test void registersSeparateClientUsingConfiguredRecipient() throws Exception {
        var jdbc = mock(JdbcTemplate.class);
        new MathiesenCompany().registerMathiesen(jdbc, "configured@example.invalid").run(null);
        var calls = mockingDetails(jdbc).getInvocations();
        assertEquals(18, calls.size());
        assertEquals(17, calls.stream().skip(1).map(c -> c.getArguments()[3]).distinct().count());
        Object[] args = calls.iterator().next().getArguments();
        String sql = (String) args[0];
        assertTrue(sql.contains("ON CONFLICT (codigo) DO NOTHING"));
        assertFalse(sql.contains("UPDATE"));
        assertEquals("mathiesen", args[2]);
        assertEquals("Grupo Mathiesen", args[3]);
        assertEquals("/empresas/mathiesen/logo.png", args[4]);
        assertEquals("configured@example.invalid", args[5]);
        assertFalse(java.util.Arrays.asList(args).contains("emp-01"));
        assertFalse(java.util.Arrays.asList(args).contains("emp-02"));
    }

    @Test void refusesBlankRecipientBeforeDatabaseWrite() {
        var jdbc = mock(JdbcTemplate.class);
        assertThrows(IllegalStateException.class,
                () -> new MathiesenCompany().registerMathiesen(jdbc, "").run(null));
        verifyNoInteractions(jdbc);
    }
}
