package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.greenlantern.item.PowerRingItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	private ModItems() {}

	/** New rings start fully charged (the CHARGE component default). */
	public static final Item POWER_RING = register("power_ring",
			new PowerRingItem(new Item.Settings()
					.maxCount(1)
					.rarity(Rarity.EPIC)
					.fireproof()
					.component(ModComponents.CHARGE, PowerRingItem.MAX_CHARGE)));

	public static final Item POWER_BATTERY = register("power_battery",
			new BlockItem(ModBlocks.POWER_BATTERY, new Item.Settings().rarity(Rarity.RARE)));

	// The Lantern suit. Wear all four pieces for the ring set bonus.
	public static final Item LANTERN_MASK = register("lantern_mask",
			new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.HELMET,
					new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(ModArmorMaterials.DURABILITY_MULTIPLIER))));

	public static final Item LANTERN_JACKET = register("lantern_jacket",
			new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.CHESTPLATE,
					new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(ModArmorMaterials.DURABILITY_MULTIPLIER))));

	public static final Item LANTERN_TROUSERS = register("lantern_trousers",
			new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.LEGGINGS,
					new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(ModArmorMaterials.DURABILITY_MULTIPLIER))));

	public static final Item LANTERN_BOOTS = register("lantern_boots",
			new ArmorItem(ModArmorMaterials.LANTERN, ArmorItem.Type.BOOTS,
					new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(ModArmorMaterials.DURABILITY_MULTIPLIER))));

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, GreenLanternMod.id(name), item);
	}

	public static void register() {}
}
