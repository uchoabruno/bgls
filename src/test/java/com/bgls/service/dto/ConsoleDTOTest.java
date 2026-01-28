package com.bgls.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConsoleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConsoleDTO.class);
        ConsoleDTO consoleDTO1 = new ConsoleDTO();
        consoleDTO1.setId(1L);
        ConsoleDTO consoleDTO2 = new ConsoleDTO();
        assertThat(consoleDTO1).isNotEqualTo(consoleDTO2);
        consoleDTO2.setId(consoleDTO1.getId());
        assertThat(consoleDTO1).isEqualTo(consoleDTO2);
        consoleDTO2.setId(2L);
        assertThat(consoleDTO1).isNotEqualTo(consoleDTO2);
        consoleDTO1.setId(null);
        assertThat(consoleDTO1).isNotEqualTo(consoleDTO2);
    }
}
