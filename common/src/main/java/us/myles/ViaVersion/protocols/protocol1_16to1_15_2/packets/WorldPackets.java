package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.BlockRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.UUIDIntArrayType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.types.Chunk1_15Type;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.UUID;

public class WorldPackets {

    public static void register(Protocol protocol) {
        BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14, Protocol1_16To1_15_2::getNewBlockStateId, Protocol1_16To1_15_2::getNewBlockId);

        // Block action
        blockRewriter.registerBlockAction(0x0B, 0x0B);

        // Block Change
        blockRewriter.registerBlockChange(0x0C, 0x0C);

        // Multi Block Change
        blockRewriter.registerMultiBlockChange(0x10, 0x10);

        // Acknowledge player digging
        blockRewriter.registerAcknowledgePlayerDigging(0x08, 0x08);

        // Chunk Data
        protocol.registerOutgoing(State.PLAY, 0x22, 0x22, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    Chunk chunk = wrapper.passthrough(new Chunk1_15Type(clientWorld));
                    for (int s = 0; s < 16; s++) {
                        ChunkSection section = chunk.getSections()[s];
                        if (section == null) continue;
                        for (int i = 0; i < section.getPaletteSize(); i++) {
                            int old = section.getPaletteEntry(i);
                            section.setPaletteEntry(i, Protocol1_16To1_15_2.getNewBlockStateId(old));
                        }
                    }

                    if (chunk.getBlockEntities() == null) return;
                    for (CompoundTag blockEntity : chunk.getBlockEntities()) {
                        String id = ((StringTag) blockEntity.get("id")).getValue();
                        if (id.equals("minecraft:conduit")) {
                            StringTag targetUuidTag = blockEntity.remove("target_uuid");
                            if (targetUuidTag == null) continue;


                            // target_uuid -> Target
                            UUID targetUuid = UUID.fromString(targetUuidTag.getValue());
                            blockEntity.put(new IntArrayTag("Target", UUIDIntArrayType.uuidToIntArray(targetUuid)));
                        } else if (id.equals("minecraft:skull")) {
                            CompoundTag ownerTag = blockEntity.remove("Owner");
                            if (ownerTag == null) continue;

                            StringTag ownerUuidTag = ownerTag.remove("Id");
                            UUID ownerUuid = UUID.fromString(ownerUuidTag.getValue());
                            ownerTag.put(new IntArrayTag("Id", UUIDIntArrayType.uuidToIntArray(ownerUuid)));

                            // Owner -> SkullOwner
                            CompoundTag skullOwnerTag = new CompoundTag("SkullOwner");
                            for (Tag tag : ownerTag) {
                                skullOwnerTag.put(tag);
                            }
                            blockEntity.put(skullOwnerTag);
                        }
                    }
                });
            }
        });

        // Effect
        blockRewriter.registerEffect(0x23, 0x23, 1010, 2001, InventoryPackets::getNewItemId);

        // Spawn Particle
        blockRewriter.registerSpawnParticle(Type.DOUBLE, 0x24, 0x24, 3, 23, 32,
                WorldPackets::getNewParticleId, InventoryPackets::toClient, Type.FLAT_VAR_INT_ITEM);
    }

    public static int getNewParticleId(int id) {
        if (id >= 27) {
            id += 2; // soul flame, soul
        }
        return id;
    }
}
