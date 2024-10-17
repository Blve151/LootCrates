package dev.blu3.lootcrates.config.types;

import dev.blu3.lootcrates.utils.objects.LootCrate;

import java.util.Optional;

public class CrateData {
	public Optional<LootCrate> currentCrate = Optional.empty();
	public long nextCrateDrop = -1;
	public long lastCrateDrop = -1;
	public String lastCrateClaimed = "N/A";
	public String lastTierName = "N/A";
}
