package com.bgls.domain;

import static com.bgls.domain.ConsoleTestSamples.*;
import static com.bgls.domain.GameTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
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

    @Test
    void gamesTest() {
        Console console = getConsoleRandomSampleGenerator();
        Game gameBack = getGameRandomSampleGenerator();

        console.addGames(gameBack);
        assertThat(console.getGames()).containsOnly(gameBack);
        assertThat(gameBack.getConsole()).isEqualTo(console);

        console.removeGames(gameBack);
        assertThat(console.getGames()).doesNotContain(gameBack);
        assertThat(gameBack.getConsole()).isNull();

        console.games(new HashSet<>(Set.of(gameBack)));
        assertThat(console.getGames()).containsOnly(gameBack);
        assertThat(gameBack.getConsole()).isEqualTo(console);

        console.setGames(new HashSet<>());
        assertThat(console.getGames()).doesNotContain(gameBack);
        assertThat(gameBack.getConsole()).isNull();
    }
}
