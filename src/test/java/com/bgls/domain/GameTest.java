package com.bgls.domain;

import static com.bgls.domain.ConsoleTestSamples.*;
import static com.bgls.domain.GameTestSamples.*;
import static com.bgls.domain.ItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
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

    @Test
    void itemsTest() {
        Game game = getGameRandomSampleGenerator();
        Item itemBack = getItemRandomSampleGenerator();

        game.addItems(itemBack);
        assertThat(game.getItems()).containsOnly(itemBack);
        assertThat(itemBack.getGame()).isEqualTo(game);

        game.removeItems(itemBack);
        assertThat(game.getItems()).doesNotContain(itemBack);
        assertThat(itemBack.getGame()).isNull();

        game.items(new HashSet<>(Set.of(itemBack)));
        assertThat(game.getItems()).containsOnly(itemBack);
        assertThat(itemBack.getGame()).isEqualTo(game);

        game.setItems(new HashSet<>());
        assertThat(game.getItems()).doesNotContain(itemBack);
        assertThat(itemBack.getGame()).isNull();
    }
}
