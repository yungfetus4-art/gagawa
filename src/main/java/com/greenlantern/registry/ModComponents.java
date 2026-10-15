package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Custom data components stored on the Power Ring stack.
 * (1.21 replaced raw item NBT with typed data components.)
 */
public final class ModComponents {
	private ModComponents() {}

	/** Remaining willpower charge, 0..PowerRingItem.MAX_CHARGE. */
	public static final ComponentType<Integer> CHARGE = register("charge",
			ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());

	/** Selected mode, stored as the ordinal of RingMode. */
	public static final ComponentType<Integer> MODE = register("mode",
			ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());

	/** Whether ring-powered flight is toggled on. */
	public static final ComponentType<Boolean> FLIGHT = register("flight",
			ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());

	private static <T> ComponentType<T> register(String name, ComponentType<T> type) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, GreenLanternMod.id(name), type);
	}

	/** Called from the mod initializer to force class loading (and therefore registration). */
	public static void register() {}
}
