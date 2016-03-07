package us.myles.ViaVersion.chunks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PacketChunk {

    private PacketChunkData[] chunkData;
    private byte[] biomeData;
}
