/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CommandBlockProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CompressionProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MainHandProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.ClientWorld1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import com.viaversion.viaversion.util.ComponentUtil;

public class PlayerPacketRewriter1_9 {
    public static void register(Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); // 0 - Chat Message (json)
                map(Types.BYTE); // 1 - Chat Position

                handler(wrapper -> {
                    JsonObject obj = (JsonObject) wrapper.get(Types.COMPONENT, 0);
                    if (obj.get("translate") != null && obj.get("translate").getAsString().equals("gameMode.changed")) {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String gameMode = tracker.getGameMode().text();

                        JsonObject gameModeObject = new JsonObject();
                        gameModeObject.addProperty("text", gameMode);
                        gameModeObject.addProperty("color", "gray");
                        gameModeObject.addProperty("italic", true);

                        JsonArray array = new JsonArray();
                        array.add(gameModeObject);

                        obj.add("with", array);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.TAB_LIST, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); // 0 - Header
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); // 1 - Footer
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.DISCONNECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); // 0 - Reason
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SET_TITLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Action
                // We only handle if the title or subtitle is set then just write through.
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 0 || action == 1) {
                        Protocol1_8To1_9.STRING_TO_JSON.write(wrapper, wrapper.read(Types.STRING));
                    }
                });
                // Everything else is handled.
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); // 0 - Player X
                map(Types.DOUBLE); // 1 - Player Y
                map(Types.DOUBLE); // 2 - Player Z

                map(Types.FLOAT); // 3 - Player Yaw
                map(Types.FLOAT); // 4 - Player Pitch

                map(Types.BYTE); // 5 - Player Flags

                create(Types.VAR_INT, 0); // 6 - Teleport ID was added
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.SET_PLAYER_TEAM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Team Name
                map(Types.BYTE); // 1 - Mode
                handler(wrapper -> {
                    byte mode = wrapper.get(Types.BYTE, 0); // Mode
                    if (mode == 0 || mode == 2) {
                        wrapper.passthrough(Types.STRING); // Display Name
                        wrapper.passthrough(Types.STRING); // Prefix
                        wrapper.passthrough(Types.STRING); // Suffix

                        wrapper.passthrough(Types.BYTE); // Friendly Fire

                        wrapper.passthrough(Types.STRING); // Name tag visibility

                        wrapper.write(Types.STRING, Via.getConfig().isPreventCollision() ? "never" : "");

                        wrapper.passthrough(Types.BYTE); // Colour
                    }

                    if (mode == 0 || mode == 3 || mode == 4) {
                        String[] players = wrapper.passthrough(Types.STRING_ARRAY); // Players
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String myName = wrapper.user().getProtocolInfo().getUsername();
                        String teamName = wrapper.get(Types.STRING, 0);
                        for (String player : players) {
                            if (entityTracker.isAutoTeam() && player.equalsIgnoreCase(myName)) {
                                if (mode == 4) {
                                    // since removing add to auto team
                                    // Workaround for packet order issue
                                    wrapper.send(Protocol1_8To1_9.class);
                                    wrapper.cancel();
                                    entityTracker.sendTeamPacket(true, true);
                                    entityTracker.setCurrentTeam("viaversion");
                                } else {
                                    // since adding remove from auto team
                                    entityTracker.sendTeamPacket(false, true);
                                    entityTracker.setCurrentTeam(teamName);
                                }
                            }
                        }
                    }

                    if (mode == 1) { // Remove team
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String teamName = wrapper.get(Types.STRING, 0);
                        if (entityTracker.isAutoTeam()
                            && teamName.equals(entityTracker.getCurrentTeam())) {
                            // team was removed
                            // Workaround for packet order issue
                            wrapper.send(Protocol1_8To1_9.class);
                            wrapper.cancel();
                            entityTracker.sendTeamPacket(true, true);
                            entityTracker.setCurrentTeam("viaversion");
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Player ID
                // Parse this info
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityId, EntityTypes1_9.EntityType.PLAYER);
                    tracker.setClientEntityId(entityId);
                });
                map(Types.UNSIGNED_BYTE); // 1 - Player Gamemode
                map(Types.BYTE); // 2 - Player Dimension
                map(Types.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Types.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Types.STRING); // 5 - Level Type
                map(Types.BOOLEAN); // 6 - Reduced Debug info

                handler(wrapper -> {
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    short gamemodeId = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    gamemodeId &= -9; // remove the hardcore mode flag
                    tracker.setGameMode(GameMode.getById(gamemodeId)); //Set player gamemode
                });

                // Track player's dimension
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_8To1_9.class);
                    int dimensionId = wrapper.get(Types.BYTE, 0);
                    clientWorld.setEnvironment(dimensionId);
                });

                // Fake their op status
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                });

                // Scoreboard will be cleared when join game is received
                handler(wrapper -> {
                    EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (Via.getConfig().isAutoTeam()) {
                        entityTracker.setAutoTeam(true);
                        // Workaround for packet order issue
                        wrapper.send(Protocol1_8To1_9.class);
                        wrapper.cancel();
                        entityTracker.sendTeamPacket(true, true);
                        entityTracker.setCurrentTeam("viaversion");
                    } else {
                        entityTracker.setAutoTeam(false);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Action
                map(Types.VAR_INT); // 1 - Player Count

                // Due to this being a complex data structure we just use a handler.
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    int count = wrapper.get(Types.VAR_INT, 1);
                    for (int i = 0; i < count; i++) {
                        wrapper.passthrough(Types.UUID); // Player UUID
                        if (action == 0) { // add player
                            wrapper.passthrough(Types.STRING); // Player Name
                            wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
                            wrapper.passthrough(Types.VAR_INT); // gamemode
                            wrapper.passthrough(Types.VAR_INT); // ping
                            String displayName = wrapper.read(Types.OPTIONAL_STRING);
                            wrapper.write(Types.OPTIONAL_COMPONENT, displayName != null ?
                                Protocol1_8To1_9.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        } else if ((action == 1) || (action == 2)) { // update gamemode || update latency
                            wrapper.passthrough(Types.VAR_INT);
                        } else if (action == 3) { // update display name
                            String displayName = wrapper.read(Types.OPTIONAL_STRING);
                            wrapper.write(Types.OPTIONAL_COMPONENT, displayName != null ?
                                Protocol1_8To1_9.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Channel Name
                handlerSoftFail(wrapper -> {
                    final String name = wrapper.get(Types.STRING, 0);
                    if (name.equals("MC|BOpen")) {
                        wrapper.write(Types.VAR_INT, 0);
                    } else if (name.equals("MC|TrList")) {
                        protocol.getItemRewriter().handleTradeList(wrapper);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Dimension
                map(Types.UNSIGNED_BYTE); // 1 - Difficulty
                map(Types.UNSIGNED_BYTE); // 2 - GameMode
                map(Types.STRING); // 3 - Level Type

                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    // Fake permissions to get Commandblocks working
                    provider.sendPermission(wrapper.user());

                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    int gamemode = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    tracker.setGameMode(GameMode.getById(gamemode));

                    ClientWorld1_9 clientWorld = wrapper.user().getClientWorld(Protocol1_8To1_9.class);
                    int dimensionId = wrapper.get(Types.INT, 0);

                    // Track player's dimension
                    if (clientWorld.setEnvironment(dimensionId)) {
                        tracker.clearEntities();

                        clientWorld.getLoadedChunks().clear();
                        provider.unloadChunks(wrapper.user());
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); //0 - Reason
                map(Types.FLOAT); //1 - Value

                handler(wrapper -> {
                    short reason = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    if (reason == 3) { //Change gamemode
                        int gamemode = wrapper.get(Types.FLOAT, 0).intValue();
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        tracker.setGameMode(GameMode.getById(gamemode));
                    } else if (reason == 4) { //Open credits screen
                        wrapper.set(Types.FLOAT, 0, 1F);
                    }
                });
            }
        });

        /* Removed packets */
        protocol.registerClientbound(ClientboundPackets1_8.SET_COMPRESSION, null, wrapper -> {
            wrapper.cancel();

            int threshold = wrapper.read(Types.VAR_INT);
            wrapper.user().getProtocolInfo().setCompressionEnabled(threshold >= 0);

            CompressionProvider provider = Via.getManager().getProviders().get(CompressionProvider.class);
            provider.handlePlayCompression(wrapper.user(), threshold);
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.COMMAND_SUGGESTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Requested Command
                read(Types.BOOLEAN); // 1 - Is Command Block
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - locale
                map(Types.BYTE); // 1 - View Distance
                map(Types.VAR_INT, Types.BYTE); // 2 - Chat Mode
                map(Types.BOOLEAN); // 3 - If Chat Colours on
                map(Types.UNSIGNED_BYTE); // 4 - Skin Parts

                handler(wrapper -> {
                    int hand = wrapper.read(Types.VAR_INT);

                    // Add 0x80 if left-handed
                    if (Via.getConfig().isLeftHandedHandling() && hand == 0) {
                        wrapper.set(Types.UNSIGNED_BYTE, 0, (short) (wrapper.get(Types.UNSIGNED_BYTE, 0).intValue() | 0x80));
                    }
                    wrapper.sendToServer(Protocol1_8To1_9.class);
                    wrapper.cancel();
                    Via.getManager().getProviders().get(MainHandProvider.class).setMainHand(wrapper.user(), hand);
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.SWING, new PacketHandlers() {
            @Override
            public void register() {
                read(Types.VAR_INT); // 0 - Hand
            }
        });

        protocol.cancelServerbound(ServerboundPackets1_9.ACCEPT_TELEPORTATION);
        protocol.cancelServerbound(ServerboundPackets1_9.MOVE_VEHICLE);
        protocol.cancelServerbound(ServerboundPackets1_9.PADDLE_BOAT);

        protocol.registerServerbound(ServerboundPackets1_9.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Channel Name
                handler(wrapper -> {
                    String name = wrapper.get(Types.STRING, 0);
                    if (name.equals("MC|BSign")) {
                        Item item = wrapper.passthrough(Types.ITEM1_8);
                        if (item != null) {
                            item.setIdentifier(387); // Written Book
                            CompoundTag tag = item.tag();
                            ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
                            if (pages == null) {
                                return;
                            }

                            for (int i = 0; i < pages.size(); i++) {
                                final StringTag pageTag = pages.get(i);
                                final String value = pageTag.getValue();

                                pageTag.setValue(ComponentUtil.plainToJson(value).toString());
                            }
                        }
                    }
                    if (name.equals("MC|AutoCmd")) {
                        wrapper.set(Types.STRING, 0, "MC|AdvCdm");
                        wrapper.write(Types.BYTE, (byte) 0);
                        wrapper.passthrough(Types.INT); // X
                        wrapper.passthrough(Types.INT); // Y
                        wrapper.passthrough(Types.INT); // Z
                        wrapper.passthrough(Types.STRING); // Command
                        wrapper.passthrough(Types.BOOLEAN); // Flag
                        wrapper.clearInputBuffer();
                    }
                    if (name.equals("MC|AdvCmd")) {
                        wrapper.set(Types.STRING, 0, "MC|AdvCdm");
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Action ID
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 2) {
                        // cancel any blocking >.>
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        if (tracker.isBlocking()) {
                            if (!Via.getConfig().isShowShieldWhenSwordInHand()) {
                                tracker.setSecondHand(null);
                            }
                            tracker.setBlocking(false);
                        }
                    }
                });
            }
        });

        final PacketHandler onGroundHandler = wrapper -> {
            final MovementTracker tracker = wrapper.user().get(MovementTracker.class);
            tracker.incrementIdlePacket();
            tracker.setGround(wrapper.get(Types.BOOLEAN, 0));
        };
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_POS, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); // 0 - X
                map(Types.DOUBLE); // 1 - Y
                map(Types.DOUBLE); // 2 - Z
                map(Types.BOOLEAN); // 3 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_POS_ROT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); // 0 - X
                map(Types.DOUBLE); // 1 - Y
                map(Types.DOUBLE); // 2 - Z
                map(Types.FLOAT); // 3 - Yaw
                map(Types.FLOAT); // 4 - Pitch
                map(Types.BOOLEAN); // 5 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_ROT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.FLOAT); // 0 - Yaw
                map(Types.FLOAT); // 1 - Pitch
                map(Types.BOOLEAN); // 2 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_STATUS_ONLY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BOOLEAN); // 0 - Ground
                handler(onGroundHandler);
            }
        });
    }
}
