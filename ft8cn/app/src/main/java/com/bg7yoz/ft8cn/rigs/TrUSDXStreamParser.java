package com.bg7yoz.ft8cn.rigs;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Parses the mixed CAT and audio stream produced by a (tr)uSDX.
 *
 * <p>Serial receive callback boundaries are arbitrary. In particular, the {@code US} audio-stream
 * marker may arrive as {@code U} in one callback and {@code S;} in the next, so command bytes must
 * be accumulated before the marker and its payload are separated.</p>
 */
final class TrUSDXStreamParser {
    interface Listener {
        void onCommand(byte[] command);

        void onAudio(byte[] data, boolean force);
    }

    private static final byte COMMAND_DELIMITER = (byte) ';';
    private static final byte STREAM_COMMAND_FIRST = (byte) 'U';
    private static final byte STREAM_COMMAND_SECOND = (byte) 'S';

    private final ByteArrayOutputStream commandBuffer = new ByteArrayOutputStream();
    private boolean streaming;

    void accept(byte[] data, Listener listener) {
        int fragmentStart = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i] != COMMAND_DELIMITER) {
                continue;
            }

            byte[] fragment = Arrays.copyOfRange(data, fragmentStart, i);
            if (streaming) {
                listener.onAudio(fragment, true);
                streaming = false;
            } else {
                commandBuffer.write(fragment, 0, fragment.length);
                handleCommand(takeCommand(), listener);
            }
            fragmentStart = i + 1;
        }

        if (fragmentStart >= data.length) {
            return;
        }

        byte[] remainder = Arrays.copyOfRange(data, fragmentStart, data.length);
        if (streaming) {
            listener.onAudio(remainder, false);
        } else if (commandBuffer.size() == 0 && isStreamCommand(remainder)) {
            streaming = true;
            listener.onCommand(Arrays.copyOf(remainder, 2));
            listener.onAudio(Arrays.copyOfRange(remainder, 2, remainder.length), false);
        } else {
            commandBuffer.write(remainder, 0, remainder.length);
        }
    }

    void clearCommandBuffer() {
        commandBuffer.reset();
    }

    void stopStreaming() {
        streaming = false;
    }

    void reset() {
        clearCommandBuffer();
        stopStreaming();
    }

    boolean isStreaming() {
        return streaming;
    }

    private void handleCommand(byte[] command, Listener listener) {
        listener.onCommand(command);
        if (!isStreamCommand(command)) {
            return;
        }

        streaming = true;
        byte[] audio = Arrays.copyOfRange(command, 2, command.length);
        if (audio.length > 0) {
            listener.onAudio(audio, false);
        }
    }

    private byte[] takeCommand() {
        byte[] command = commandBuffer.toByteArray();
        commandBuffer.reset();
        return command;
    }

    private static boolean isStreamCommand(byte[] data) {
        return data.length >= 2
                && data[0] == STREAM_COMMAND_FIRST
                && data[1] == STREAM_COMMAND_SECOND;
    }
}
