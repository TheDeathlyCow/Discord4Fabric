package me.reimnop.d4f.console;

import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.exceptions.ChannelException;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Utils;
import org.jetbrains.annotations.Nullable;

public class DiscordConsoleBuffer {
    private volatile StringBuffer bufferedMessage = new StringBuffer();

    public void writeLine(String msg) {
        bufferedMessage.append(msg);
        bufferedMessage.append('\n');
    }

    public int getLength() {
        return bufferedMessage.length();
    }

    public void flush() {
        if (Discord4Fabric.CONFIG.consoleChannelId == 0) {
            return;
        }

        try {
            Discord4Fabric.DISCORD
                    .getConsoleChannel()
                    .sendMessage(bufferedMessage.toString())
                    .queue();
            bufferedMessage = new StringBuffer();
        } catch (GuildException | ChannelException e) {
            Utils.logException(e);
        }
    }

    public void flushAndDestroy() {
        if (Discord4Fabric.CONFIG.consoleChannelId == 0) {
            return;
        }

        try {
            Discord4Fabric.DISCORD
                    .getConsoleChannel()
                    .sendMessage(bufferedMessage.toString())
                    .complete();
            bufferedMessage = null;
        } catch (GuildException | ChannelException e) {
            Utils.logException(e);
        }
    }
}
