package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets;

import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class PlayerPackets {

	public static void register(Protocol protocol) {

		// Open Sign Editor
		protocol.registerOutgoing(State.PLAY, 0x2C, 0x2C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION, Type.POSITION1_14);
			}
		});

		// Query Block NBT
		protocol.registerIncoming(State.PLAY, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.POSITION1_14, Type.POSITION);
			}
		});

		// Player Digging
		protocol.registerIncoming(State.PLAY, 0x18, 0x18, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.POSITION1_14, Type.POSITION);
				map(Type.BYTE);
			}
		});

		// Update Command Block
		protocol.registerIncoming(State.PLAY, 0x22, 0x22, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION1_14, Type.POSITION);
			}
		});

		// Update Structure Block
		protocol.registerIncoming(State.PLAY, 0x25, 0x25, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION1_14, Type.POSITION);
			}
		});

		// Update Sign
		protocol.registerIncoming(State.PLAY, 0x26, 0x26, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION1_14, Type.POSITION);
			}
		});

		// Player Block Placement
		protocol.registerIncoming(State.PLAY, 0x29, 0x29, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION1_14, Type.POSITION);
			}
		});
	}
}
