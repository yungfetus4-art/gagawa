package com.greenlantern.item;

import com.greenlantern.block.ConstructBlock;
import com.greenlantern.registry.ModComponents;
import com.greenlantern.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * The Green Lantern Power Ring.
 *
 *  - Right-click:            cast the selected mode
 *  - Sneak + right-click:    cycle to the next mode
 *  - Right-click a Battery:  recharge every ring in your inventory
 *  - The ring also trickles charge back on its own; the full Lantern suit makes that faster
 *    and makes every cast 25% cheaper.
 */
public class PowerRingItem extends Item {
	public static final int MAX_CHARGE = 100;

	private static final int BRIDGE_LENGTH = 16;
	private static final double BOLT_RANGE = 48.0;
	private static final float BOLT_DAMAGE = 10.0f;
	private static final double SCAN_RADIUS = 24.0;

	/** Ticks per +1 charge while the ring sits in an inventory. */
	private static final int RECHARGE_INTERVAL = 60;
	private static final int RECHARGE_INTERVAL_SUIT = 20;

	public PowerRingItem(Settings settings) {
		super(settings);
	}

	// ------------------------------------------------------------------ state helpers

	public static int getCharge(ItemStack stack) {
		return stack.getOrDefault(ModComponents.CHARGE, 0);
	}

	public static void setCharge(ItemStack stack, int value) {
		stack.set(ModComponents.CHARGE, MathHelper.clamp(value, 0, MAX_CHARGE));
	}

	public static RingMode getMode(ItemStack stack) {
		return RingMode.byIndex(stack.getOrDefault(ModComponents.MODE, 0));
	}

