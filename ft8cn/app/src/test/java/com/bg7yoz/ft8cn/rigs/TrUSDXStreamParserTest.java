package com.bg7yoz.ft8cn.rigs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TrUSDXStreamParserTest {
    @Test
    public void streamMarkerMayBeSplitAcrossCallbacks() {
        TrUSDXStreamParser parser = new TrUSDXStreamParser();
        RecordingListener listener = new RecordingListener();

        parser.accept(ascii("U"), listener);
        assertEquals(0, listener.commands.size());

        parser.accept(ascii("S;"), listener);

        assertEquals(1, listener.commands.size());
        assertArrayEquals(ascii("US"), listener.commands.get(0));
        assertEquals(0, listener.audio.size());
        assertTrue(parser.isStreaming());
    }

    @Test
    public void audioAfterFragmentedMarkerIsDeliveredAndTerminated() {
        TrUSDXStreamParser parser = new TrUSDXStreamParser();
        RecordingListener listener = new RecordingListener();

        parser.accept(ascii("U"), listener);
        parser.accept(new byte[]{'S', ';', 1, 2, 3}, listener);
        parser.accept(new byte[]{4, 5, ';'}, listener);

        assertEquals(2, listener.audio.size());
        assertArrayEquals(new byte[]{1, 2, 3}, listener.audio.get(0));
        assertFalse(listener.force.get(0));
        assertArrayEquals(new byte[]{4, 5}, listener.audio.get(1));
        assertTrue(listener.force.get(1));
        assertFalse(parser.isStreaming());
    }

    @Test
    public void catCommandMayBeSplitAcrossCallbacks() {
        TrUSDXStreamParser parser = new TrUSDXStreamParser();
        RecordingListener listener = new RecordingListener();

        parser.accept(ascii("FA00014"), listener);
        parser.accept(ascii("074000;"), listener);

        assertEquals(1, listener.commands.size());
        assertArrayEquals(ascii("FA00014074000"), listener.commands.get(0));
        assertFalse(parser.isStreaming());
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static final class RecordingListener implements TrUSDXStreamParser.Listener {
        private final List<byte[]> commands = new ArrayList<>();
        private final List<byte[]> audio = new ArrayList<>();
        private final List<Boolean> force = new ArrayList<>();

        @Override
        public void onCommand(byte[] command) {
            commands.add(command);
        }

        @Override
        public void onAudio(byte[] data, boolean force) {
            audio.add(data);
            this.force.add(force);
        }
    }
}
