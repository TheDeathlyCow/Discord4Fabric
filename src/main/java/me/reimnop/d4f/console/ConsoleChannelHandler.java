package me.reimnop.d4f.console;

import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.OnConsoleMessageReceivedCallback;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.apache.logging.log4j.Level;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ConsoleChannelHandler {
    private ConsoleChannelHandler() {}

    private static DiscordConsoleBuffer buffer;
    private static boolean threadShouldStop = false;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());

    public static void stop() {
        threadShouldStop = true;
        buffer.flushAndDestroy();
        try {
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Utils.logException(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void init(Config config, Discord discord) {
        buffer = new DiscordConsoleBuffer();

        executor.scheduleAtFixedRate(() -> {
            if (threadShouldStop) {
                return;
            }

            if (buffer.getLength() > 0) {
                buffer.flush();
            }
        }, 0, 2500, TimeUnit.MILLISECONDS);

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            if (message.getChannel().getIdLong() != config.consoleChannelId || user.isBot()) {
                return;
            }

            MinecraftDedicatedServer server = (MinecraftDedicatedServer) FabricLoader.getInstance().getGameInstance();
            server.enqueueCommand(message.getContentRaw(), server.getCommandSource());
        });

        OnConsoleMessageReceivedCallback.EVENT.register(event -> {
            if (config.consoleChannelId == 0L) {
                return;
            }

            Level level = event.getLevel();
            if (level != Level.INFO && level != Level.WARN && level != Level.ERROR) {
                return;
            }

            String msg = event.getMessage().getFormattedMessage();
            addMessage(msg);
        });
    }

    private static void addMessage(String msg) {
        if (threadShouldStop) return;
        if (msg.length() > 2000) {
            String[] lines = msg.split("\n");
            if (lines.length > 1) {
                for (String line : lines) {
                    addMessage(line);
                }
                return;
            }
        }
        if (buffer.getLength() + msg.length() > 2000) {
            buffer.flush();
        }
        buffer.writeLine(msg);
    }
}