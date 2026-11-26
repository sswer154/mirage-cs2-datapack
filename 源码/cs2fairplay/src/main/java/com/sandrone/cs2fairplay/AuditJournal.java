package com.sandrone.cs2fairplay;

import com.mojang.logging.LogUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class AuditJournal implements AutoCloseable {
    private final BufferedWriter writer;
    private long size;
    private boolean full;

    AuditJournal(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        size = Files.exists(path) ? Files.size(path) : 0;
        full = size >= AuditWire.MAX_FILE - 64;
        writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    void write(int tick, int round, String kind, String data) {
        if (full) return;
        String row = tick + "\t" + round + "\t" + kind + "\t" + data + "\n";
        int bytes = row.getBytes(StandardCharsets.UTF_8).length;
        try {
            if (size + bytes > AuditWire.MAX_FILE - 64) {
                writer.write(tick + "\t" + round + "\tlimit\tcap\n");
                full = true;
                return;
            }
            writer.write(row);
            size += bytes;
        } catch (IOException e) {
            full = true;
            LogUtils.getLogger().warn("CS2 input audit write failed", e);
        }
    }

    void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            full = true;
            LogUtils.getLogger().warn("CS2 input audit flush failed", e);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            LogUtils.getLogger().warn("CS2 input audit close failed", e);
        }
    }
}
