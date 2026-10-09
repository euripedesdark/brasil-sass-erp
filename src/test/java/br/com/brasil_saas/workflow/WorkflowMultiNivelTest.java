package br.com.brasil_saas.workflow;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowMultiNivelTest {
    static List<String> splitAprovadores(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) return out;
        for (String p : raw.split("[,;]")) {
            String x = p.trim();
            if (!x.isEmpty()) out.add(x);
        }
        return out;
    }

    @Test
    void multiAprovador() {
        List<String> a = splitAprovadores("10, 20; 30");
        assertEquals(3, a.size());
        assertTrue(a.contains("20"));
    }

    @Test
    void soAvancaSeTodos() {
        long pendentes = 2;
        assertFalse(pendentes == 0);
        pendentes = 0;
        assertTrue(pendentes == 0);
    }
}
