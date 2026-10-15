package com.greenlantern.item;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Small particle helpers so the ring effects all share one "emerald light" look.
 * If a future Minecraft version changes the DustParticleEffect constructor, this is the only place to fix.
 */
public final class RingFx {
	private RingFx() {}

	public static final DustParticleEffect GREEN = new DustParticleEffect(new Vector3f(0.15f, 1.0f, 0.35f), 1.3f);

	/** A straight beam of green dust from one point to another. */
	public static void beam(ServerWorld world, Vec3d from, Vec3d to) {
		Vec3d delta = to.subtract(from);
		double length = delta.length();
		if (length < 0.1) {
			return;
		}
		Vec3d step = delta.multiply(0.5 / length);
		int steps = (int) (length / 0.5);
		Vec3d p = from;
		for (int i = 0; i <= steps; i++) {
			world.spawnParticles(GREEN, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
			p = p.add(step);
		}
	}

	/** A horizontal ring of green dust. */
	public static void circle(ServerWorld world, Vec3d center, double radius, int points) {
		for (int i = 0; i < points; i++) {
			double angle = (Math.PI * 2.0 * i) / points;
			world.spawnParticles(GREEN,
					center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius,
					1, 0.0, 0.0, 0.0, 0.0);
		}
	}
}
