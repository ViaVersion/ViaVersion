/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data.ComponentRewriter1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.metadata.MetadataRewriter1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets.PlayerPackets;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Protocol1_14To1_13_2 extends AbstractProtocol<ClientboundPackets1_13, ClientboundPackets1_14, ServerboundPackets1_13, ServerboundPackets1_14> {

    public static final MappingData MAPPINGS = new MappingData();
    private final MetadataRewriter1_14To1_13_2 metadataRewriter = new MetadataRewriter1_14To1_13_2(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_14To1_13_2() {
        super(ClientboundPackets1_13.class, ClientboundPackets1_14.class, ServerboundPackets1_13.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        EntityPackets.register(this);
        WorldPackets.register(this);
        PlayerPackets.register(this);

        new SoundRewriter<>(this).registerSound(ClientboundPackets1_13.SOUND);
        new StatisticsRewriter<>(this).register(ClientboundPackets1_13.STATISTICS);

        ComponentRewriter<ClientboundPackets1_13> componentRewriter = new ComponentRewriter1_14<>(this);
        componentRewriter.registerComponentPacket(ClientboundPackets1_13.CHAT_MESSAGE);

        CommandRewriter<ClientboundPackets1_13> commandRewriter = new CommandRewriter<ClientboundPackets1_13>(this) {
            @Override
            public @Nullable String handleArgumentType(String argumentType) {
                if (argumentType.equals("minecraft:nbt")) {
                    return "minecraft:nbt_compound_tag";
                }
                return super.handleArgumentType(argumentType);
            }
        };
        commandRewriter.registerDeclareCommands(ClientboundPackets1_13.DECLARE_COMMANDS);

        registerClientbound(ClientboundPackets1_13.TAGS, wrapper -> {
            int blockTagsSize = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, blockTagsSize + 6); // block tags
            for (int i = 0; i < blockTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                int[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                for (int j = 0; j < blockIds.length; j++) {
                    blockIds[j] = MAPPINGS.getNewBlockId(blockIds[j]);
                }
            }
            // Minecraft crashes if we not send signs tags
            wrapper.write(Type.STRING, "minecraft:signs");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{
                    MAPPINGS.getNewBlockId(150), MAPPINGS.getNewBlockId(155)
            });
            wrapper.write(Type.STRING, "minecraft:wall_signs");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{
                    MAPPINGS.getNewBlockId(155)
            });
            wrapper.write(Type.STRING, "minecraft:standing_signs");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{
                    MAPPINGS.getNewBlockId(150)
            });
            // Fences and walls tags - used for block connections
            wrapper.write(Type.STRING, "minecraft:fences");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{189, 248, 472, 473, 474, 475});
            wrapper.write(Type.STRING, "minecraft:walls");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{271, 272});
            wrapper.write(Type.STRING, "minecraft:wooden_fences");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{189, 472, 473, 474, 475});
            int itemTagsSize = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, itemTagsSize + 2); // item tags
            for (int i = 0; i < itemTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                int[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                for (int j = 0; j < itemIds.length; j++) {
                    itemIds[j] = MAPPINGS.getNewItemId(itemIds[j]);
                }
            }
            // Should fix fuel shift clicking
            wrapper.write(Type.STRING, "minecraft:signs");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{
                    MAPPINGS.getNewItemId(541)
            });
            // Arrows tag (used by bow)
            wrapper.write(Type.STRING, "minecraft:arrows");
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{526, 825, 826});
            int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
            for (int i = 0; i < fluidTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
            }
            wrapper.write(Type.VAR_INT, 0);  // new entity tags - do we need to send this?
        });

        // Set Difficulty packet added in 19w11a
        cancelServerbound(ServerboundPackets1_14.SET_DIFFICULTY);
        // Lock Difficulty packet added in 19w11a
        cancelServerbound(ServerboundPackets1_14.LOCK_DIFFICULTY);
        // Unknown packet added in 19w13a
        cancelServerbound(ServerboundPackets1_14.UPDATE_JIGSAW_BLOCK);
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();
        WorldPackets.air = MAPPINGS.getBlockStateMappings().getNewId(0);
        WorldPackets.voidAir = MAPPINGS.getBlockStateMappings().getNewId(8591);
        WorldPackets.caveAir = MAPPINGS.getBlockStateMappings().getNewId(8592);

        Types1_13_2.PARTICLE.filler(this, false)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("item", ParticleType.Readers.ITEM1_13_2);
        Types1_14.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("item", ParticleType.Readers.ITEM1_13_2);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_14(userConnection));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public MetadataRewriter1_14To1_13_2 getEntityRewriter() {
        return metadataRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }
}
