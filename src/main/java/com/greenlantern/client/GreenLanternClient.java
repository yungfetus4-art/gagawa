package com.greenlantern.client;

import com.greenlantern.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class GreenLanternClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Hard-light is see-through; the battery model has transparent pixels around its silhouette.
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CONSTRUCT, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POWER_BATTERY, RenderLayer.getCutout());

		RingHud.register();
	}
}
