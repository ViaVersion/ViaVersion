/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.chat.ChatRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.chat.GameMode;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.CompressionProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MainHandProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.ClientChunks;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.MovementTracker;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.logging.Level;

public class PlayerPackets {
    public static void register(Protocol1_9To1_8 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.CHAT_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING, Protocol1_9To1_8.STRING_TO_JSON); // 0 - Chat Message (json)
                map(Type.BYTE); // 1 - Chat Position

                handler(wrapper -> {
                    try {
                        JsonObject obj = (JsonObject) wrapper.get(Type.COMPONENT, 0);
                        ChatRewriter.toClient(obj, wrapper.user());
                    } catch (Exception e) {
                        Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to transform chat component", e);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.TAB_LIST, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING, Protocol1_9To1_8.STRING_TO_JSON); // 0 - Header
                map(Type.STRING, Protocol1_9To1_8.STRING_TO_JSON); // 1 - Footer
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.DISCONNECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING, Protocol1_9To1_8.STRING_TO_JSON); // 0 - Reason
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.TITLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Action
                // We only handle if the title or subtitle is set then just write through.
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 0 || action == 1) {
                        Protocol1_9To1_8.STRING_TO_JSON.write(wrapper, wrapper.read(Type.STRING));
                    }
                });
                // Everything else is handled.
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.DOUBLE); // 0 - Player X
                map(Type.DOUBLE); // 1 - Player Y
                map(Type.DOUBLE); // 2 - Player Z

                map(Type.FLOAT); // 3 - Player Yaw
                map(Type.FLOAT); // 4 - Player Pitch

                map(Type.BYTE); // 5 - Player Flags

                handler(wrapper -> {
                    wrapper.write(Type.VAR_INT, 0); // 6 - Teleport ID was added
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.TEAMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Team Name
                map(Type.BYTE); // 1 - Mode
                handler(wrapper -> {
                    byte mode = wrapper.get(Type.BYTE, 0); // Mode
                    if (mode == 0 || mode == 2) {
                        wrapper.passthrough(Type.STRING); // Display Name
                        wrapper.passthrough(Type.STRING); // Prefix
                        wrapper.passthrough(Type.STRING); // Suffix

                        wrapper.passthrough(Type.BYTE); // Friendly Fire

                        wrapper.passthrough(Type.STRING); // Name tag visibility

                        wrapper.write(Type.STRING, Via.getConfig().isPreventCollision() ? "never" : "");

                        wrapper.passthrough(Type.BYTE); // Colour
                    }

                    if (mode == 0 || mode == 3 || mode == 4) {
                        String[] players = wrapper.passthrough(Type.STRING_ARRAY); // Players
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        String myName = wrapper.user().getProtocolInfo().getUsername();
                        String teamName = wrapper.get(Type.STRING, 0);
                        for (String player : players) {
                            if (entityTracker.isAutoTeam() && player.equalsIgnoreCase(myName)) {
                                if (mode == 4) {
                                    // since removing add to auto team
                                    // Workaround for packet order issue
                                    wrapper.send(Protocol1_9To1_8.class);
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
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        String teamName = wrapper.get(Type.STRING, 0);
                        if (entityTracker.isAutoTeam()
                                && teamName.equals(entityTracker.getCurrentTeam())) {
                            // team was removed
                            // Workaround for packet order issue
                            wrapper.send(Protocol1_9To1_8.class);
                            wrapper.cancel();
                            entityTracker.sendTeamPacket(true, true);
                            entityTracker.setCurrentTeam("viaversion");
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Player ID
                // Parse this info
                handler(wrapper -> {
                    int entityId = wrapper.get(Type.INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addEntity(entityId, EntityTypes1_10.EntityType.PLAYER);
                    tracker.setClientEntityId(entityId);
                });
                map(Type.UNSIGNED_BYTE); // 1 - Player Gamemode
                map(Type.BYTE); // 2 - Player Dimension
                map(Type.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Type.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Type.STRING); // 5 - Level Type
                map(Type.BOOLEAN); // 6 - Reduced Debug info

                handler(wrapper -> {
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.setGameMode(GameMode.getById(wrapper.get(Type.UNSIGNED_BYTE, 0))); //Set player gamemode
                });

                // Track player's dimension
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.BYTE, 0);
                    clientWorld.setEnvironment(dimensionId);
                });

                // Fake their op status
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                });

                // Scoreboard will be cleared when join game is received
                handler(wrapper -> {
                    EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    if (Via.getConfig().isAutoTeam()) {
                        entityTracker.setAutoTeam(true);
                        // Workaround for packet order issue
                        wrapper.send(Protocol1_9To1_8.class);
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
                map(Type.VAR_INT); // 0 - Action
                map(Type.VAR_INT); // 1 - Player Count

                // Due to this being a complex data structure we just use a handler.
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    int count = wrapper.get(Type.VAR_INT, 1);
                    for (int i = 0; i < count; i++) {
                        wrapper.passthrough(Type.UUID); // Player UUID
                        if (action == 0) { // add player
                            wrapper.passthrough(Type.STRING); // Player Name

                            int properties = wrapper.passthrough(Type.VAR_INT);

                            // loop through properties
                            for (int j = 0; j < properties; j++) {
                                wrapper.passthrough(Type.STRING); // name
                                wrapper.passthrough(Type.STRING); // value
                                wrapper.passthrough(Type.OPTIONAL_STRING); // signature
                            }

                            wrapper.passthrough(Type.VAR_INT); // gamemode
                            wrapper.passthrough(Type.VAR_INT); // ping
                            String displayName = wrapper.read(Type.OPTIONAL_STRING);
                            wrapper.write(Type.OPTIONAL_COMPONENT, displayName != null ?
                                    Protocol1_9To1_8.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        } else if ((action == 1) || (action == 2)) { // update gamemode || update latency
                            wrapper.passthrough(Type.VAR_INT);
                        } else if (action == 3) { // update display name
                            String displayName = wrapper.read(Type.OPTIONAL_STRING);
                            wrapper.write(Type.OPTIONAL_COMPONENT, displayName != null ?
                                    Protocol1_9To1_8.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Channel Name
                handlerSoftFail(wrapper -> {
                    String name = wrapper.get(Type.STRING, 0);
                    if (name.equals("MC|BOpen")) {
                        wrapper.write(Type.VAR_INT, 0);
                    } else if (name.equals("MC|TrList")) {
                        wrapper.passthrough(Type.INT); // ID

                        Short size = wrapper.passthrough(Type.UNSIGNED_BYTE);

                        for (int i = 0; i < size; ++i) {
                            Item item1 = wrapper.passthrough(Type.ITEM1_8);
                            ItemRewriter.toClient(item1);

                            Item item2 = wrapper.passthrough(Type.ITEM1_8);
                            ItemRewriter.toClient(item2);

                            boolean present = wrapper.passthrough(Type.BOOLEAN);

                            if (present) {
                                Item item3 = wrapper.passthrough(Type.ITEM1_8);
                                ItemRewriter.toClient(item3);
                            }

                            wrapper.passthrough(Type.BOOLEAN);

                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.INT);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Dimension
                map(Type.UNSIGNED_BYTE); // 1 - Difficulty
                map(Type.UNSIGNED_BYTE); // 2 - GameMode
                map(Type.STRING); // 3 - Level Type

                // Track player's dimension
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });

                handler(wrapper -> {
                    // Client unloads chunks on respawn
                    wrapper.user().get(ClientChunks.class).getLoadedChunks().clear();

                    int gamemode = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.setGameMode(GameMode.getById(gamemode));
                });

                // Fake permissions to get Commandblocks working
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                    provider.unloadChunks(wrapper.user());
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); //0 - Reason
                map(Type.FLOAT); //1 - Value

                handler(wrapper -> {
                    short reason = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (reason == 3) { //Change gamemode
                        int gamemode = wrapper.get(Type.FLOAT, 0).intValue();
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        tracker.setGameMode(GameMode.getById(gamemode));
                    } else if (reason == 4) { //Open credits screen
                        wrapper.set(Type.FLOAT, 0, 1F);
                    }
                });
            }
        });

        /* Removed packets */
        protocol.registerClientbound(ClientboundPackets1_8.SET_COMPRESSION, null, wrapper -> {
            wrapper.cancel();
            CompressionProvider provider = Via.getManager().getProviders().get(CompressionProvider.class);

            provider.handlePlayCompression(wrapper.user(), wrapper.read(Type.VAR_INT));
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.TAB_COMPLETE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Requested Command
                read(Type.BOOLEAN); // 1 - Is Command Block
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_SETTINGS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - locale
                map(Type.BYTE); // 1 - View Distance
                map(Type.VAR_INT, Type.BYTE); // 2 - Chat Mode
                map(Type.BOOLEAN); // 3 - If Chat Colours on
                map(Type.UNSIGNED_BYTE); // 4 - Skin Parts

                handler(wrapper -> {
                    int hand = wrapper.read(Type.VAR_INT);

                    // Add 0x80 if left-handed
                    if (Via.getConfig().isLeftHandedHandling() && hand == 0) {
                        wrapper.set(Type.UNSIGNED_BYTE, 0, (short) (wrapper.get(Type.UNSIGNED_BYTE, 0).intValue() | 0x80));
                    }
                    wrapper.sendToServer(Protocol1_9To1_8.class);
                    wrapper.cancel();
                    Via.getManager().getProviders().get(MainHandProvider.class).setMainHand(wrapper.user(), hand);
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                read(Type.VAR_INT); // 0 - Hand
            }
        });

        protocol.cancelServerbound(ServerboundPackets1_9.TELEPORT_CONFIRM);
        protocol.cancelServerbound(ServerboundPackets1_9.VEHICLE_MOVE);
        protocol.cancelServerbound(ServerboundPackets1_9.STEER_BOAT);

        protocol.registerServerbound(ServerboundPackets1_9.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Channel Name
                handlerSoftFail(wrapper -> {
                    String name = wrapper.get(Type.STRING, 0);
                    if (name.equals("MC|BSign")) {
                        Item item = wrapper.passthrough(Type.ITEM1_8);
                        if (item != null) {
                            item.setIdentifier(387); // Written Book
                            ItemRewriter.rewriteBookToServer(item);
                        }
                    }
                    if (name.equals("MC|AutoCmd")) {
                        wrapper.set(Type.STRING, 0, "MC|AdvCdm");
                        wrapper.write(Type.BYTE, (byte) 0);
                        wrapper.passthrough(Type.INT); // X
                        wrapper.passthrough(Type.INT); // Y
                        wrapper.passthrough(Type.INT); // Z
                        wrapper.passthrough(Type.STRING); // Command
                        wrapper.passthrough(Type.BOOLEAN); // Flag
                        wrapper.clearInputBuffer();
                    }
                    if (name.equals("MC|AdvCmd")) {
                        wrapper.set(Type.STRING, 0, "MC|AdvCdm");
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_STATUS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Action ID
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 2) {
                        // cancel any blocking >.>
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
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
            tracker.setGround(wrapper.get(Type.BOOLEAN, 0));
        };
        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.DOUBLE); // 0 - X
                map(Type.DOUBLE); // 1 - Y
                map(Type.DOUBLE); // 2 - Z
                map(Type.BOOLEAN); // 3 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_POSITION_AND_ROTATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.DOUBLE); // 0 - X
                map(Type.DOUBLE); // 1 - Y
                map(Type.DOUBLE); // 2 - Z
                map(Type.FLOAT); // 3 - Yaw
                map(Type.FLOAT); // 4 - Pitch
                map(Type.BOOLEAN); // 5 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_ROTATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.FLOAT); // 0 - Yaw
                map(Type.FLOAT); // 1 - Pitch
                map(Type.BOOLEAN); // 2 - Ground
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_MOVEMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BOOLEAN); // 0 - Ground
                handler(onGroundHandler);
            }
        });
    }
}