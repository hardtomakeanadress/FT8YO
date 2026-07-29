package com.bg7yoz.ft8cn.rigs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.bg7yoz.ft8cn.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

/**
 * Captures a bounded, shareable diagnostic trace for (tr)uSDX audio over CAT.
 */
public final class TrUSDXDiagnostics {
    private static final String TAG = "TrUSDXDiag";
    private static final int MAX_EVENTS = 500;
    private static final ArrayDeque<String> events = new ArrayDeque<>();

    private static String device = "not detected";
    private static String driver = "not selected";
    private static String serial = "not configured";
    private static String controlLines = "not configured";
    private static String lastError = "";
    private static long txBytes;
    private static long rxBytes;
    private static long rxCallbacks;
    private static long ua2Requests;
    private static long catReplies;
    private static long streamMarkers;
    private static long audioBytes;
    private static long audioChunks;
    private static long resampledBlocks;
    private static long lastRawRxLogAt;
    private static long lastAudioLogAt;

    private TrUSDXDiagnostics() {
    }

    public static synchronized void initialize(Context context, int expectedVendorId, int port) {
        events.clear();
        device = "not detected";
        driver = "not selected";
        serial = "not configured";
        controlLines = "not configured";
        lastError = "";
        txBytes = 0;
        rxBytes = 0;
        rxCallbacks = 0;
        ua2Requests = 0;
        catReplies = 0;
        streamMarkers = 0;
        audioBytes = 0;
        audioChunks = 0;
        resampledBlocks = 0;
        lastRawRxLogAt = 0;
        lastAudioLogAt = 0;
        event("SESSION", "app=" + BuildConfig.VERSION_NAME
                + " android=" + Build.VERSION.RELEASE + "/SDK" + Build.VERSION.SDK_INT
                + " phone=" + Build.MANUFACTURER + " " + Build.MODEL);
        event("CONFIG", String.format(Locale.US,
                "expected USB vendor=0x%04X selected port=%d", expectedVendorId, port));
    }

    public static synchronized void usbInventory(int count) {
        event("USB", "attached device count=" + count);
    }

    public static synchronized void deviceFound(
            int vendorId, int productId, String deviceName, String productName) {
        device = String.format(Locale.US, "VID:PID=%04X:%04X name=%s product=%s",
                vendorId, productId, safe(deviceName), safe(productName));
        event("USB", "matched device " + device);
    }

    public static synchronized void driverSelected(String name, int port, int portCount) {
        driver = name + " port=" + port + " portCount=" + portCount;
        event("USB", "driver selected " + driver);
    }

    public static synchronized void permission(String state) {
        event("PERMISSION", state);
    }

    public static synchronized void portOpened() {
        event("SERIAL", "port opened");
    }

    public static synchronized void serialConfigured() {
        serial = "115200 baud, 8 data bits, 1 stop bit, no parity";
        event("SERIAL", "setParameters succeeded: " + serial);
    }

    public static synchronized void controlLines(
            boolean dtrSupported, Boolean dtrActual, boolean rtsSupported, Boolean rtsActual) {
        controlLines = "DTR=" + lineValue(dtrSupported, dtrActual, true)
                + " RTS=" + lineValue(rtsSupported, rtsActual, false);
        event("SERIAL", "control lines after set: " + controlLines);
    }

    public static synchronized void connected() {
        event("SERIAL", "I/O manager started; connector reports connected");
    }

    public static synchronized void transmitted(byte[] data) {
        if (data == null) {
            return;
        }
        txBytes += data.length;
        if (startsWith(data, "UA2;")) {
            ua2Requests++;
        }
        event("TX", data.length + " B ascii=\"" + printable(data, 48)
                + "\" hex=" + hex(data, 24));
    }

    public static synchronized void transmitError(byte[] data, Exception error) {
        exception("TX failed for \"" + printable(data, 32) + "\"", error);
    }

    public static synchronized void received(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        rxBytes += data.length;
        rxCallbacks++;
        long now = System.currentTimeMillis();
        if (audioBytes == 0 || now - lastRawRxLogAt >= 2000) {
            lastRawRxLogAt = now;
            event("RX", data.length + " B ascii=\"" + printable(data, 48)
                    + "\" hex=" + hex(data, 24));
        }
    }

    public static synchronized void parsedCommand(byte[] command) {
        if (command == null) {
            return;
        }
        if (startsWith(command, "US")) {
            streamMarkers++;
            event("PARSER", "US audio marker recognized");
        } else {
            catReplies++;
            event("PARSER", "CAT reply=\"" + printable(command, 64) + "\"");
        }
    }

    public static synchronized void unknownCommand(byte[] command) {
        event("PARSER", "unrecognized command=\"" + printable(command, 64)
                + "\" hex=" + hex(command, 32));
    }

    public static synchronized void audio(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        audioBytes += data.length;
        audioChunks++;
        long now = System.currentTimeMillis();
        if (audioChunks == 1 || now - lastAudioLogAt >= 5000) {
            lastAudioLogAt = now;
            event("AUDIO", "raw audio reached app; cumulative=" + audioBytes
                    + " B in " + audioChunks + " chunks");
        }
    }

    public static synchronized void resampled(int inputBytes, int outputSamples) {
        resampledBlocks++;
        if (resampledBlocks == 1 || resampledBlocks % 50 == 0) {
            event("AUDIO", "resampler delivered block input=" + inputBytes
                    + " B output=" + outputSamples + " samples"
                    + " cumulativeBlocks=" + resampledBlocks);
        }
    }

