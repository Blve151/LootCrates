package dev.blu3.lootcrates.utils.objects;


import com.google.common.collect.Lists;
import dev.blu3.lootcrates.LootCrates;

import java.util.ArrayList;
import java.util.List;

public class CrateTier {

	public CrateTier() {}

	public int chance = 10;
	public int cmdAmount = 3;
	public String name = "Tier 1";
	public String holo_name = "&a&l&nTier 1&r";

	public String broadcast = "&dLootCrates &7> &e<player> &bclaimed a &e&nTier 1&r &bcrate!";
	public ArrayList<SimpleReward> rewardSimpleRewards = Lists.newArrayList(new SimpleReward(),
			new SimpleReward(2, "pokegiveother %player% Mewtwo", 10, "x1 Mewtwo"),
			new SimpleReward(3, "kill %player%", 5, "Death."));
	public String message = "&dLootCrates &7> &bYou claimed a &e&nTier 1&r &bcrate! You received: &3<rewards>";

	public boolean allowRepeatRewards = false;
	public ArrayList<SimpleReward> getRewardCommands() {
		return rewardSimpleRewards;
	}

	public List<SimpleReward> getRandomRewards() {
		List<SimpleReward> possibleRewards = new ArrayList<>();
		List<SimpleReward> finalRewards = new ArrayList<>();
		List<Integer> ids = new ArrayList<>();

		if(this.rewardSimpleRewards.size() < this.cmdAmount && !this.allowRepeatRewards){
			this.allowRepeatRewards = true;
		}

		this.rewardSimpleRewards.forEach(simpleReward -> {
			for (int i = 0; i < simpleReward.chance; i++) {
				possibleRewards.add(simpleReward);
			}
		});

		while(finalRewards.size() != this.cmdAmount){
			SimpleReward sr  = possibleRewards.get(LootCrates.random.nextInt(possibleRewards.size()));
			if(!allowRepeatRewards && ids.contains(sr.id)) continue;
			ids.add(sr.id);
			finalRewards.add(sr);
		}

		return finalRewards;
	}

	public static class SimpleReward {

		public SimpleReward(){
		}
		public SimpleReward(int id, String commandString, int chance, String name) {
			this.id = id;
			this.commandString = commandString;
			this.chance = chance;
			this.name = name;
		}
		public int id = 1;
		public String commandString = "give %player% cobblemon:poke_ball 1";
		public int chance = 10;
		public String name = "1x Poke Ball";
	}
}
