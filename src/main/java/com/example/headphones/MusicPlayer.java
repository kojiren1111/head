package com.example.headphones;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Plays audio from a link by piping: yt-dlp (download best audio) -> ffmpeg (decode to raw PCM) -> Java sound line.
 * Requires yt-dlp and ffmpeg to be installed and available on PATH.
 */
public final class MusicPlayer {
    private static final Object LOCK = new Object();
    private static volatile boolean running = false;
    private static volatile float volume = 0.8f;
    private static List<Process> processes = List.of();
    private static SourceDataLine line;

    private MusicPlayer() {}

    public static boolean isPlaying() { return running; }

    public static void setVolume(int percent) {
        volume = Math.max(0, Math.min(100, percent)) / 100f;
        applyVolume();
    }

    private static void applyVolume() {
        SourceDataLine l = line;
        if (l != null && l.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl c = (FloatControl) l.getControl(FloatControl.Type.MASTER_GAIN);
            float db = volume <= 0.0001f ? c.getMinimum() : (float) (20.0 * Math.log10(volume));
            c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), db)));
        }
    }

    public static void play(String url) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            say("ลิงก์ต้องขึ้นต้นด้วย https://");
            return;
        }
        stop();
        running = true;
        Thread t = new Thread(() -> run(url), "Headphones-Audio");
        t.setDaemon(true);
        t.start();
    }

    public static void stop() {
        synchronized (LOCK) {
            running = false;
            for (Process p : processes) p.destroyForcibly();
            processes = List.of();
            SourceDataLine l = line;
            if (l != null) { l.stop(); l.flush(); l.close(); }
            line = null;
        }
    }

    private static void run(String url) {
        try {
            ProcessBuilder yt = new ProcessBuilder("yt-dlp", "-f", "bestaudio", "--no-playlist", "-q", "-o", "-", "--", url)
                    .redirectError(ProcessBuilder.Redirect.DISCARD);
            ProcessBuilder ff = new ProcessBuilder("ffmpeg", "-loglevel", "quiet", "-i", "pipe:0",
                    "-f", "s16le", "-ar", "44100", "-ac", "2", "pipe:1")
                    .redirectError(ProcessBuilder.Redirect.DISCARD);

            List<Process> started;
            try {
                started = ProcessBuilder.startPipeline(List.of(yt, ff));
            } catch (IOException e) {
                say("ไม่พบ yt-dlp หรือ ffmpeg กรุณาติดตั้งและเพิ่มใน PATH ก่อน");
                running = false;
                return;
            }
            synchronized (LOCK) {
                if (!running) { started.forEach(Process::destroyForcibly); return; }
                processes = started;
            }

            AudioFormat fmt = new AudioFormat(44100f, 16, 2, true, false);
            SourceDataLine l = AudioSystem.getSourceDataLine(fmt);
            l.open(fmt, 44100 * 4);
            l.start();
            synchronized (LOCK) { line = l; }
            applyVolume();
            say("กำลังเล่นเพลง...");

            InputStream in = started.get(started.size() - 1).getInputStream();
            byte[] buf = new byte[8192];
            int n;
            boolean gotAudio = false;
            while (running && (n = in.read(buf)) > 0) {
                gotAudio = true;
                l.write(buf, 0, n);
            }
            if (running) {
                if (!gotAudio) say("เล่นไม่ได้ ตรวจสอบลิงก์หรืออัปเดต yt-dlp");
                else l.drain();
            }
        } catch (Exception e) {
            if (running) say("เกิดข้อผิดพลาด: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private static void say(String msg) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) mc.player.displayClientMessage(Component.literal("[Headphones] " + msg), false);
        });
    }
}
