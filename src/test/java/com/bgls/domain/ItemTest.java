package com.bgls.domain;

import static com.bgls.domain.GameTestSamples.*;
import static com.bgls.domain.ItemTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bgls.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Item.class);
        Item item1 = getItemSample1();
        Item item2 = new Item();
        assertThat(item1).isNotEqualTo(item2);

        item2.setId(item1.getId());
        assertThat(item1).isEqualTo(item2);

        item2 = getItemSample2();
        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void gameTest() {
        Item item = getItemRandomSampleGenerator();
        Game gameBack = getGameRandomSampleGenerator();

        item.setGame(gameBack);
        assertThat(item.getGame()).isEqualTo(gameBack);

        item.game(null);
        assertThat(item.getGame()).isNull();
    }
}
