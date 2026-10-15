package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
	private ModItemGroup() {}

	public static final ItemGroup GREEN_LANTERN = Registry.register(Registries.ITEM_GROUP,
			GreenLanternMod.id("main"),
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(ModItems.POWER_RING))
					.displayName(Text.translatable("itemGroup.greenlantern"))
					.entries((context, entries) -> {
						entries.add(ModItems.POWER_RING);
						entries.add(ModItems.POWER_BATTERY);
						entries.add(ModItems.LANTERN_MASK);
						entries.add(ModItems.LANTERN_JACKET);
						entries.add(ModItems.LANTERN_TROUSERS);
						entries.add(ModItems.LANTERN_BOOTS);
					})
					.build());

	public static void register() {}
}
