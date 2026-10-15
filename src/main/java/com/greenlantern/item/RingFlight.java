package com.greenlantern.item;

import com.greenlantern.registry.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Ring-powered flight.
 *
 * While any ring in a player's inventory has flight toggled on (and charge left), the player is
 * granted creative-style flight (double-tap jump to take off). It drains one charge every two seconds
 * while actually flying (every four with the full suit). When the ring is dropped, switched off or
 * runs dry, the flight is revoked and the player gets a few seconds of slow falling so they land softly.
 *
 * We remember "this flight came from the ring" with an entity command tag, which is saved with the
 * player - so if someone logs out in mid-air and comes back without the ring, they don't keep flying.
 */
public final class RingFlight {
	private RingFlight() {}

	private static final String TAG = "greenlantern_flight";
	private static final float FLY_SPEED = 0.09f;       // vanilla creative flight is 0.05
	private static final float DEFAULT_FLY_SPEED = 0.05f;

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				tick(player);
			}
		});
	}

	private static void tick(ServerPlayerEntity player) {
		if (player.isSpectator()) {
			return;
		}

		ItemStack ring = findActiveRing(player);
		boolean granted = player.getCommandTags().contains(TAG);
		PlayerAbilities abilities = player.getAbilities();

		if (ring != null) {
			if (!granted) {
				player.addCommandTag(TAG);
			}
			if (!abilities.allowFlying || Math.abs(abilities.getFlySpeed() - FLY_SPEED) > 1.0e-4f) {
				abilities.allowFlying = true;
				abilities.setFlySpeed(FLY_SPEED);
				player.sendAbilitiesUpdate();
			}

			if (abilities.flying) {
				ServerWorld world = player.getServerWorld();
				if (player.age % 3 == 0) {
					world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
							player.getX(), player.getY() + 0.3, player.getZ(), 1, 0.2, 0.2, 0.2, 0.0);
				}
				if (!player.isCreative()) {
					int interval = PowerRingItem.hasFullSuit(player) ? 80 : 40;
					if (player.age % interval == 0) {
						int left = PowerRingItem.getCharge(ring) - 1;
						PowerRingItem.setCharge(ring, left);
						if (left <= 0) {
							ring.set(ModComponents.FLIGHT, false);
							player.sendMessage(Text.translatable("message.greenlantern.depleted").formatted(Formatting.RED), true);
						}
					}
				}
			}
		} else if (granted) {
			player.removeCommandTag(TAG);
			abilities.setFlySpeed(DEFAULT_FLY_SPEED);
			if (!player.isCreative()) {
				boolean airborne = !player.isOnGround();
				abilities.allowFlying = false;
				abilities.flying = false;
				if (airborne) {
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0, false, false));
				}
			}
			player.sendAbilitiesUpdate();
		}
	}

	private static ItemStack findActiveRing(ServerPlayerEntity player) {
		PlayerInventory inventory = player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.getItem() instanceof PowerRingItem
					&& stack.getOrDefault(ModComponents.FLIGHT, false)
					&& (player.isCreative() || PowerRingItem.getCharge(stack) > 0)) {
				return stack;
			}
		}
		return null;
	}
}
