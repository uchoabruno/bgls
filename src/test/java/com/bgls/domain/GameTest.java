package com.bgls.domain;

import static com.bgls.domain.ConsoleTestSamples.*;
import static com.bgls.domain.GameTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Game.class);
        Game game1 = getGameSample1();
        Game game2 = new Game();
        assertThat(game1).isNotEqualTo(game2);

        game2.setId(game1.getId());
        assertThat(game1).isEqualTo(game2);

        game2 = getGameSample2();
        assertThat(game1).isNotEqualTo(game2);
    }

    @Test
    void consoleTest() {
        Game game = getGameRandomSampleGenerator();
        Console consoleBack = getConsoleRandomSampleGenerator();

        game.setConsole(consoleBack);
        assertThat(game.getConsole()).isEqualTo(consoleBack);

        game.console(null);
        assertThat(game.getConsole()).isNull();
    }
}
