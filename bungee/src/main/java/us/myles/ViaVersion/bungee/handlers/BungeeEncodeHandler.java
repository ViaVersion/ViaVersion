package us.myles.ViaVersion.bungee.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.bungee.storage.BungeeStorage;
import us.myles.ViaVersion.bungee.util.BungeePipelineUtil;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.util.PipelineUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.List;

@ChannelHandler.Sharable
public class BungeeEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression = false;

    public BungeeEncodeHandler(UserConnection info) {
        this.info = info;
    }


    @Override
    protected void encode(final ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (bytebuf.readableBytes() == 0) {
            throw new CancelException();
        }
        boolean needsCompress = false;
        if (!handledCompression) {
            if (ctx.pipeline().names().indexOf("compress") > ctx.pipeline().names().indexOf("via-encoder")) {
                // Need to decompress this packet due to bad order
                bytebuf = BungeePipelineUtil.decompress(ctx, bytebuf);
                ChannelHandler encoder = ctx.pipeline().get("via-decoder");
                ChannelHandler decoder = ctx.pipeline().get("via-encoder");
                ctx.pipeline().remove(encoder);
                ctx.pipeline().remove(decoder);
                ctx.pipeline().addAfter("decompress", "via-decoder", encoder);
                ctx.pipeline().addAfter("compress", "via-encoder", decoder);
                needsCompress = true;
                handledCompression = true;
            }
        }
        // Increment sent
        info.incrementSent();

        if (info.isActive()) {
            // Handle ID
            int id = Type.VAR_INT.read(bytebuf);
            // Transform
            ByteBuf oldPacket = bytebuf.copy();
            bytebuf.clear();

            try {
                PacketWrapper wrapper = new PacketWrapper(id, oldPacket, info);
                ProtocolInfo protInfo = info.get(ProtocolInfo.class);
                protInfo.getPipeline().transform(Direction.OUTGOING, protInfo.getState(), wrapper);
                wrapper.writeToBuffer(bytebuf);
            } catch (Exception e) {
                bytebuf.clear();
                throw e;
            } finally {
                oldPacket.release();
            }
        }

        if (needsCompress) {
            bytebuf = BungeePipelineUtil.compress(ctx, bytebuf);
        }
        out.add(bytebuf.retain());

        checkServerChange();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelException.class)) return;
        super.exceptionCaught(ctx, cause);
    }

    public void checkServerChange() throws Exception {
        if (info.has(BungeeStorage.class)) {
            BungeeStorage storage = info.get(BungeeStorage.class);
            ProxiedPlayer player = storage.getPlayer();

            if (player.getServer() != null) {
                if (player.getServer() != null && !player.getServer().getInfo().getName().equals(storage.getCurrentServer())) {
                    String serverName = player.getServer().getInfo().getName();

                    storage.setCurrentServer(serverName);

                    // TODO HANDLE
                    if (!ProtocolDetectorService.hasProtocolId(serverName)) {
                        Via.getPlatform().getLogger().severe("Could not find the protocol id for server " + serverName);
                        return;
                    }

                    int protocolId = ProtocolDetectorService.getProtocolId(serverName);

                    UserConnection viaConnection = Via.getManager().getConnection(player.getUniqueId());
                    ProtocolInfo info = viaConnection.get(ProtocolInfo.class);
                    // Refresh the pipes
                    List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocolId);
                    ProtocolPipeline pipeline = viaConnection.get(ProtocolInfo.class).getPipeline();

                    viaConnection.clearStoredObjects();
                    pipeline.cleanPipes();

                    if (protocols != null)
                        for (Pair<Integer, Protocol> prot : protocols) {
                            pipeline.add(prot.getValue());
                        }


                    viaConnection.put(info);
                    viaConnection.put(storage);

                    viaConnection.setActive(protocols != null);

                    // Init all protocols TODO check if this can get moved up to the previous for loop, and doesn't require the pipeline to already exist.
                    for (Protocol protocol : pipeline.pipes()) {
                        protocol.init(viaConnection);
                    }

                    Object wrapper = ReflectionUtil.get(player, "ch", Object.class);
                    wrapper.getClass().getDeclaredMethod("setVersion", int.class).invoke(wrapper, protocolId);
//                    ReflectionUtil.invoke(player, "init");

                    Object entityMap = Class.forName("net.md_5.bungee.entitymap.EntityMap").getDeclaredMethod("getEntityMap", int.class).invoke(null, protocolId);
                    ReflectionUtil.set(player, "entityRewrite", entityMap);
                }
            }
        }
    }
}
