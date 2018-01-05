package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.MappingData;

import java.util.List;

public class WorldPackets {
    public static void register(Protocol protocol) {
        // Outgoing packets

        // Block Change
        protocol.registerOutgoing(State.PLAY, 0xB, 0xB, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, toNewId(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });

        // Multi Block Change
        protocol.registerOutgoing(State.PLAY, 0x10, 0xF, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT);
                map(Type.INT);
                map(Type.BLOCK_CHANGE_RECORD_ARRAY);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Convert ids
                        for (BlockChangeRecord record : wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                            record.setBlockId(toNewId(record.getBlockId()));
                        }
                    }
                });
            }
        });

        // Named Sound Effect TODO String -> Identifier? Check if identifier is present?
        protocol.registerOutgoing(State.PLAY, 0x19, 0x1A);

        // Chunk Data
        protocol.registerOutgoing(State.PLAY, 0x20, 0x21, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        Chunk1_9_3_4Type type = new Chunk1_9_3_4Type(clientWorld);
                        Chunk chunk = wrapper.passthrough(type);

                        // Remap palette ids
                        for (ChunkSection section : chunk.getSections()) {
                            if (section == null) continue;
                            List<Integer> palette = section.getPalette();
                            for (int i = 0; i < palette.size(); i++) {
                                int newId = toNewId(palette.get(i));
                                palette.set(i, newId);
                            }
                        }
                    }
                });
            }
        });

        // Particle
        protocol.registerOutgoing(State.PLAY, 0x22, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                // TODO: This packet has changed
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        wrapper.cancel();
                    }
                });
            }
        });
    }

    public static int toNewId(int oldId) {
        if (MappingData.oldToNewBlocks.containsKey(oldId)) {
            return MappingData.oldToNewBlocks.get(oldId);
        } else {
            if (MappingData.oldToNewBlocks.containsKey((oldId >> 4) << 4)) {
                System.out.println("Missing block " + oldId);
                return MappingData.oldToNewBlocks.get((oldId >> 4) << 4);
            }
            System.out.println("Missing block completely " + oldId);
            // Default stone
            return 1;
        }
    }

}
