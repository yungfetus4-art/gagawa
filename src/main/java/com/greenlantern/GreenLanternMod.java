package com.greenlantern;

import com.greenlantern.item.RingFlight;
import com.greenlantern.registry.ModBlocks;
import com.greenlantern.registry.ModComponents;
import com.greenlantern.registry.ModItemGroup;
import com.greenlantern.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Green Lantern - main (common) entrypoint.
 *
 * Registration order matters: components first (items reference them as defaults),
 * then blocks (the Power Battery block item needs its block), then items, then the creative tab.
 */
public class GreenLanternMod implements ModInitializer {
	public static final String MOD_ID = "greenlantern";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModComponents.register();
		ModBlocks.register();
		ModItems.register();
		ModItemGroup.register();
		RingFlight.register();

		LOGGER.info("The ring has chosen you. Green Lantern loaded.");
	}
}
