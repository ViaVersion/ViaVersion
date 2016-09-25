package us.myles.ViaVersion.api.command;

import java.util.UUID;

public interface ViaCommandSender {
    public boolean hasPermission(String permission);

    public void sendMessage(String msg);

    public UUID getUUID();

    public String getName();
}
