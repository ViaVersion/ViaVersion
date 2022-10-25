/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.classgenerator.generated.HandlerSupplier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;

//TODO maybe clean this up a bit 👀
public final class ClassGenerator {
    private static final boolean useModules = hasModuleMethod();
    private static Class psConnectListener;

    public static void generate() {
        if (ViaVersionPlugin.getInstance().isProtocolSupport() && isMultiplatformPS()) {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new LoaderClassPath(Bukkit.class.getClassLoader()));
            for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                pool.insertClassPath(new LoaderClassPath(p.getClass().getClassLoader()));
            }

            Via.getPlatform().getLogger().info("Generating ProtocolSupport compatibility connect listener...");
            psConnectListener = makePSConnectListener(pool);
        }
    }

    private static Class makePSConnectListener(ClassPool pool) {
        HandshakeProtocolType type = handshakeVersionMethod();
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
                            + "        int protoVersion = packet." + type.methodName() + "();\n"
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
        if (psConnectListener != null) {
            try {
                Class<? extends Event> connectionOpenEvent = (Class<? extends Event>) Class.forName("protocolsupport.api.events.ConnectionOpenEvent");
                Bukkit.getPluginManager().registerEvent(connectionOpenEvent, new Listener() {
                }, EventPriority.HIGH, new EventExecutor() {
                    @Override
                    public void execute(@NonNull Listener listener, @NonNull Event event) throws EventException {
                        try {
                            Object connection = event.getClass().getMethod("getConnection").invoke(event);
                            Object connectListener = psConnectListener.getConstructor(connection.getClass()).newInstance(connection);
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

    public static boolean isMultiplatformPS() {
        try {
            Class.forName("protocolsupport.zplatform.impl.spigot.network.pipeline.SpigotPacketEncoder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static HandshakeProtocolType handshakeVersionMethod() {
        Class<?> clazz = null;
        // Check for the mapped method
        try {
            clazz = NMSUtil.nms(
                    "PacketHandshakingInSetProtocol",
                    "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol"
            );
            clazz.getMethod("getProtocolVersion");
            return HandshakeProtocolType.MAPPED;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException ignored) {
        }

        // Check for obfusacted b/c methods
        try {
            if (clazz.getMethod("b").getReturnType() == int.class) {
                return HandshakeProtocolType.OBFUSCATED_OLD;
            } else if (clazz.getMethod("c").getReturnType() == int.class) {
                return HandshakeProtocolType.OBFUSCATED_NEW;
            }
            throw new UnsupportedOperationException("Protocol version method not found in " + clazz.getSimpleName());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    private static Class<?> toClass(CtClass ctClass) throws CannotCompileException {
        return useModules ? ctClass.toClass(HandlerSupplier.class) : ctClass.toClass(HandlerSupplier.class.getClassLoader());
    }

    private static boolean hasModuleMethod() {
        try {
            Class.class.getDeclaredMethod("getModule");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private enum HandshakeProtocolType {

        MAPPED("getProtocolVersion"),
        OBFUSCATED_OLD("b"),
        OBFUSCATED_NEW("c");

        private final String methodName;

        HandshakeProtocolType(String methodName) {
            this.methodName = methodName;
        }

        public String methodName() {
            return methodName;
        }
    }
}
