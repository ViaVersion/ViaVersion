package us.myles.ViaVersion.velocity.platform;

import io.netty.channel.ServerChannel;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.ViaConnectionManager;

import java.util.Objects;

public class VelocityConnectionManager extends ViaConnectionManager {
    @Override
    public void onLoginSuccess(UserConnection connection) {
        if (connection.isClientSide()) {
            System.out.println("backend connect!");
            Objects.requireNonNull(connection, "connection is null!");
            connections.add(connection);
        } else {
            System.out.println("frontend connect!");
            super.onLoginSuccess(connection);
        }
    }

    @Override
    public void onDisconnect(UserConnection connection) {
        if (connection.isClientSide()) {
            System.out.println("backend disconnect!");
            Objects.requireNonNull(connection, "connection is null!");
            connections.remove(connection);
        } else {
            System.out.println("frontend disconnect!");
            super.onDisconnect(connection);
        }
    }
}
