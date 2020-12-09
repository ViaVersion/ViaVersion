package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets;

import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;

public class InventoryPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_16_2.COOLDOWN);
        itemRewriter.registerWindowItems(ClientboundPackets1_16_2.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        itemRewriter.registerTradeList(ClientboundPackets1_16_2.TRADE_LIST, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerSetSlot(ClientboundPackets1_16_2.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerAdvancements(ClientboundPackets1_16_2.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerEntityEquipmentArray(ClientboundPackets1_16_2.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);

        new RecipeRewriter1_16(protocol, InventoryPackets::toClient).registerDefaultHandler(ClientboundPackets1_16_2.DECLARE_RECIPES);

        itemRewriter.registerClickWindow(ServerboundPackets1_16_2.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerCreativeInvAction(ServerboundPackets1_16_2.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        protocol.registerIncoming(ServerboundPackets1_16_2.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.SPAWN_PARTICLE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Particle id
                map(Type.BOOLEAN); // Long distance
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.FLOAT); // Offset X
                map(Type.FLOAT); // Offset Y
                map(Type.FLOAT); // Offset Z
                map(Type.FLOAT); // Particle data
                map(Type.INT); // Particle count
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    if (id == 14) { // Dust
                        // RGB now written as doubles
                        wrapper.write(Type.DOUBLE, wrapper.read(Type.FLOAT).doubleValue()); // R
                        wrapper.write(Type.DOUBLE, wrapper.read(Type.FLOAT).doubleValue()); // G
                        wrapper.write(Type.DOUBLE, wrapper.read(Type.FLOAT).doubleValue()); // B
                        wrapper.passthrough(Type.FLOAT); // Scale
                    }
                });
                handler(itemRewriter.getSpawnParticleHandler(Type.FLAT_VAR_INT_ITEM, Type.DOUBLE));
            }
        });
    }

    public static void toClient(Item item) {
        if (item == null) return;

        item.setIdentifier(Protocol1_17To1_16_4.MAPPINGS.getNewItemId(item.getIdentifier()));
    }

    public static void toServer(Item item) {
        if (item == null) return;

        item.setIdentifier(Protocol1_17To1_16_4.MAPPINGS.getOldItemId(item.getIdentifier()));
    }
}
