package us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_14To1_13_2 extends Protocol {
	@Override
	protected void registerPackets() {
		InventoryPackets.register(this);
		EntityPackets.register(this);
		WorldPackets.register(this);

		registerOutgoing(State.PLAY, 0x4E, 0x4F);
		registerOutgoing(State.PLAY, 0x4F, 0x50);
		registerOutgoing(State.PLAY, 0x50, 0x51);
		registerOutgoing(State.PLAY, 0x51, 0x52);
		registerOutgoing(State.PLAY, 0x52, 0x53);
		registerOutgoing(State.PLAY, 0x53, 0x54);

		registerOutgoing(State.PLAY, 0x55, 0x56, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						int blockTagsSize = wrapper.passthrough(Type.VAR_INT); // block tags
						for (int i = 0; i < blockTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							Integer[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
							for (int j = 0; j < blockIds.length; j++) {
								blockIds[j] = getNewBlockId(blockIds[j]);
							}
						}
						int itemTagsSize = wrapper.passthrough(Type.VAR_INT); // item tags
						for (int i = 0; i < itemTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							Integer[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
							for (int j = 0; j < itemIds.length; j++) {
								itemIds[j] = InventoryPackets.getNewItemId(itemIds[j]);
							}
						}
						int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
						for (int i = 0; i < fluidTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							wrapper.passthrough(Type.VAR_INT_ARRAY);
						}
						wrapper.write(Type.VAR_INT, 0);  // new unknown tags
					}
				});
			}
		});
	}

	public static int getNewBlockStateId(int id) {
		if (id < 1121) return id;
		else if (id < 3108) return id + 3;
		else if (id < 3278) return id + 163;
		else if (id < 3978) return id + 203;
		else if (id < 3984) return id + 207;
		else if (id < 3988) return id + 197;
		else if (id < 5284) return id + 203;
		else if (id < 7300) return id + 206;
		else if (id < 8591) return id + 212;
		else if (id < 8595) return id + 226;
		else return id + 2192;
	}

	public static int getNewBlockId(int id) {
		return id; //TODO
	}

	@Override
	public void init(UserConnection userConnection) {
		userConnection.put(new EntityTracker(userConnection));
		if (!userConnection.has(ClientWorld.class))
			userConnection.put(new ClientWorld(userConnection));

	}
}