	/** True if the player wears all four pieces of the Lantern suit. */
	public static boolean hasFullSuit(PlayerEntity player) {
		return player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.LANTERN_MASK)
				&& player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.LANTERN_JACKET)
				&& player.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.LANTERN_TROUSERS)
				&& player.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.LANTERN_BOOTS);
	}

	private static int costFor(PlayerEntity player, RingMode mode) {
		int base = mode.cost();
		if (base == 0) {
			return 0;
		}
		return hasFullSuit(player) ? Math.max(1, (int) Math.ceil(base * 0.75)) : base;
	}

	// ------------------------------------------------------------------ item behaviour

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		// Client side: just swing the arm. All the real work happens on the server.
		if (!(world instanceof ServerWorld serverWorld) || !(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.success(stack);
		}

		// Sneak + use = cycle mode.
		if (player.isSneaking()) {
			RingMode next = getMode(stack).next();
			stack.set(ModComponents.MODE, next.ordinal());
			serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
					SoundCategory.PLAYERS, 0.8f, 1.2f);
			player.sendMessage(Text.translatable("message.greenlantern.mode_selected", next.displayName())
					.formatted(Formatting.GREEN), true);
			player.getItemCooldownManager().set(this, 4);
			return TypedActionResult.success(stack);
		}

		RingMode mode = getMode(stack);

		if (mode == RingMode.FLIGHT) {
			return toggleFlight(serverWorld, player, stack);
		}

		int cost = costFor(player, mode);
		if (!player.isCreative() && getCharge(stack) < cost) {
			serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE,
					SoundCategory.PLAYERS, 0.8f, 1.2f);
			player.sendMessage(Text.translatable("message.greenlantern.depleted").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}

		boolean cast = switch (mode) {
			case BRIDGE -> castBridge(serverWorld, player);
			case WALL -> castWall(serverWorld, player);
			case SHELTER -> castShelter(serverWorld, player);
			case BOLT -> castBolt(serverWorld, player);
			case SHIELD -> castShield(serverWorld, player);
			case RESTORE -> castRestore(serverWorld, player);
			case SCAN -> castScan(serverWorld, player);
			case FLIGHT -> false; // handled above
		};

		if (!cast) {
			player.sendMessage(Text.translatable("message.greenlantern.nothing_happened").formatted(Formatting.GRAY), true);
			return TypedActionResult.fail(stack);
		}

		if (!player.isCreative()) {
			setCharge(stack, getCharge(stack) - cost);
		}
		player.getItemCooldownManager().set(this, mode == RingMode.BOLT ? 6 : 15);
		if (mode != RingMode.SCAN) { // scan sends its own, more useful message
			player.sendMessage(Text.translatable("message.greenlantern.cast", mode.displayName(), getCharge(stack))
					.formatted(Formatting.GREEN), true);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient() || !(entity instanceof PlayerEntity player)) {
			return;
		}
		int interval = hasFullSuit(player) ? RECHARGE_INTERVAL_SUIT : RECHARGE_INTERVAL;
		if (world.getTime() % interval == 0 && getCharge(stack) < MAX_CHARGE) {
			setCharge(stack, getCharge(stack) + 1);
		}
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return getCharge(stack) > 0;
	}

	// Charge bar under the item icon.
	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return Math.round(13.0f * getCharge(stack) / (float) MAX_CHARGE);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return 0x33FF66;
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.greenlantern.charge", getCharge(stack), MAX_CHARGE).formatted(Formatting.GREEN));
		tooltip.add(Text.translatable("tooltip.greenlantern.mode", getMode(stack).displayName()).formatted(Formatting.GRAY));
		if (stack.getOrDefault(ModComponents.FLIGHT, false)) {
			tooltip.add(Text.translatable("tooltip.greenlantern.flight_active").formatted(Formatting.AQUA));
		}
		tooltip.add(Text.translatable("tooltip.greenlantern.hint_use").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.greenlantern.hint_cycle").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.greenlantern.hint_battery").formatted(Formatting.DARK_GRAY));
	}

	// ------------------------------------------------------------------ the modes

	private TypedActionResult<ItemStack> toggleFlight(ServerWorld world, ServerPlayerEntity player, ItemStack stack) {
		boolean enable = !stack.getOrDefault(ModComponents.FLIGHT, false);
		if (enable && !player.isCreative() && getCharge(stack) < 1) {
			player.sendMessage(Text.translatable("message.greenlantern.depleted").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		stack.set(ModComponents.FLIGHT, enable);
		world.playSound(null, player.getBlockPos(),
				enable ? SoundEvents.BLOCK_BEACON_POWER_SELECT : SoundEvents.BLOCK_BEACON_DEACTIVATE,
				SoundCategory.PLAYERS, 0.8f, 1.4f);
		player.sendMessage(Text.translatable(enable ? "message.greenlantern.flight_on" : "message.greenlantern.flight_off")
				.formatted(Formatting.GREEN), true);
		player.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(stack);
	}

	/**
	 * John Stewart's bridge: a 3-wide hard-light road in the direction you face.
	 * Look up or down (more than 15 degrees) and the road ramps with you.
	 */
	private static boolean castBridge(ServerWorld world, ServerPlayerEntity player) {
		Direction dir = player.getHorizontalFacing();
		Direction side = dir.rotateYClockwise();
		float pitch = player.getPitch();
		double slope = Math.abs(pitch) < 15.0f
				? 0.0
				: Math.tan(Math.toRadians(MathHelper.clamp(-pitch, -40.0f, 40.0f)));
		BlockPos origin = player.getBlockPos().down();

		int placed = 0;
		for (int i = 1; i <= BRIDGE_LENGTH; i++) {
			int dy = (int) Math.round(slope * i);
			BlockPos center = origin.offset(dir, i).up(dy);
			for (int w = -1; w <= 1; w++) {
				if (ConstructBlock.tryPlace(world, center.offset(side, w))) {
					placed++;
				}
			}
		}
		if (placed > 0) {
			world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.7f, 1.8f);
		}
		return placed > 0;
	}

	/** A 5x4 barrier three blocks in front of you. */
	private static boolean castWall(ServerWorld world, ServerPlayerEntity player) {
		Direction dir = player.getHorizontalFacing();
		Direction side = dir.rotateYClockwise();
		BlockPos base = player.getBlockPos().offset(dir, 3);

		int placed = 0;
		for (int w = -2; w <= 2; w++) {
			for (int h = 0; h < 4; h++) {
				if (ConstructBlock.tryPlace(world, base.offset(side, w).up(h))) {
					placed++;
				}
			}
		}
		if (placed > 0) {
			world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.7f, 1.4f);
		}
		return placed > 0;
	}

	/** A hollow 5x5x5 hard-light bunker around you, with a doorway on the side you face. */
	private static boolean castShelter(ServerWorld world, ServerPlayerEntity player) {
		Direction dir = player.getHorizontalFacing();
		Direction side = dir.rotateYClockwise();
		BlockPos feet = player.getBlockPos();

		int placed = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = -1; dy <= 3; dy++) {
					boolean shell = Math.abs(dx) == 2 || Math.abs(dz) == 2 || dy == -1 || dy == 3;
					if (!shell) {
						continue;
					}
					int forward = dx * dir.getOffsetX() + dz * dir.getOffsetZ();
					int lateral = dx * side.getOffsetX() + dz * side.getOffsetZ();
					boolean doorway = forward == 2 && lateral == 0 && (dy == 0 || dy == 1);
					if (doorway) {
						continue;
					}
					if (ConstructBlock.tryPlace(world, feet.add(dx, dy, dz))) {
						placed++;
					}
				}
			}
		}
		if (placed > 0) {
			world.playSound(null, feet, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.8f, 1.6f);
		}
		return placed > 0;
	}

	/** Hal Jordan's go-to: a pulse of raw willpower along your line of sight. */
	private static boolean castBolt(ServerWorld world, ServerPlayerEntity player) {
		Vec3d start = player.getEyePos();
		Vec3d look = player.getRotationVec(1.0f);
		Vec3d end = start.add(look.multiply(BOLT_RANGE));

		BlockHitResult blockHit = world.raycast(new RaycastContext(
				start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		if (blockHit.getType() != HitResult.Type.MISS) {
			end = blockHit.getPos();
		}

		Box searchBox = player.getBoundingBox().stretch(look.multiply(BOLT_RANGE)).expand(1.0);
		LivingEntity target = null;
		Vec3d impact = end;
		double bestDistSq = start.squaredDistanceTo(end);
		for (Entity entity : world.getOtherEntities(player, searchBox,
				e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator())) {
			Box hitbox = entity.getBoundingBox().expand(entity.getTargetingMargin());
			Optional<Vec3d> hit = hitbox.raycast(start, end);
			if (hit.isPresent()) {
				double distSq = start.squaredDistanceTo(hit.get());
				if (distSq <= bestDistSq) {
					bestDistSq = distSq;
					target = (LivingEntity) entity;
					impact = hit.get();
				}
			}
		}

		if (target != null) {
			target.damage(world.getDamageSources().playerAttack(player), BOLT_DAMAGE);
			target.takeKnockback(0.9, -look.x, -look.z);
		}

		RingFx.beam(world, start.add(0.0, -0.25, 0.0).add(look.multiply(0.6)), impact);
		world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, impact.x, impact.y, impact.z, 12, 0.25, 0.25, 0.25, 0.05);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
				SoundCategory.PLAYERS, 0.9f, 1.7f);
		return true;
	}

	/** Resistance II + Absorption III for 12 seconds. */
	private static boolean castShield(ServerWorld world, ServerPlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 240, 2));
		Vec3d pos = player.getPos();
		for (int i = 0; i < 4; i++) {
			RingFx.circle(world, pos.add(0.0, 0.2 + i * 0.5, 0.0), 1.1, 20);
		}
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8f, 1.0f);
		return true;
	}

	/** Heal 4 hearts, restore a little hunger, and strip harmful potion effects. */
	private static boolean castRestore(ServerWorld world, ServerPlayerEntity player) {
		player.heal(8.0f);
		player.getHungerManager().add(4, 0.5f);
		for (StatusEffectInstance effect : List.copyOf(player.getStatusEffects())) {
			if (effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
				player.removeStatusEffect(effect.getEffectType());
			}
		}
		world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
				25, 0.5, 0.8, 0.5, 0.05);
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.9f, 0.8f);
		return true;
	}

	/** The detective mode: ping everything alive within 24 blocks and make it glow for 10 seconds. */
	private static boolean castScan(ServerWorld world, ServerPlayerEntity player) {
		Box area = player.getBoundingBox().expand(SCAN_RADIUS);
		List<LivingEntity> found = world.getEntitiesByClass(LivingEntity.class, area,
				e -> e != player && e.isAlive() && !e.isSpectator());
		for (LivingEntity entity : found) {
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0, false, false));
		}

		Vec3d feet = player.getPos().add(0.0, 0.1, 0.0);
		for (int radius = 3; radius <= 12; radius += 3) {
			RingFx.circle(world, feet, radius, radius * 8);
		}
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 2.0f);
		player.sendMessage(Text.translatable("message.greenlantern.scan_result", found.size()).formatted(Formatting.GREEN), true);
		return true;
	}
}
