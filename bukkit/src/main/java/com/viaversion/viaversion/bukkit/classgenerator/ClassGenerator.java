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
package com.viaversion.viaversion.bukkit.classgenerator;

import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.classgenerator.generated.BasicHandlerConstructor;
import com.viaversion.viaversion.classgenerator.generated.HandlerConstructor;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

//TODO maybe clean this up a bit 👀
public class ClassGenerator {
    private static final boolean useModules = hasModuleMethod();
    private static HandlerConstructor constructor = new BasicHandlerConstructor();
    private static String psPackage;
    private static Class psConnectListener;

    public static HandlerConstructor getConstructor() {
        return constructor;
    }

    public static void generate() {
        if (ViaVersionPlugin.getInstance().isCompatSpigotBuild() || ViaVersionPlugin.getInstance().isProtocolSupport()) {
            try {
                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new LoaderClassPath(Bukkit.class.getClassLoader()));
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    pool.insertClassPath(new LoaderClassPath(p.getClass().getClassLoader()));
                }

                if (ViaVersionPlugin.getInstance().isCompatSpigotBuild()) {
                    Class decodeSuper = NMSUtil.nms("PacketDecoder", "net.minecraft.network.PacketDecoder");
                    Class encodeSuper = NMSUtil.nms("PacketEncoder", "net.minecraft.network.PacketEncoder");
                    // Generate the classes
                    addSpigotCompatibility(pool, BukkitDecodeHandler.class, decodeSuper);
                    addSpigotCompatibility(pool, BukkitEncodeHandler.class, encodeSuper);
                } else {
                    // ProtocolSupport compatibility
                    Class encodeSuper;
                    Class decodeSuper;
                    if (isMultiplatformPS()) {
                        psConnectListener = makePSConnectListener(pool, shouldUseNewHandshakeVersionMethod());
                        return;
                    } else {
                        String psPackage = getOldPSPackage();
                        decodeSuper = Class.forName(psPackage.equals("unknown") ? "protocolsupport.protocol.pipeline.common.PacketDecoder" : psPackage + ".wrapped.WrappedDecoder");
                        encodeSuper = Class.forName(psPackage.equals("unknown") ? "protocolsupport.protocol.pipeline.common.PacketEncoder" : psPackage + ".wrapped.WrappedEncoder");
                    }
                    // Generate the classes
                    addPSCompatibility(pool, BukkitDecodeHandler.class, decodeSuper);
                    addPSCompatibility(pool, BukkitEncodeHandler.class, encodeSuper);
                }


                // Implement Constructor
                CtClass generated = pool.makeClass("com.viaversion.viaversion.classgenerator.generated.GeneratedConstructor");
                CtClass handlerInterface = pool.get(HandlerConstructor.class.getName());

                generated.setInterfaces(new CtClass[]{handlerInterface});
                // Import required classes
                pool.importPackage("com.viaversion.viaversion.classgenerator.generated");
                pool.importPackage("com.viaversion.viaversion.classgenerator");
                pool.importPackage("com.viaversion.viaversion.api.connection");
                pool.importPackage("io.netty.handler.codec");
                // Implement Methods
                generated.addMethod(CtMethod.make("public MessageToByteEncoder newEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {\n" +
                        "        return new BukkitEncodeHandler(info, minecraftEncoder);\n" +
                        "    }", generated));
                generated.addMethod(CtMethod.make("public ByteToMessageDecoder newDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {\n" +
                        "        return new BukkitDecodeHandler(info, minecraftDecoder);\n" +
                        "    }", generated));

                constructor = (HandlerConstructor) toClass(generated).newInstance();
            } catch (ReflectiveOperationException | CannotCompileException | NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addSpigotCompatibility(ClassPool pool, Class input, Class superclass) {
        String newName = "com.viaversion.viaversion.classgenerator.generated." + input.getSimpleName();

        try {
            CtClass generated = pool.getAndRename(input.getName(), newName);
            if (superclass != null) {
                CtClass toExtend = pool.get(superclass.getName());
                generated.setSuperclass(toExtend);

                // If it's NMS satisfy constructor.
                if (superclass.getName().startsWith("net.minecraft")) {
                    // Modify constructor to call super
                    if (generated.getConstructors().length != 0) {
                        generated.getConstructors()[0].instrument(new ExprEditor() {
                            @Override
                            public void edit(ConstructorCall c) throws CannotCompileException {
                                if (c.isSuper()) {
                                    // Constructor for both has a stats thing.
                                    c.replace("super(null);");
                                }
                                super.edit(c);
                            }
                        });
                    }
                }
            }
            toClass(generated);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private static void addPSCompatibility(ClassPool pool, Class input, Class superclass) {
        boolean newPS = getOldPSPackage().equals("unknown");
        String newName = "com.viaversion.viaversion.classgenerator.generated." + input.getSimpleName();

        try {
            CtClass generated = pool.getAndRename(input.getName(), newName);
            if (superclass != null) {
                CtClass toExtend = pool.get(superclass.getName());
                generated.setSuperclass(toExtend);

                if (!newPS) {
                    // Override setRealEncoder / setRealDecoder
                    pool.importPackage(getOldPSPackage());
                    pool.importPackage(getOldPSPackage() + ".wrapped");
                    if (superclass.getName().endsWith("Decoder")) {
                        // Decoder
                        generated.addMethod(CtMethod.make("public void setRealDecoder(IPacketDecoder dec) {\n" +
                                "        ((WrappedDecoder) this.minecraftDecoder).setRealDecoder(dec);\n" +
                                "    }", generated));
                    } else {
                        // Encoder
                        pool.importPackage("protocolsupport.api");
                        pool.importPackage("java.lang.reflect");
                        generated.addMethod(CtMethod.make("public void setRealEncoder(IPacketEncoder enc) {\n" +
                                "         try {\n" +
                                // Tell ProtocolSupport to decode MINECRAFT_FUTURE packets using the default decoder (for 1.9.4)
                                "             Field field = enc.getClass().getDeclaredField(\"version\");\n" +
                                "             field.setAccessible(true);\n" +
                                "             ProtocolVersion version = (ProtocolVersion) field.get(enc);\n" +

                                "             if (version == ProtocolVersion.MINECRAFT_FUTURE) enc = enc.getClass().getConstructor(\n" +
                                "                 new Class[]{ProtocolVersion.class}).newInstance(new Object[] {ProtocolVersion.getLatest()});\n" +
                                "         } catch (Exception e) {\n" +
                                // I guess we're not on 1.9.4
                                "         }\n" +
                                "        ((WrappedEncoder) this.minecraftEncoder).setRealEncoder(enc);\n" +
                                "    }", generated));
                    }
                }
            }
            toClass(generated);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private static Class makePSConnectListener(ClassPool pool, boolean newVersionMethod) {
        try {
            // Reference classes
            CtClass toExtend = pool.get("protocolsupport.api.Connection$PacketListener");
            CtClass connectListenerClazz = pool.makeClass("com.viaversion.viaversion.classgenerator.generated.ProtocolSupportConnectListener");
            connectListenerClazz.setSuperclass(toExtend);
            // Import packages
            pool.importPackage("java.util.Arrays");
            pool.importPackage("protocolsupport.api.ProtocolVersion");
            pool.importPackage("protocolsupport.api.ProtocolType");
            pool.importPackage("protocolsupport.api.Connection");
            pool.importPackage("protocolsupport.api.Connection.PacketListener");
            pool.importPackage("protocolsupport.api.Connection.PacketListener.PacketEvent");
            pool.importPackage("protocolsupport.protocol.ConnectionImpl");
            pool.importPackage(NMSUtil.nms(
                    "PacketHandshakingInSetProtocol",
                    "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol"
            ).getName());
            // Add connection reference field
            connectListenerClazz.addField(CtField.make("private ConnectionImpl connection;", connectListenerClazz));
            // Bake constructor
            connectListenerClazz.addConstructor(CtNewConstructor.make(
                    "public ProtocolSupportConnectListener (ConnectionImpl connection) {\n"
                            + "    this.connection = connection;\n"
                            + "}", connectListenerClazz));
            // Add the listening method
            connectListenerClazz.addMethod(CtNewMethod.make(
                    // On packet receive
                    "public void onPacketReceiving(protocolsupport.api.Connection.PacketListener.PacketEvent event) {\n"
                            // Check if we are getting handshake packet.
                            + "    if (event.getPacket() instanceof PacketHandshakingInSetProtocol) {\n"
                            // Get protocol version.
                            + "        PacketHandshakingInSetProtocol packet = (PacketHandshakingInSetProtocol) event.getPacket();\n"
                            + (newVersionMethod ? (
                            "        int protoVersion = packet.getProtocolVersion();\n"
                    ) : (
                            "        int protoVersion = packet.b();\n"
                    ))
                            // ViaVersion has at this point already spoofed the connectionversion. (Since it is higher up the pipeline)
                            // If via has put the protoVersion to the server we can spoof ProtocolSupport's version.
                            + "        if (connection.getVersion() == ProtocolVersion.MINECRAFT_FUTURE && protoVersion == com.viaversion.viaversion.api.Via.getAPI().getServerVersion().lowestSupportedVersion()) {\n"
                            + "            connection.setVersion(ProtocolVersion.getLatest(ProtocolType.PC));\n"
                            + "        }\n"
                            + "    }\n"
                            // Id version is not serverversion viaversion will not spoof. ProtocolSupport will handle the rest.
                            // In any case, remove the packet listener and wrap up.
                            + "    connection.removePacketListener(this);\n"
                            + "}", connectListenerClazz));
            return toClass(connectListenerClazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void registerPSConnectListener(ViaVersionPlugin plugin) {
        if (getPSConnectListener() != null) {
            try {
                Class<? extends Event> connectionOpenEvent = (Class<? extends Event>) Class.forName("protocolsupport.api.events.ConnectionOpenEvent");
                Bukkit.getPluginManager().registerEvent(connectionOpenEvent, new Listener() {
                }, EventPriority.HIGH, new EventExecutor() {
                    @Override
                    public void execute(Listener listener, Event event) throws EventException {
                        try {
                            Object connection = event.getClass().getMethod("getConnection").invoke(event);
                            Object connectListener = getPSConnectListener().getConstructor(connection.getClass()).newInstance(connection);
                            Method addConnectListener = connection.getClass().getMethod("addPacketListener", Class.forName("protocolsupport.api.Connection$PacketListener"));
                            addConnectListener.invoke(connection, connectListener);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Class getPSConnectListener() {
        return psConnectListener;
    }

    public static String getOldPSPackage() {
        if (psPackage == null) {
            try {
                Class.forName("protocolsupport.protocol.core.IPacketDecoder");
                psPackage = "protocolsupport.protocol.core";
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("protocolsupport.protocol.pipeline.IPacketDecoder");
                    psPackage = "protocolsupport.protocol.pipeline";
                } catch (ClassNotFoundException e1) {
                    psPackage = "unknown";
                }
            }
        }
        return psPackage;
    }

    public static boolean isMultiplatformPS() {
        try {
            Class.forName("protocolsupport.zplatform.impl.spigot.network.pipeline.SpigotPacketEncoder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean shouldUseNewHandshakeVersionMethod() {
        try {
            NMSUtil.nms(
                    "PacketHandshakingInSetProtocol",
                    "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol"
            ).getMethod("getProtocolVersion");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static Class<?> toClass(CtClass ctClass) throws CannotCompileException {
        return useModules ? ctClass.toClass(HandlerConstructor.class) : ctClass.toClass(HandlerConstructor.class.getClassLoader());
    }

    private static boolean hasModuleMethod() {
        try {
            Class.class.getDeclaredMethod("getModule");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
