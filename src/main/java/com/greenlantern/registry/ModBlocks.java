package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.greenlantern.block.ConstructBlock;
import com.greenlantern.block.PowerBatteryBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
	private ModBlocks() {}

	public static final Block POWER_BATTERY = Registry.register(Registries.BLOCK,
			GreenLanternMod.id("power_battery"),
			new PowerBatteryBlock(AbstractBlock.Settings.create()
					.mapColor(MapColor.EMERALD_GREEN)
					.strength(3.0f, 6.0f)
					.sounds(BlockSoundGroup.METAL)
					.luminance(state -> 15)
					.nonOpaque()));

	/** Temporary hard-light block. Has no item form on purpose - only the ring can make it. */
	public static final Block CONSTRUCT = Registry.register(Registries.BLOCK,
			GreenLanternMod.id("hard_light_construct"),
			new ConstructBlock(AbstractBlock.Settings.create()
					.mapColor(MapColor.EMERALD_GREEN)
					.nonOpaque()
					.luminance(state -> 12)
					.breakInstantly()
					.dropsNothing()
					.sounds(BlockSoundGroup.AMETHYST_BLOCK)
					.pistonBehavior(PistonBehavior.DESTROY)
					.allowsSpawning((state, world, pos, type) -> false)
					.solidBlock((state, world, pos) -> false)
					.suffocates((state, world, pos) -> false)
					.blockVision((state, world, pos) -> false)));

	public static void register() {}
}
