package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class ClientChunks extends StoredObject {
    // Reflection
    private static ReflectionUtil.ClassReflection mapChunkBulkRef;
    private static ReflectionUtil.ClassReflection mapChunkRef;
    private static Method obfuscateRef;
    private static Class<?> worldRef;

    static {
        try {
            mapChunkBulkRef = new ReflectionUtil.ClassReflection(ReflectionUtil.nms("PacketPlayOutMapChunkBulk"));
            mapChunkRef = new ReflectionUtil.ClassReflection(ReflectionUtil.nms("PacketPlayOutMapChunk"));
            if (ViaVersion.getInstance().isSpigot()) {
                obfuscateRef = Class.forName("org.spigotmc.AntiXray").getMethod("obfuscate", int.class, int.class, int.class, byte[].class, ReflectionUtil.nms("World"));
                worldRef = ReflectionUtil.nms("World");
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to initialise chunks reflection", e);
        }
    }

    private final Set<Long> loadedChunks = Sets.newConcurrentHashSet();
    private final Set<Long> bulkChunks = Sets.newConcurrentHashSet();

    public ClientChunks(UserConnection user) {
        super(user);
    }

    private static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - -2147483648L;
    }

    public List<Object> transformMapChunkBulk(Object packet) {
        List<Object> list = Lists.newArrayList();
        try {
            int[] xcoords = mapChunkBulkRef.getFieldValue("a", packet, int[].class);
            int[] zcoords = mapChunkBulkRef.getFieldValue("b", packet, int[].class);
            Object[] chunkMaps = mapChunkBulkRef.getFieldValue("c", packet, Object[].class);

            if (ViaVersion.getConfig().isAntiXRay() && ViaVersion.getInstance().isSpigot()) { //Spigot anti-xray patch
                try {
                    Object world = mapChunkBulkRef.getFieldValue("world", packet, Object.class);

                    for (int i = 0; i < xcoords.length; ++i) {
                        Object spigotConfig = ReflectionUtil.getPublic(world, "spigotConfig", Object.class);
                        Object antiXrayInstance = ReflectionUtil.getPublic(spigotConfig, "antiXrayInstance", Object.class);

                        Object b = ReflectionUtil.get(chunkMaps[i], "b", Object.class);
                        Object a = ReflectionUtil.get(chunkMaps[i], "a", Object.class);

                        obfuscateRef.invoke(antiXrayInstance, xcoords[i], zcoords[i], b, a, world);
                    }
                } catch (Exception e) {
                    // not spigot, or it failed.
                }
            }
            for (int i = 0; i < chunkMaps.length; i++) {
                int x = xcoords[i];
                int z = zcoords[i];
                Object chunkMap = chunkMaps[i];
                Object chunkPacket = mapChunkRef.newInstance();
                mapChunkRef.setFieldValue("a", chunkPacket, x);
                mapChunkRef.setFieldValue("b", chunkPacket, z);
                mapChunkRef.setFieldValue("c", chunkPacket, chunkMap);
                mapChunkRef.setFieldValue("d", chunkPacket, true); // Chunk bulk chunks are always ground-up
                bulkChunks.add(toLong(x, z)); // Store for later
                list.add(chunkPacket);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to transform chunks bulk", e);
        }
        return list;
    }
}
