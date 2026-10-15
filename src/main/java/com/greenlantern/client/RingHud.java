package com.greenlantern.client;

import com.greenlantern.item.PowerRingItem;
import com.greenlantern.item.RingMode;
import com.greenlantern.registry.ModComponents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * A small readout in the bottom-right corner while you hold a Power Ring:
 * current mode and a charge bar.
 */
public final class RingHud {
	private RingHud() {}

	private static final int BAR_WIDTH = 90;
	private static final int BAR_HEIGHT = 5;

	public static void register() {
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.hudHidden) {
				return;
			}

			ItemStack ring = client.player.getMainHandStack();
			if (!(ring.getItem() instanceof PowerRingItem)) {
				ring = client.player.getOffHandStack();
				if (!(ring.getItem() instanceof PowerRingItem)) {
					return;
				}
			}

			int charge = PowerRingItem.getCharge(ring);
			RingMode mode = PowerRingItem.getMode(ring);
			boolean flight = ring.getOrDefault(ModComponents.FLIGHT, false);

			int screenW = client.getWindow().getScaledWidth();
			int screenH = client.getWindow().getScaledHeight();
			int x = screenW - BAR_WIDTH - 10;
			int y = screenH - 44;

			Text label = Text.literal(mode.displayName().getString() + (flight ? "  [FLIGHT]" : ""));
			context.drawTextWithShadow(client.textRenderer, label, x, y, 0x55FF55);
			context.drawTextWithShadow(client.textRenderer, Text.literal(charge + "%"), x, y + 11, 0xAAFFAA);

			int barX = x + 28;
			int barY = y + 13;
			context.fill(barX - 1, barY - 1, barX + BAR_WIDTH - 27, barY + BAR_HEIGHT + 1, 0xFF03140A);
			int filled = Math.round((BAR_WIDTH - 28) * (charge / (float) PowerRingItem.MAX_CHARGE));
			context.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, 0xFF33FF66);
		});
	}
}
