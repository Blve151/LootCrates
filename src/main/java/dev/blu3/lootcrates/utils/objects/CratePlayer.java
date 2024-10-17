package dev.blu3.lootcrates.utils.objects;


import dev.blu3.lootcrates.LootCrates;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;

public class CratePlayer {
	private static final List<CratePlayer> players = new ArrayList<>();

	public static CratePlayer getCratePlayer(UUID uuid) {
		for (CratePlayer player : players) {
			if (player.uuid.equals(uuid))
				return player;
		}
		CratePlayer cp = new CratePlayer(uuid);
		players.add(cp);
		return cp;
	}

	private CratePlayer(UUID uuid) {
		this.uuid = uuid;
	}


	public boolean isDefaultOrLowerSpeeds() {
		ServerPlayerEntity player = LootCrates.server.getPlayerManager().getPlayer(uuid);
		if (player != null) {
			if(player.speed > 0 && player.getMovementSpeed() >0.05 || player.getMovementSpeed() > 0.14) return false;
		}
		return false;
	}

	private final UUID uuid;

	private long speedTimestamp;

	public void setSpeedTimestamp(long time) {
		speedTimestamp = time;
	}

	public long getSpeedChangeTimestamp() {
		return speedTimestamp;
	}
}
