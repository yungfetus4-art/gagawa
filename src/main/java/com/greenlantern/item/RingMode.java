package com.greenlantern.item;

import net.minecraft.text.Text;

/**
 * Everything the ring can do. Sneak + right-click cycles through these in order.
 *
 * Hal Jordan, the veteran test pilot: BOLT, FLIGHT, SHIELD.
 * John Stewart, the architect:        BRIDGE, WALL, SHELTER.
 * The murder-mystery side of the show: SCAN.
 */
public enum RingMode {
	BRIDGE("bridge", 10),
	WALL("wall", 12),
	SHELTER("shelter", 20),
	BOLT("bolt", 6),
	SHIELD("shield", 12),
	RESTORE("restore", 15),
	SCAN("scan", 8),
	FLIGHT("flight", 0);

	private final String key;
	private final int cost;

	RingMode(String key, int cost) {
		this.key = key;
		this.cost = cost;
	}

	public int cost() {
		return cost;
	}

	public Text displayName() {
		return Text.translatable("mode.greenlantern." + key);
	}

	public RingMode next() {
		RingMode[] all = values();
		return all[(ordinal() + 1) % all.length];
	}

	public static RingMode byIndex(int index) {
		RingMode[] all = values();
		return all[Math.floorMod(index, all.length)];
	}
}
