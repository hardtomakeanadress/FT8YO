package com.bg7yoz.ft8cn.rigs;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class TrUSDXCommandTest {
    @Test
    public void streamingUsesPortableUa1EnableCommand() {
        assertArrayEquals(
                "UA1;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXStreaming(true));
    }

    @Test
    public void streamingDisableCommandIsUnchanged() {
        assertArrayEquals(
                "UA0;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXStreaming(false));
    }
}
