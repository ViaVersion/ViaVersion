package us.myles.ViaVersion.protocols.protocol1_12_1to1_12;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_12_1TO1_12 extends Protocol {
    @Override
    protected void registerPackets() {
        registerOutgoing(State.PLAY, -1, 0x2B); // TODO new packet?
        registerOutgoing(State.PLAY, 0x2b, 0x2c); // Player Abilities (clientbound)
        registerOutgoing(State.PLAY, 0x2c, 0x2d); // Combat Event
        registerOutgoing(State.PLAY, 0x2d, 0x2e); // Player List Item
        registerOutgoing(State.PLAY, 0x2e, 0x2f); // Player Position And Look (clientbound)
        registerOutgoing(State.PLAY, 0x2f, 0x30); // Use Bed
        registerOutgoing(State.PLAY, 0x30, 0x31); // Unlock Recipes
        registerOutgoing(State.PLAY, 0x31, 0x32); // Destroy Entities
        registerOutgoing(State.PLAY, 0x32, 0x33); // Remove Entity Effect
        registerOutgoing(State.PLAY, 0x33, 0x34); // Resource Pack Send
        registerOutgoing(State.PLAY, 0x34, 0x35); // Respawn
        registerOutgoing(State.PLAY, 0x35, 0x36); // Entity Head Look
        registerOutgoing(State.PLAY, 0x36, 0x37); // Select Advancement Tab
        registerOutgoing(State.PLAY, 0x37, 0x38); // World Border
        registerOutgoing(State.PLAY, 0x38, 0x39); // Camera
        registerOutgoing(State.PLAY, 0x39, 0x3a); // Held Item Change (clientbound)
        registerOutgoing(State.PLAY, 0x3a, 0x3b); // Display Scoreboard
        registerOutgoing(State.PLAY, 0x3b, 0x3c); // Entity Metadata
        registerOutgoing(State.PLAY, 0x3c, 0x3d); // Attach Entity
        registerOutgoing(State.PLAY, 0x3d, 0x3e); // Entity Velocity
        registerOutgoing(State.PLAY, 0x3e, 0x3f); // Entity Equipment
        registerOutgoing(State.PLAY, 0x3f, 0x40); // Set Experience
        registerOutgoing(State.PLAY, 0x40, 0x41); // Update Health
        registerOutgoing(State.PLAY, 0x41, 0x42); // Scoreboard Objective
        registerOutgoing(State.PLAY, 0x42, 0x43); // Set Passengers
        registerOutgoing(State.PLAY, 0x43, 0x44); // Teams
        registerOutgoing(State.PLAY, 0x44, 0x45); // Update Sc
        registerOutgoing(State.PLAY, 0x45, 0x46); // Spawn Position
        registerOutgoing(State.PLAY, 0x46, 0x47); // Time Update
        registerOutgoing(State.PLAY, 0x47, 0x48); // Title
        registerOutgoing(State.PLAY, 0x48, 0x49); // Sound Effect
        registerOutgoing(State.PLAY, 0x49, 0x4a); // Player List Header And Footer
        registerOutgoing(State.PLAY, 0x4a, 0x4b); // Collect Item
        registerOutgoing(State.PLAY, 0x4b, 0x4c); // Entity Teleport
        registerOutgoing(State.PLAY, 0x4c, 0x4d); // Advancements
        registerOutgoing(State.PLAY, 0x4d, 0x4e); // Entity Properties
        registerOutgoing(State.PLAY, 0x4e, 0x4f); // Entity Effect

        // TODO Where did the Prepare Crafting Grid packet go to?
        registerIncoming(State.PLAY, 0x01, -1); // Prepare Crafting Grid (removed)

        registerIncoming(State.PLAY, 0x02, 0x01); // Tab-Complete (serverbound)
        registerIncoming(State.PLAY, 0x03, 0x02); // Chat Message (serverbound)
        registerIncoming(State.PLAY, 0x04, 0x03); // Client Status
        registerIncoming(State.PLAY, 0x05, 0x04); // Client Settings
        registerIncoming(State.PLAY, 0x06, 0x05); // Confirm Transaction (serverbound)
        registerIncoming(State.PLAY, 0x07, 0x06); // Enchant Item
        registerIncoming(State.PLAY, 0x08, 0x07); // Click Window
        registerIncoming(State.PLAY, 0x09, 0x08); // Close Window (serverbound)
        registerIncoming(State.PLAY, 0x0a, 0x09); // Plugin Message (serverbound)
        registerIncoming(State.PLAY, 0x0b, 0x0a); // Use Entity
        registerIncoming(State.PLAY, 0x0c, 0x0b); // Keep Alive (serverbound)
        registerIncoming(State.PLAY, 0x0d, 0x0c); // Player
        registerIncoming(State.PLAY, 0x0e, 0x0d); // Player Position
        registerIncoming(State.PLAY, 0x0f, 0x0e); // Player Position And Look (serverbound)
        registerIncoming(State.PLAY, 0x10, 0x0f); // Player Look
        registerIncoming(State.PLAY, 0x11, 0x10); // Vehicle Move (serverbound)
        registerIncoming(State.PLAY, 0x12, 0x11); // Steer Boat

        // TODO hello new packet
        registerIncoming(State.PLAY, -1, 0x12, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Unknown
                map(Type.VAR_INT); // 1 - Unknown
                map(Type.BOOLEAN); // 2 - Unknown

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });


    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
