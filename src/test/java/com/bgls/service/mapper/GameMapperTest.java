package com.bgls.service.mapper;

import static com.bgls.domain.GameAsserts.*;
import static com.bgls.domain.GameTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameMapperTest {

    private GameMapper gameMapper;

    @BeforeEach
    void setUp() {
        gameMapper = new GameMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getGameSample1();
        var actual = gameMapper.toEntity(gameMapper.toDto(expected));
        assertGameAllPropertiesEquals(expected, actual);
    }
}
