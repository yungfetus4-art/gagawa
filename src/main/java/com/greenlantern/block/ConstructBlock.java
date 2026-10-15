package com.greenlantern.block;

import com.greenlantern.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A chunk of solid green light created by the ring.
 *
 * It lives for LIFETIME_TICKS, flickers (fading = true) for the last FADE_TICKS as a warning,
 * and then vanishes. It uses scheduled block ticks, so no block entity is needed and the timer
 * survives chunk unloads and world saves.
 *
 * Extends TransparentBlock so it gets glass-like behaviour (adjacent faces are culled, no AO darkening).
 */
public class ConstructBlock extends TransparentBlock {
	public static final BooleanProperty FADING = BooleanProperty.of("fading");

	/** Total life of a construct, in ticks (20 ticks = 1 second). 600 = 30 seconds. */
	public static final int LIFETIME_TICKS = 600;
	/** How long the block flickers before disappearing. */
	public static final int FADE_TICKS = 60;

	public ConstructBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FADING, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FADING);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!world.isClient() && !state.get(FADING)) {
			world.scheduleBlockTick(pos, this, LIFETIME_TICKS - FADE_TICKS);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!state.get(FADING)) {
			// Phase 1 -> 2: start flickering.
			world.setBlockState(pos, state.with(FADING, true), Block.NOTIFY_LISTENERS);
			world.scheduleBlockTick(pos, this, FADE_TICKS);
		} else {
			// Phase 2 -> gone.
			world.removeBlock(pos, false);
			world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					3, 0.3, 0.3, 0.3, 0.0);
			if (random.nextInt(6) == 0) {
				world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.4f, 1.8f);
			}
		}
	}

	/**
	 * Try to turn the position into a construct. Only air and soft replaceable blocks
	 * (grass, snow layers, etc.) are replaced - never solid blocks, never fluids.
	 *
	 * @return true if a construct was placed
	 */
	public static boolean tryPlace(ServerWorld world, BlockPos pos) {
		if (world.isOutOfHeightLimit(pos)) {
			return false;
		}
		BlockState existing = world.getBlockState(pos);
		boolean free = existing.isAir() || (existing.isReplaceable() && existing.getFluidState().isEmpty());
		if (!free) {
			return false;
		}
		return world.setBlockState(pos, ModBlocks.CONSTRUCT.getDefaultState(), Block.NOTIFY_ALL);
	}
}
