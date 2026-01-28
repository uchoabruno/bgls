package com.bgls.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class GameTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Game getGameSample1() {
        return new Game().id(1L).name("name1");
    }

    public static Game getGameSample2() {
        return new Game().id(2L).name("name2");
    }

    public static Game getGameRandomSampleGenerator() {
        return new Game().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
