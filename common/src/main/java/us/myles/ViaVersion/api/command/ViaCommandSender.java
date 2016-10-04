package us.myles.ViaVersion.api.command;

import java.util.UUID;

public interface ViaCommandSender {
    /**
     * Check if the sender has a permission.
     *
     * @param permission Permission string eg. viaversion.admin
     * @return True if the sender has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Send a message to the sender
     *
     * @param msg The message to send
     */
    void sendMessage(String msg);

    /**
     * Get the senders UUID.
     *
     * @return The senders UUID
     */
    UUID getUUID();

    /**
     * Get the senders name.
     *
     * @return The senders name
     */
    String getName();
}