    public static synchronized void exception(String operation, Throwable error) {
        String type = error == null ? "unknown" : error.getClass().getSimpleName();
        String detail = error == null ? "" : safe(error.getMessage());
        lastError = operation + ": " + type + ": " + detail;
        event("ERROR", lastError);
    }

    public static synchronized void error(String detail) {
        lastError = safe(detail);
        event("ERROR", lastError);
    }

    public static synchronized void disconnected() {
        event("SERIAL", "disconnected");
    }

    public static synchronized File createReport(Context context) throws IOException {
        File directory = context.getExternalCacheDir();
        if (directory == null) {
            directory = context.getCacheDir();
        }
        File report = new File(directory, "FT8YO-trUSDX-USB-diagnostic.txt");
        try (FileWriter writer = new FileWriter(report, false)) {
            writer.write(buildReport());
        }
        return report;
    }

    public static synchronized void share(Context context) throws IOException {
        File report = createReport(context);
        Uri uri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".fileprovider", report);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "FT8YO (tr)uSDX USB diagnostic");
        intent.putExtra(Intent.EXTRA_TEXT, buildReport());
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share USB diagnostic"));
    }

    public static synchronized String buildReport() {
        StringBuilder report = new StringBuilder();
        report.append("FT8YO (tr)uSDX USB diagnostic\n");
        report.append("Generated: ").append(timestamp()).append('\n');
        report.append("App: ").append(BuildConfig.VERSION_NAME).append('\n');
        report.append("Diagnosis: ").append(diagnosis()).append("\n\n");
        report.append("Device: ").append(device).append('\n');
        report.append("Driver: ").append(driver).append('\n');
        report.append("Serial: ").append(serial).append('\n');
        report.append("Control lines: ").append(controlLines).append('\n');
        report.append("TX bytes: ").append(txBytes)
                .append(" | UA2 requests: ").append(ua2Requests).append('\n');
        report.append("RX bytes: ").append(rxBytes)
                .append(" | callbacks: ").append(rxCallbacks)
                .append(" | CAT replies: ").append(catReplies)
                .append(" | US markers: ").append(streamMarkers).append('\n');
        report.append("Audio bytes: ").append(audioBytes)
                .append(" | chunks: ").append(audioChunks)
                .append(" | resampled blocks: ").append(resampledBlocks).append('\n');
        if (!lastError.isEmpty()) {
            report.append("Last error: ").append(lastError).append('\n');
        }
        report.append("\nTimestamped event trace:\n");
        for (String item : events) {
            report.append(item).append('\n');
        }
        return report.toString();
    }

    private static String diagnosis() {
        if (!lastError.isEmpty()) {
            return "APP/USB ERROR RECORDED — inspect Last error and event trace";
        }
        if (resampledBlocks > 0) {
            return "USB AUDIO REACHED DECODER INPUT — investigate decoding after USB transport";
        }
        if (audioBytes > 0) {
            return "RAW USB AUDIO ARRIVED — stream works; failure is in buffering/resampling";
        }
        if (streamMarkers > 0) {
            return "US STREAM MARKER ARRIVED WITHOUT AUDIO PAYLOAD";
        }
        if (ua2Requests >= 3 && catReplies > 0) {
            return "CAT WORKS BUT RADIO NEVER SENT A US AUDIO MARKER AFTER UA2";
        }
        if (ua2Requests >= 3 && rxBytes == 0) {
            return "UA2 WAS SENT BUT RADIO RETURNED ZERO USB BYTES";
        }
        if (ua2Requests >= 3 && rxBytes > 0) {
            return "RADIO RETURNED BYTES BUT NO VALID CAT/US STREAM MARKER WAS PARSED";
        }
        return "NOT ENOUGH DATA YET — leave receive running for at least 15 seconds";
    }

    private static void event(String category, String detail) {
        Log.i(TAG, "[" + category + "] " + detail);
        while (events.size() >= MAX_EVENTS) {
            events.removeFirst();
        }
        events.addLast(timestamp() + " [" + category + "] " + detail);
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(new Date());
    }

    private static String lineValue(boolean supported, Boolean actual, boolean wanted) {
        if (!supported) {
            return "UNSUPPORTED";
        }
        if (actual == null) {
            return wanted ? "set HIGH (readback unavailable)" : "set LOW (readback unavailable)";
        }
        return actual ? "HIGH" : "LOW";
    }

    private static boolean startsWith(byte[] data, String prefix) {
        if (data == null) {
            return false;
        }
        byte[] expected = prefix.getBytes(StandardCharsets.US_ASCII);
        if (data.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static String printable(byte[] data, int limit) {
        if (data == null) {
            return "null";
        }
        StringBuilder value = new StringBuilder();
        int length = Math.min(data.length, limit);
        for (int i = 0; i < length; i++) {
            int b = data[i] & 0xff;
            value.append(b >= 32 && b <= 126 ? (char) b : '.');
        }
        if (data.length > limit) {
            value.append("...");
        }
        return value.toString();
    }

    private static String hex(byte[] data, int limit) {
        if (data == null) {
            return "null";
        }
        StringBuilder value = new StringBuilder();
        int length = Math.min(data.length, limit);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                value.append(' ');
            }
            value.append(String.format(Locale.US, "%02X", data[i] & 0xff));
        }
        if (data.length > limit) {
            value.append(" ...");
        }
        return value.toString();
    }

    private static String safe(String value) {
        return value == null ? "unknown" : value.replace('\n', ' ').replace('\r', ' ');
    }
}
