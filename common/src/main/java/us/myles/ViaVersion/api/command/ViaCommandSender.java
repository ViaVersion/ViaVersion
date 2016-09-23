package us.myles.ViaVersion.api.command;

public interface ViaCommandSender {
    public boolean hasPermission(String permission);
    public void sendMessage(String msg);
}
