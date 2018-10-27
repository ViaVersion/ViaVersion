package us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13_2;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_13;
import us.myles.ViaVersion.api.type.types.version.Types1_13_2;
import us.myles.ViaVersion.packets.State;

public class EntityPackets {

	public static void register(Protocol protocol) {
		// Spawn mob packet
		protocol.registerOutgoing(State.PLAY, 0x3, 0x3, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT); // 0 - Entity ID
				map(Type.UUID); // 1 - Entity UUID
				map(Type.VAR_INT); // 2 - Entity Type
				map(Type.DOUBLE); // 3 - X
				map(Type.DOUBLE); // 4 - Y
				map(Type.DOUBLE); // 5 - Z
				map(Type.BYTE); // 6 - Yaw
				map(Type.BYTE); // 7 - Pitch
				map(Type.BYTE); // 8 - Head Pitch
				map(Type.SHORT); // 9 - Velocity X
				map(Type.SHORT); // 10 - Velocity Y
				map(Type.SHORT); // 11 - Velocity Z
				map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST); // 12 - Metadata

				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						for (Metadata metadata : wrapper.get(Types1_13_2.METADATA_LIST, 0)) {
							if (metadata.getMetaType() == MetaType1_13.Slot) {
								metadata.setMetaType(MetaType1_13_2.Slot);
							}
						}
					}
				});
			}
		});

		// Spawn player packet
		protocol.registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT); // 0 - Entity ID
				map(Type.UUID); // 1 - Player UUID
				map(Type.DOUBLE); // 2 - X
				map(Type.DOUBLE); // 3 - Y
				map(Type.DOUBLE); // 4 - Z
				map(Type.BYTE); // 5 - Yaw
				map(Type.BYTE); // 6 - Pitch
				map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST); // 7 - Metadata

				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						for (Metadata metadata : wrapper.get(Types1_13_2.METADATA_LIST, 0)) {
							if (metadata.getMetaType() == MetaType1_13.Slot) {
								metadata.setMetaType(MetaType1_13_2.Slot);
							}
						}
					}
				});
			}
		});


		// Metadata packet
		protocol.registerOutgoing(State.PLAY, 0x3F, 0x3F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT); // 0 - Entity ID
				map(Types1_13.METADATA_LIST, Types1_13_2.METADATA_LIST); // 1 - Metadata list

				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						for (Metadata metadata : wrapper.get(Types1_13_2.METADATA_LIST, 0)) {
							if (metadata.getMetaType() == MetaType1_13.Slot) {
								metadata.setMetaType(MetaType1_13_2.Slot);
							}
						}
					}
				});
			}
		});
	}

}
