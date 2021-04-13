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
package us.myles.ViaVersion.bungee.platform;

import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.platform.ViaInjector;
import us.myles.ViaVersion.bungee.handlers.BungeeChannelInitializer;
import us.myles.ViaVersion.compatibility.FieldModifierAccessor;
import us.myles.ViaVersion.compatibility.JavaVersionIdentifier;
import us.myles.ViaVersion.compatibility.jre16.Jre16FieldModifierAccessor;
import us.myles.ViaVersion.compatibility.jre8.Jre8FieldModifierAccessor;
import us.myles.ViaVersion.compatibility.jre9.Jre9FieldModifierAccessor;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class BungeeViaInjector implements ViaInjector {

    private final FieldModifierAccessor fieldModifierAccessor;

    public BungeeViaInjector() {
        FieldModifierAccessor fieldModifierAccessor = null;
        try {
            if (JavaVersionIdentifier.IS_JAVA_16) {
                fieldModifierAccessor = new Jre16FieldModifierAccessor();
            } else if (JavaVersionIdentifier.IS_JAVA_9) {
                fieldModifierAccessor = new Jre9FieldModifierAccessor();
            }
        } catch (final ReflectiveOperationException outer) {
            try {
                fieldModifierAccessor = new Jre8FieldModifierAccessor();
                Via.getPlatform().getLogger().warning("Had to fall back to the Java 8 field modifier accessor.");
                outer.printStackTrace();
            } catch (final ReflectiveOperationException inner) {
                inner.addSuppressed(outer);
                throw new IllegalStateException("Cannot create a modifier accessor", inner);
            }
        }

        try {
          if (fieldModifierAccessor == null) {
              fieldModifierAccessor = new Jre8FieldModifierAccessor();
          }
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot create a modifier accessor", ex);
        }

        // Must be non-null by now.
        this.fieldModifierAccessor = fieldModifierAccessor;
    }

    @Override
    public void inject() throws Exception {
        try {
            Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
            Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
            field.setAccessible(true);

            // Remove the final modifier (unless removed by a fork)
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                this.fieldModifierAccessor.setModifiers(field, modifiers & ~Modifier.FINAL);
            }

            BungeeChannelInitializer newInit = new BungeeChannelInitializer((ChannelInitializer<Channel>) field.get(null));
            field.set(null, newInit);
        } catch (Exception e) {
            Via.getPlatform().getLogger().severe("Unable to inject ViaVersion, please post these details on our GitHub and ensure you're using a compatible server version.");
            throw e;
        }
    }

    @Override
    public void uninject() {
        Via.getPlatform().getLogger().severe("ViaVersion cannot remove itself from Bungee without a reboot!");
    }


    @Override
    public int getServerProtocolVersion() throws Exception {
        return getBungeeSupportedVersions().get(0);
    }

    @Override
    public IntSortedSet getServerProtocolVersions() throws Exception {
        return new IntLinkedOpenHashSet(getBungeeSupportedVersions());
    }

    private List<Integer> getBungeeSupportedVersions() throws Exception {
        return ReflectionUtil.getStatic(Class.forName("net.md_5.bungee.protocol.ProtocolConstants"), "SUPPORTED_VERSION_IDS", List.class);
    }

    @Override
    public String getEncoderName() {
        return "via-encoder";
    }

    @Override
    public String getDecoderName() {
        return "via-decoder";
    }

    private ChannelInitializer<Channel> getChannelInitializer() throws Exception {
        Class<?> pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils");
        Field field = pipelineUtils.getDeclaredField("SERVER_CHILD");
        field.setAccessible(true);
        return (ChannelInitializer<Channel>) field.get(null);
    }

    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();
        try {
            ChannelInitializer<Channel> initializer = getChannelInitializer();
            data.addProperty("currentInitializer", initializer.getClass().getName());
            if (initializer instanceof BungeeChannelInitializer) {
                data.addProperty("originalInitializer", ((BungeeChannelInitializer) initializer).getOriginal().getClass().getName());
            }
        } catch (Exception e) {
            // Ignored, not printed in the dump
        }
        return data;
    }
}
