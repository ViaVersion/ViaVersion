/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;

import java.util.ArrayList;
import java.util.List;

public class BulkChunkTranslatorProvider implements Provider {
    /**
     * Transforms a Bulk Chunk packet into Chunk Packets
     *
     * @param packet       The NMS Packet
     * @param clientChunks The ClientChunks object for the current player
     * @return A List of all the output packets
     * @throws Exception If there is an issue translating
     */
    public List<Object> transformMapChunkBulk(Object packet, ClientChunks clientChunks) throws Exception {
        if (!(packet instanceof PacketWrapper))
            throw new IllegalArgumentException("The default packet has to be a PacketWrapper for transformMapChunkBulk, unexpected " + packet.getClass());

        List<Object> packets = new ArrayList<>();
        PacketWrapper wrapper = (PacketWrapper) packet;

        boolean skyLight = wrapper.read(Type.BOOLEAN);
        int count = wrapper.read(Type.VAR_INT);

        ChunkBulkSection[] metas = new ChunkBulkSection[count];
        for (int i = 0; i < count; i++) {
            metas[i] = ChunkBulkSection.read(wrapper, skyLight);
        }

        for (ChunkBulkSection meta : metas) {
            CustomByteType customByteType = new CustomByteType(meta.getLength());
            meta.setData(wrapper.read(customByteType));

            // Construct chunk packet
            PacketWrapper chunkPacket = new PacketWrapper(0x21, null, wrapper.user());
            chunkPacket.write(Type.INT, meta.getX());
            chunkPacket.write(Type.INT, meta.getZ());
            chunkPacket.write(Type.BOOLEAN, true); // Always ground-up
            chunkPacket.write(Type.UNSIGNED_SHORT, meta.getBitMask());
            chunkPacket.write(Type.VAR_INT, meta.getLength());
            chunkPacket.write(customByteType, meta.getData());

            clientChunks.getBulkChunks().add(ClientChunks.toLong(meta.getX(), meta.getZ())); // Store for later
            packets.add(chunkPacket);
        }

        return packets;
    }


    /**
     * Check if a packet of a class should be filtered
     *
     * @param packet NMS Packet
     * @return True if it should be filtered into transformmapChunkBulk
     */
    public boolean isFiltered(Class<?> packet) {
        return false;
    }

    /**
     * Check if the packet should be provided as PacketWrapper
     *
     * @return True if enabled
     */
    public boolean isPacketLevel() {
        return true;
    }

    private static class ChunkBulkSection {
        private int x;
        private int z;
        private int bitMask;
        private int length;
        private byte[] data;

        public static ChunkBulkSection read(PacketWrapper wrapper, boolean skylight) throws Exception {
            ChunkBulkSection bulkSection = new ChunkBulkSection();
            bulkSection.setX(wrapper.read(Type.INT));
            bulkSection.setZ(wrapper.read(Type.INT));
            bulkSection.setBitMask(wrapper.read(Type.UNSIGNED_SHORT));

            int bitCount = Integer.bitCount(bulkSection.getBitMask());
            bulkSection.setLength((bitCount * ((4096 * 2) + 2048)) + (skylight ? bitCount * 2048 : 0) + 256); // Thanks MCProtocolLib

            return bulkSection;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public int getBitMask() {
            return bitMask;
        }

        public void setBitMask(int bitMask) {
            this.bitMask = bitMask;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
