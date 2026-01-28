package com.bgls.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ConsoleTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Console getConsoleSample1() {
        return new Console().id(1L).name("name1");
    }

    public static Console getConsoleSample2() {
        return new Console().id(2L).name("name2");
    }

    public static Console getConsoleRandomSampleGenerator() {
        return new Console().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
