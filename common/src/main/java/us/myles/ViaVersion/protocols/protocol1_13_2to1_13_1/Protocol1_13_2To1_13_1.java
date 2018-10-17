package us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.types.Particle1_13_2Type;

public class Protocol1_13_2To1_13_1 extends Protocol {
	public static final Particle1_13_2Type PARTICLE_TYPE = new Particle1_13_2Type();

	@Override
	protected void registerPackets() {
		InventoryPackets.register(this);
		WorldPackets.register(this);
		EntityPackets.register(this);

		//Edit Book
		registerIncoming(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLAT_ITEM, Type.FLAT_VAR_INT_ITEM);
				map(Type.BOOLEAN);
			}
		});

		// Advancements
		registerOutgoing(State.PLAY, 0x51, 0x51, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						wrapper.passthrough(Type.BOOLEAN); // Reset/clear
						int size = wrapper.passthrough(Type.VAR_INT); // Mapping size

						for (int i = 0; i < size; i++) {
							wrapper.passthrough(Type.STRING); // Identifier

							// Parent
							if (wrapper.passthrough(Type.BOOLEAN))
								wrapper.passthrough(Type.STRING);

							// Display data
							if (wrapper.passthrough(Type.BOOLEAN)) {
								wrapper.passthrough(Type.STRING); // Title
								wrapper.passthrough(Type.STRING); // Description
								Item icon = wrapper.read(Type.FLAT_ITEM);
								wrapper.write(Type.FLAT_VAR_INT_ITEM, icon);
								wrapper.passthrough(Type.VAR_INT); // Frame type
								int flags = wrapper.passthrough(Type.INT); // Flags
								if ((flags & 1) != 0)
									wrapper.passthrough(Type.STRING); // Background texture
								wrapper.passthrough(Type.FLOAT); // X
								wrapper.passthrough(Type.FLOAT); // Y
							}

							wrapper.passthrough(Type.STRING_ARRAY); // Criteria

							int arrayLength = wrapper.passthrough(Type.VAR_INT);
							for (int array = 0; array < arrayLength; array++) {
								wrapper.passthrough(Type.STRING_ARRAY); // String array
							}
						}
					}
				});
			}
		});
	}

	@Override
	public void init(UserConnection userConnection) {

	}
}
