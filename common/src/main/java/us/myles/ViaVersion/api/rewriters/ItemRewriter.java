package us.myles.ViaVersion.api.rewriters;

import us.myles.ViaVersion.api.data.ParticleMappings;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.ClientboundPacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ServerboundPacketType;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;

// If any of these methods become outdated, just create a new rewriter overriding the methods
public class ItemRewriter {
    private final Protocol protocol;
    private final RewriteFunction toClient;
    private final RewriteFunction toServer;

    public ItemRewriter(Protocol protocol, RewriteFunction toClient, RewriteFunction toServer) {
        this.protocol = protocol;
        this.toClient = toClient;
        this.toServer = toServer;
    }

    public void registerWindowItems(ClientboundPacketType packetType, Type<Item[]> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(type); // 1 - Window Values

                handler(itemArrayHandler(type));
            }
        });
    }

    public void registerSetSlot(ClientboundPacketType packetType, Type<Item> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(type); // 2 - Slot Value

                handler(itemToClientHandler(type));
            }
        });
    }

    // Sub 1.16
    public void registerEntityEquipment(ClientboundPacketType packetType, Type<Item> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(type); // 2 - Item

                handler(itemToClientHandler(type));
            }
        });
    }

    // 1.16+
    public void registerEntityEquipmentArray(ClientboundPacketType packetType, Type<Item> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Type.BYTE);
                        // & 0x7F into an extra variable if slot is needed
                        toClient.rewrite(wrapper.passthrough(type));
                    } while ((slot & 0xFFFFFF80) != 0);
                });
            }
        });
    }

    public void registerCreativeInvAction(ServerboundPacketType packetType, Type<Item> type) {
        protocol.registerIncoming(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot
                map(type); // 1 - Clicked Item

                handler(itemToServerHandler(type));
            }
        });
    }

    public void registerClickWindow(ServerboundPacketType packetType, Type<Item> type) {
        protocol.registerIncoming(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(type); // 5 - Clicked Item

                handler(itemToServerHandler(type));
            }
        });
    }

    public void registerClickWindow1_17(ServerboundPacketType packetType, Type<Item> type) {
        protocol.registerIncoming(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                map(Type.VAR_INT); // Mode

                handler(wrapper -> {
                    // Affected items
                    int length = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.passthrough(Type.SHORT); // Slot
                        toServer.rewrite(wrapper.passthrough(type));
                    }

                    // Carried item
                    toServer.rewrite(wrapper.passthrough(type));
                });
            }
        });
    }

    public void registerSetCooldown(ClientboundPacketType packetType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int itemId = wrapper.read(Type.VAR_INT);
                    wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
                });
            }
        });
    }

    // 1.14.4+
    public void registerTradeList(ClientboundPacketType packetType, Type<Item> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                    for (int i = 0; i < size; i++) {
                        toClient.rewrite(wrapper.passthrough(type)); // Input
                        toClient.rewrite(wrapper.passthrough(type)); // Output

                        if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                            toClient.rewrite(wrapper.passthrough(type)); // Second Item
                        }

                        wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                        wrapper.passthrough(Type.INT); // Number of tools uses
                        wrapper.passthrough(Type.INT); // Maximum number of trade uses

                        wrapper.passthrough(Type.INT); // XP
                        wrapper.passthrough(Type.INT); // Special price
                        wrapper.passthrough(Type.FLOAT); // Price multiplier
                        wrapper.passthrough(Type.INT); // Demand
                    }
                    //...
                });
            }
        });
    }

    public void registerAdvancements(ClientboundPacketType packetType, Type<Item> type) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.BOOLEAN); // Reset/clear
                    int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
                    for (int i = 0; i < size; i++) {
                        wrapper.passthrough(Type.STRING); // Identifier

                        // Parent
                        if (wrapper.passthrough(Type.BOOLEAN))
                            wrapper.passthrough(Type.STRING);

                        // Display data
                        if (wrapper.passthrough(Type.BOOLEAN)) {
                            wrapper.passthrough(Type.COMPONENT); // Title
                            wrapper.passthrough(Type.COMPONENT); // Description
                            toClient.rewrite(wrapper.passthrough(type)); // Icon
                            wrapper.passthrough(Type.VAR_INT); // Frame type
                            int flags = wrapper.passthrough(Type.INT); // Flags
                            if ((flags & 1) != 0) {
                                wrapper.passthrough(Type.STRING); // Background texture
                            }
                            wrapper.passthrough(Type.FLOAT); // X
                            wrapper.passthrough(Type.FLOAT); // Y
                        }

                        wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                        int arrayLength = wrapper.passthrough(Type.VAR_INT);
                        for (int array = 0; array < arrayLength; array++) {
                            wrapper.passthrough(Type.STRING_ARRAY); // String array
                        }
                    }
                });
            }
        });
    }

    // Not the very best place for this, but has to stay here until *everything* is abstracted
    public void registerSpawnParticle(ClientboundPacketType packetType, Type<Item> itemType, Type<?> coordType) {
        protocol.registerOutgoing(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(getSpawnParticleHandler(itemType));
            }
        });
    }

    public PacketHandler getSpawnParticleHandler(Type<Item> itemType) {
        return wrapper -> {
            int id = wrapper.get(Type.INT, 0);
            if (id == -1) return;

            ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            if (id == mappings.getBlockId() || id == mappings.getFallingDustId()) {
                int data = wrapper.passthrough(Type.VAR_INT);
                wrapper.set(Type.VAR_INT, 0, protocol.getMappingData().getNewBlockStateId(data));
            } else if (id == mappings.getItemId()) {
                toClient.rewrite(wrapper.passthrough(itemType));
            }

            int newId = protocol.getMappingData().getNewParticleId(id);
            if (newId != id) {
                wrapper.set(Type.INT, 0, newId);
            }
        };
    }

    // Only sent to the client
    public PacketHandler itemArrayHandler(Type<Item[]> type) {
        return wrapper -> {
            Item[] items = wrapper.get(type, 0);
            for (Item item : items) {
                toClient.rewrite(item);
            }
        };
    }

    public PacketHandler itemToClientHandler(Type<Item> type) {
        return wrapper -> toClient.rewrite(wrapper.get(type, 0));
    }

    public PacketHandler itemToServerHandler(Type<Item> type) {
        return wrapper -> toServer.rewrite(wrapper.get(type, 0));
    }

    @FunctionalInterface
    public interface RewriteFunction {

        void rewrite(Item item);
    }
}
