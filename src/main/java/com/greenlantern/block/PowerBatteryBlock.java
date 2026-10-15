package com.greenlantern.block;

import com.greenlantern.item.PowerRingItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * The Central Power Battery (the lantern). Right-click it to recharge every ring in your inventory.
 */
public class PowerBatteryBlock extends Block {
	private static final VoxelShape SHAPE = VoxelShapes.union(
			Block.createCuboidShape(3, 0, 3, 13, 2, 13),   // base plate
			Block.createCuboidShape(4, 2, 4, 12, 12, 12),  // glowing body
			Block.createCuboidShape(3, 12, 3, 13, 14, 13), // top cap
			Block.createCuboidShape(6, 14, 6, 10, 16, 10)  // handle
	);

	public PowerBatteryBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		int recharged = 0;
		boolean hasRing = false;
		PlayerInventory inventory = player.getInventory();
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.getItem() instanceof PowerRingItem) {
				hasRing = true;
				if (PowerRingItem.getCharge(stack) < PowerRingItem.MAX_CHARGE) {
					PowerRingItem.setCharge(stack, PowerRingItem.MAX_CHARGE);
					recharged++;
				}
			}
		}

		if (recharged > 0) {
			serverWorld.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.9f, 1.5f);
			serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
					30, 0.5, 0.6, 0.5, 0.05);
			player.sendMessage(Text.translatable("message.greenlantern.recharged", recharged).formatted(Formatting.GREEN), true);
		} else if (hasRing) {
			player.sendMessage(Text.translatable("message.greenlantern.already_full").formatted(Formatting.GRAY), true);
		} else {
			player.sendMessage(Text.translatable("message.greenlantern.no_ring").formatted(Formatting.GRAY), true);
		}
		return ActionResult.SUCCESS;
	}

	// Client-side only in practice; harmless to declare on the common class.
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(3) == 0) {
			double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
			double y = pos.getY() + 0.3 + random.nextDouble() * 0.8;
			double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
			world.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.03, 0.0);
		}
	}
}
