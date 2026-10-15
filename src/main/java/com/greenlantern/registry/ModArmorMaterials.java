package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

public final class ModArmorMaterials {
	private ModArmorMaterials() {}

	/** Multiplier used with ArmorItem.Type#getMaxDamage (leather = 5, gold = 7, chain = 15, iron = 15). */
	public static final int DURABILITY_MULTIPLIER = 15;

	/**
	 * The suit is mostly cosmetic - the ring does the heavy lifting - but wearing all four
	 * pieces gives the ring a set bonus (see PowerRingItem#hasFullSuit).
	 *
	 * Textures: assets/greenlantern/textures/models/armor/lantern_layer_1.png and lantern_layer_2.png
	 */
	public static final RegistryEntry<ArmorMaterial> LANTERN = Registry.registerReference(
			Registries.ARMOR_MATERIAL,
			GreenLanternMod.id("lantern"),
			new ArmorMaterial(
					Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
						map.put(ArmorItem.Type.BOOTS, 1);
						map.put(ArmorItem.Type.LEGGINGS, 3);
						map.put(ArmorItem.Type.CHESTPLATE, 4);
						map.put(ArmorItem.Type.HELMET, 1);
						map.put(ArmorItem.Type.BODY, 4);
					}),
					20,
					SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
					() -> Ingredient.ofItems(Items.GREEN_WOOL),
					List.of(new ArmorMaterial.Layer(GreenLanternMod.id("lantern"))),
					0.0f,
					0.0f
			)
	);

	public static void register() {}
}
