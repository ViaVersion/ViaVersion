package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;

public class PlayerPackets {

    public static void register(Protocol protocol) {
        protocol.registerOutgoing(ClientboundPackets1_13.OPEN_SIGN_EDITOR, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_13.DECLARE_COMMANDS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int size = wrapper.passthrough(Type.VAR_INT); // Size
                        for (int i = 0; i < size; i++) { // Nodes
                            byte flags = wrapper.passthrough(Type.BYTE); // Flags
                            wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE); //children
                            if ((flags & 0x08) != 0) {
                                wrapper.passthrough(Type.VAR_INT); // Redirect node index
                            }
                            byte nodeType = (byte) (flags & 0x03);
                            if (nodeType == 1 || nodeType == 2) { // Name for literal and argument nodes
                                wrapper.passthrough(Type.STRING); // Name
                            }
                            if (nodeType == 2) {
                                String parser = wrapper.read(Type.STRING); // Parser
                                if (parser.equals("minecraft:nbt")) {
                                    parser = "minecraft:nbt_compound_tag";
                                }
                                wrapper.write(Type.STRING, parser);
                                switch (parser) {
                                    case "brigadier:double":
                                        byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                                        if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.DOUBLE); // Min Value
                                        if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.DOUBLE); // Max Value
                                        break;
                                    case "brigadier:float":
                                        propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                                        if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.FLOAT); // Min Value
                                        if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.FLOAT); // Max Value
                                        break;
                                    case "brigadier:integer":
                                        propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
                                        if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.INT); // Min Value
                                        if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.INT); // Max Value
                                        break;
                                    case "brigadier:string":
                                        wrapper.passthrough(Type.VAR_INT); // Flags
                                        break;
                                    case "minecraft:entity":
                                    case "minecraft:score_holder":
                                        wrapper.passthrough(Type.BYTE); // Flags
                                        break;
                                }
                            }
                            if ((flags & 0x10) != 0) {
                                wrapper.passthrough(Type.STRING); // Suggestion type
                            }
                        }
                        wrapper.passthrough(Type.VAR_INT); // Root node index
                    }
                });
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.QUERY_BLOCK_NBT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION);
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item item = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        InventoryPackets.toServer(item);

                        // Client limit when editing a book was upped from 50 to 100 in 1.14, but some anti-exploit plugins ban with a size higher than the old client limit
                        if (Via.getConfig().isTruncate1_14Books()) {
                            if (item == null) return;
                            CompoundTag tag = item.getTag();

                            if (tag == null) return;
                            Tag pages = tag.get("pages");

                            if (!(pages instanceof ListTag)) return;

                            ListTag listTag = (ListTag) pages;
                            if (listTag.size() <= 50) return;
                            listTag.setValue(listTag.getValue().subList(0, 50));
                        }
                    }
                });
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.PLAYER_DIGGING, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION);
                map(Type.BYTE);
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.RECIPE_BOOK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int type = wrapper.get(Type.VAR_INT, 0);
                        if (type == 0) {
                            wrapper.passthrough(Type.STRING);
                        } else if (type == 1) {
                            wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Book Open
                            wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Filter Active
                            wrapper.passthrough(Type.BOOLEAN); // Smelting Recipe Book Open
                            wrapper.passthrough(Type.BOOLEAN); // Smelting Recipe Filter Active

                            // Unknown new booleans
                            wrapper.read(Type.BOOLEAN);
                            wrapper.read(Type.BOOLEAN);
                            wrapper.read(Type.BOOLEAN);
                            wrapper.read(Type.BOOLEAN);
                        }
                    }
                });
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.UPDATE_COMMAND_BLOCK, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });
        protocol.registerIncoming(ServerboundPackets1_14.UPDATE_STRUCTURE_BLOCK, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });
        protocol.registerIncoming(ServerboundPackets1_14.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });

        protocol.registerIncoming(ServerboundPackets1_14.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);
                        Position position = wrapper.read(Type.POSITION1_14);
                        int face = wrapper.read(Type.VAR_INT);
                        float x = wrapper.read(Type.FLOAT);
                        float y = wrapper.read(Type.FLOAT);
                        float z = wrapper.read(Type.FLOAT);
                        wrapper.read(Type.BOOLEAN);  // new unknown boolean

                        wrapper.write(Type.POSITION, position);
                        wrapper.write(Type.VAR_INT, face);
                        wrapper.write(Type.VAR_INT, hand);
                        wrapper.write(Type.FLOAT, x);
                        wrapper.write(Type.FLOAT, y);
                        wrapper.write(Type.FLOAT, z);
                    }
                });
            }
        });
    }
}
