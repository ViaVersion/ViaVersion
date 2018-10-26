package us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.PlayerPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_14To1_13_2 extends Protocol {
	@Override
	protected void registerPackets() {
		InventoryPackets.register(this);
		EntityPackets.register(this);
		WorldPackets.register(this);
		PlayerPackets.register(this);

		// Sound Effect
		registerOutgoing(State.PLAY, 0x4D, 0x4D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT); // Sound Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						wrapper.set(Type.VAR_INT, 0, getNewSoundId(wrapper.get(Type.VAR_INT, 0)));
					}
				});
			}
		});
		registerOutgoing(State.PLAY, 0x4E, 0x4F);
		registerOutgoing(State.PLAY, 0x4F, 0x50);
		registerOutgoing(State.PLAY, 0x50, 0x51);
		registerOutgoing(State.PLAY, 0x51, 0x52);
		registerOutgoing(State.PLAY, 0x52, 0x53);
		registerOutgoing(State.PLAY, 0x53, 0x54);

		registerOutgoing(State.PLAY, 0x55, 0x56, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						int blockTagsSize = wrapper.passthrough(Type.VAR_INT); // block tags
						for (int i = 0; i < blockTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							Integer[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
							for (int j = 0; j < blockIds.length; j++) {
								blockIds[j] = getNewBlockId(blockIds[j]);
							}
						}
						int itemTagsSize = wrapper.passthrough(Type.VAR_INT); // item tags
						for (int i = 0; i < itemTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							Integer[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
							for (int j = 0; j < itemIds.length; j++) {
								itemIds[j] = InventoryPackets.getNewItemId(itemIds[j]);
							}
						}
						int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
						for (int i = 0; i < fluidTagsSize; i++) {
							wrapper.passthrough(Type.STRING);
							wrapper.passthrough(Type.VAR_INT_ARRAY);
						}
						wrapper.write(Type.VAR_INT, 0);  // new unknown tags
					}
				});
			}
		});
	}

	public static int getNewSoundId(int id) {
		// AUTO GENERATED - todo compact this?
		if (id < 15) return id + 0; // 15 = block.beacon.activate
		if (id < 19) return id + 29; // 19 = block.brewing_stand.brew
		if (id < 25) return id + 42; // 25 = block.chest.close
		if (id < 28) return id + 54; // 28 = block.chorus_flower.death
		if (id < 30) return id + 59; // 30 = block.wool.break
		if (id < 35) return id + 60; // 35 = block.comparator.click
		if (id < 41) return id + 64; // 41 = block.dispenser.dispense
		if (id < 44) return id + 80; // 44 = block.enchantment_table.use
		if (id < 45) return id + 114; // 45 = block.end_gateway.spawn
		if (id < 46) return id + 136; // 46 = block.end_portal.spawn
		if (id < 47) return id + 137; // 47 = block.end_portal_frame.fill
		if (id < 48) return id + 135; // 48 = block.ender_chest.close
		if (id < 50) return id + 111; // 50 = block.fence_gate.close
		if (id < 52) return id + 144; // 52 = block.fire.ambient
		if (id < 54) return id + 153; // 54 = block.furnace.fire_crackle
		if (id < 55) return id + 155; // 55 = block.glass.break
		if (id < 80) return id + 172; // 80 = block.iron_door.close
		if (id < 82) return id + 219; // 82 = block.iron_trapdoor.close
		if (id < 84) return id + 223; // 84 = block.ladder.break
		if (id < 92) return id + 230; // 92 = block.lever.click
		if (id < 93) return id + 232; // 93 = block.metal.break
		if (id < 97) return id + 248; // 97 = block.metal.step
		if (id < 98) return id + 250; // 98 = block.metal_pressure_plate.click_off
		if (id < 100) return id + 247; // 100 = block.note_block.basedrum
		if (id < 111) return id + 263; // 111 = block.piston.contract
		if (id < 113) return id + 325; // 113 = block.portal.ambient
		if (id < 116) return id + 349; // 116 = block.pumpkin.carve
		if (id < 117) return id + 356; // 117 = block.redstone_torch.burnout
		if (id < 118) return id + 373; // 118 = block.sand.break
		if (id < 123) return id + 377; // 123 = block.shulker_box.close
		if (id < 125) return id + 386; // 125 = block.slime_block.break
		if (id < 130) return id + 417; // 130 = block.snow.break
		if (id < 132) return id + 425; // 132 = block.snow.hit
		if (id < 135) return id + 429; // 135 = block.stone.break
		if (id < 136) return id + 439; // 136 = block.stone.fall
		if (id < 139) return id + 441; // 139 = block.stone.step
		if (id < 140) return id + 443; // 140 = block.stone_button.click_off
		if (id < 142) return id + 435; // 142 = block.stone_pressure_plate.click_off
		if (id < 144) return id + 438; // 144 = block.tripwire.attach
		if (id < 148) return id + 454; // 148 = block.water.ambient
		if (id < 149) return id + 490; // 149 = block.lily_pad.place
		if (id < 150) return id + 488; // 150 = block.wood.break
		if (id < 151) return id + 519; // 151 = block.wood.fall
		if (id < 154) return id + 521; // 154 = block.wood.step
		if (id < 155) return id + 523; // 155 = block.wooden_button.click_off
		if (id < 157) return id + 515; // 157 = block.wooden_pressure_plate.click_off
		if (id < 159) return id + 518; // 159 = block.wooden_door.close
		if (id < 163) return id + 506; // 163 = enchant.thorns.hit
		if (id < 164) return id + 424; // 164 = entity.armor_stand.break
		if (id < 171) return id + -141; // 171 = entity.bat.ambient
		if (id < 176) return id + -132; // 176 = entity.blaze.ambient
		if (id < 186) return id + -128; // 186 = entity.cat.ambient
		if (id < 192) return id + -113; // 192 = entity.chicken.ambient
		if (id < 197) return id + -110; // 197 = entity.cod.ambient
		if (id < 201) return id + -102; // 201 = entity.cow.ambient
		if (id < 209) return id + -96; // 209 = entity.dolphin.ambient
		if (id < 242) return id + -85; // 242 = entity.ender_dragon.ambient
		if (id < 244) return id + -81; // 244 = entity.ender_dragon.flap
		if (id < 248) return id + -80; // 248 = entity.dragon_fireball.explode
		if (id < 249) return id + -85; // 249 = entity.ender_eye.death
		if (id < 262) return id + -81; // 262 = entity.evoker.ambient
		if (id < 265) return id + -78; // 265 = entity.evoker.hurt
		if (id < 269) return id + -77; // 269 = entity.evoker_fangs.attack
		if (id < 270) return id + -82; // 270 = entity.experience_bottle.throw
		if (id < 272) return id + -78; // 272 = entity.firework_rocket.blast
		if (id < 280) return id + -75; // 280 = entity.fish.swim
		if (id < 281) return id + -73; // 281 = entity.generic.big_fall
		if (id < 298) return id + -71; // 298 = entity.guardian.ambient
		if (id < 306) return id + -46; // 306 = entity.horse.ambient
		if (id < 330) return id + -45; // 330 = entity.illusioner.ambient
		if (id < 337) return id + -38; // 337 = entity.iron_golem.attack
		if (id < 341) return id + -36; // 341 = entity.item.break
		if (id < 343) return id + -29; // 343 = entity.item_frame.add_item
		if (id < 348) return id + -36; // 348 = entity.leash_knot.break
		if (id < 350) return id + -26; // 350 = entity.lightning_bolt.impact
		if (id < 366) return id + -25; // 366 = entity.minecart.inside
		if (id < 373) return id + -18; // 373 = entity.painting.break
		if (id < 375) return id + 1; // 375 = entity.parrot.ambient
		if (id < 421) return id + 12; // 421 = entity.player.attack.crit
		if (id < 445) return id + 17; // 445 = entity.puffer_fish.ambient
		if (id < 452) return id + 20; // 452 = entity.rabbit.ambient
		if (id < 457) return id + 21; // 457 = entity.salmon.ambient
		if (id < 461) return id + 34; // 461 = entity.sheep.ambient
		if (id < 466) return id + 39; // 466 = entity.shulker.ambient
		if (id < 467) return id + 42; // 467 = entity.shulker.close
		if (id < 474) return id + 46; // 474 = entity.shulker_bullet.hit
		if (id < 476) return id + 37; // 476 = entity.silverfish.ambient
		if (id < 482) return id + 44; // 482 = entity.skeleton.hurt
		if (id < 485) return id + 52; // 485 = entity.skeleton_horse.ambient
		if (id < 493) return id + 41; // 493 = entity.slime.attack
		if (id < 498) return id + 44; // 498 = entity.magma_cube.death_small
		if (id < 505) return id + 49; // 505 = entity.snow_golem.ambient
		if (id < 509) return id + 52; // 509 = entity.snowball.throw
		if (id < 510) return id + 45; // 510 = entity.spider.ambient
		if (id < 520) return id + 54; // 520 = entity.stray.ambient
		if (id < 524) return id + 63; // 524 = entity.tnt.primed
		if (id < 525) return id + 64; // 525 = entity.tropical_fish.ambient
		if (id < 541) return id + 77; // 541 = entity.vex.ambient
		if (id < 554) return id + 83; // 554 = entity.witch.ambient
		if (id < 564) return id + 87; // 564 = entity.wither.spawn
		if (id < 565) return id + 91; // 565 = entity.wither_skeleton.ambient
		if (id < 569) return id + 86; // 569 = entity.wolf.ambient
		if (id < 578) return id + 87; // 578 = entity.zombie.ambient
		if (id < 585) return id + 100; // 585 = entity.zombie.hurt
		if (id < 587) return id + 103; // 587 = entity.zombie.step
		if (id < 588) return id + 107; // 588 = entity.zombie_horse.ambient
		if (id < 591) return id + 97; // 591 = entity.zombie_pigman.ambient
		if (id < 595) return id + 99; // 595 = entity.zombie_villager.ambient
		if (id < 601) return id + 100; // 601 = item.armor.equip_chain
		if (id < 609) return id + -586; // 609 = item.axe.strip
		if (id < 610) return id + -579; // 610 = item.bottle.empty
		if (id < 613) return id + -552; // 613 = item.bucket.empty
		if (id < 619) return id + -546; // 619 = item.chorus_fruit.teleport
		if (id < 620) return id + -530; // 620 = item.elytra.flying
		if (id < 621) return id + -463; // 621 = item.firecharge.use
		if (id < 622) return id + -425; // 622 = item.flintandsteel.use
		if (id < 623) return id + -414; // 623 = item.hoe.till
		if (id < 624) return id + -363; // 624 = item.shield.block
		if (id < 627) return id + -119; // 627 = item.totem.use
		if (id < 636) return id + -38; // 636 = music.creative
		if (id < 644) return id + -281; // 644 = music_disc.11
		if (id < 656) return id + -166; // 656 = ui.button.click
		if (id < 657) return id + -38; // 657 = ui.toast.challenge_complete
		if (id < 660) return id + -36; // 660 = weather.rain
		return id + -21;
	}

	public static int getNewBlockStateId(int id) {
		if (id < 1121) return id;
		else if (id < 3108) return id + 3;
		else if (id < 3278) return id + 163;
		else if (id < 3978) return id + 203;
		else if (id < 3984) return id + 207;
		else if (id < 3988) return id + 197;
		else if (id < 5284) return id + 203;
		else if (id < 7300) return id + 206;
		else if (id < 8591) return id + 212;
		else if (id < 8595) return id + 226;
		else return id + 2192;
	}

	public static int getNewBlockId(int id) {
		// AUTO GENERATED
		if (id < 128) return id + 0; // 128 = minecraft:brown_mushroom
		if (id < 151) return id + 3; // 151 = minecraft:oak_door
		if (id < 156) return id + 8; // 156 = minecraft:lever
		if (id < 208) return id + 13; // 208 = minecraft:infested_stone
		if (id < 214) return id + 17; // 214 = minecraft:stone_bricks
		if (id < 218) return id + 7; // 218 = minecraft:brown_mushroom_block
		if (id < 278) return id + 13; // 278 = minecraft:potted_red_mushroom
		if (id < 435) return id + 16; // 435 = minecraft:sandstone_slab
		if (id < 594) return id + 17; // 594 = minecraft:void_air
		if (id < 597) return id + 20; // 597 = minecraft:structure_block
		return id + 59;
	}

	@Override
	public void init(UserConnection userConnection) {
		userConnection.put(new EntityTracker(userConnection));
		if (!userConnection.has(ClientWorld.class))
			userConnection.put(new ClientWorld(userConnection));

	}
}
