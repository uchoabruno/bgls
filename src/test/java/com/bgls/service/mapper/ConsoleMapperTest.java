package com.bgls.service.mapper;

import static com.bgls.domain.ConsoleAsserts.*;
import static com.bgls.domain.ConsoleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleMapperTest {

    private ConsoleMapper consoleMapper;

    @BeforeEach
    void setUp() {
        consoleMapper = new ConsoleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConsoleSample1();
        var actual = consoleMapper.toEntity(consoleMapper.toDto(expected));
        assertConsoleAllPropertiesEquals(expected, actual);
    }
}
