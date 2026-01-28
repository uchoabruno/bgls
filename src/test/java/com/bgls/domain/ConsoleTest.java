package com.bgls.domain;

import static com.bgls.domain.ConsoleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConsoleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Console.class);
        Console console1 = getConsoleSample1();
        Console console2 = new Console();
        assertThat(console1).isNotEqualTo(console2);

        console2.setId(console1.getId());
        assertThat(console1).isEqualTo(console2);

        console2 = getConsoleSample2();
        assertThat(console1).isNotEqualTo(console2);
    }
}
