package us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class InventoryPackets {

	public static void register(Protocol protocol) {

		/*
            Outgoing packets
        */

		// Set slot packet
		protocol.registerOutgoing(State.PLAY, 0x17, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE); // 0 - Window ID
				map(Type.SHORT); // 1 - Slot ID
				map(Type.FLAT_ITEM, Type.FLAT_VAR_INT_ITEM); // 2 - Slot Value
			}
		});

		// Window items packet
		protocol.registerOutgoing(State.PLAY, 0x15, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE); // 0 - Window ID
				map(Type.FLAT_ITEM_ARRAY, Type.FLAT_VAR_INT_ITEM_ARRAY); // 1 - Window Values
			}
		});

		// Plugin message
		protocol.registerOutgoing(State.PLAY, 0x19, 0x19, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING); // Channel
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						String channel = wrapper.get(Type.STRING, 0);
						if (channel.equals("minecraft:trader_list") || channel.equals("trader_list")) {
							wrapper.passthrough(Type.INT); // Passthrough Window ID

							int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
							for (int i = 0; i < size; i++) {
								// Input Item
								wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));
								// Output Item
								wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));

								boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
								if (secondItem) {
									wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));
								}

								wrapper.passthrough(Type.BOOLEAN); // Trade disabled
								wrapper.passthrough(Type.INT); // Number of tools uses
								wrapper.passthrough(Type.INT); // Maximum number of trade uses
							}
						}
					}
				});
			}
		});

		// Entity Equipment Packet
		protocol.registerOutgoing(State.PLAY, 0x42, 0x42, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT); // 0 - Entity ID
				map(Type.VAR_INT); // 1 - Slot ID
				map(Type.FLAT_ITEM, Type.FLAT_VAR_INT_ITEM); // 2 - Item
			}
		});

		// Declare Recipes
		protocol.registerOutgoing(State.PLAY, 0x54, 0x54, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						int recipesNo = wrapper.passthrough(Type.VAR_INT);
						for (int i = 0; i < recipesNo; i++) {
							wrapper.passthrough(Type.STRING); // Id
							String type = wrapper.passthrough(Type.STRING);
							if (type.equals("crafting_shapeless")) {
								wrapper.passthrough(Type.STRING); // Group
								int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
								for (int i1 = 0; i1 < ingredientsNo; i1++) {
									wrapper.write(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT, wrapper.read(Type.FLAT_ITEM_ARRAY_VAR_INT));
								}
								wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));
							} else if (type.equals("crafting_shaped")) {
								int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
								wrapper.passthrough(Type.STRING); // Group
								for (int i1 = 0; i1 < ingredientsNo; i1++) {
									wrapper.write(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT, wrapper.read(Type.FLAT_ITEM_ARRAY_VAR_INT));
								}
								wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));
							} else if (type.equals("smelting")) {
								wrapper.passthrough(Type.STRING); // Group
								// Ingredient start
								wrapper.write(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT, wrapper.read(Type.FLAT_ITEM_ARRAY_VAR_INT));
								// Ingredient end
								wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.FLAT_ITEM));
								wrapper.passthrough(Type.FLOAT); // EXP
								wrapper.passthrough(Type.VAR_INT); // Cooking time
							}
						}
					}
				});
			}
		});


        /*
            Incoming packets
         */

		// Click window packet
		protocol.registerIncoming(State.PLAY, 0x08, 0x08, new PacketRemapper() {
					@Override
					public void registerMap() {
						map(Type.UNSIGNED_BYTE); // 0 - Window ID
						map(Type.SHORT); // 1 - Slot
						map(Type.BYTE); // 2 - Button
						map(Type.SHORT); // 3 - Action number
						map(Type.VAR_INT); // 4 - Mode
						map(Type.FLAT_VAR_INT_ITEM, Type.FLAT_ITEM); // 5 - Clicked Item
					}
				}
		);

		// Creative Inventory Action
		protocol.registerIncoming(State.PLAY, 0x24, 0x24, new PacketRemapper() {
					@Override
					public void registerMap() {
						map(Type.SHORT); // 0 - Slot
						map(Type.FLAT_VAR_INT_ITEM, Type.FLAT_ITEM); // 1 - Clicked Item
					}
				}
		);
	}

}
