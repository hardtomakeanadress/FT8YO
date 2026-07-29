package com.bg7yoz.ft8cn.rigs;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class TrUSDXCommandTest {
    @Test
    public void streamingUsesOfficialSpeakerOffEnableCommand() {
        assertArrayEquals(
                "UA2;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXStreaming(true));
    }

    @Test
    public void streamingDisableCommandIsUnchanged() {
        assertArrayEquals(
                "UA0;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXStreaming(false));
    }

    @Test
    public void pttOnTerminatesReceiveAudioBeforeTxCommand() {
        assertArrayEquals(
                ";TX0;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXPTTState(true));
    }

    @Test
    public void pttOffTerminatesTransmitAudioBeforeRxCommand() {
        assertArrayEquals(
                ";RX;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.setTrUSDXPTTState(false));
    }

    @Test
    public void receiveRecoveryMatchesWorkingTripleRxDriverSequence() {
        assertArrayEquals(
                ";RX;;RX;;RX;".getBytes(StandardCharsets.US_ASCII),
                KenwoodTK90RigConstant.recoverTrUSDXReceive());
    }

}
