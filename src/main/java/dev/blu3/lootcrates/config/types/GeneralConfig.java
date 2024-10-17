package dev.blu3.lootcrates.config.types;


import com.google.common.collect.Lists;
import dev.blu3.lootcrates.utils.objects.CrateTier;
import dev.blu3.lootcrates.utils.objects.SimpleWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GeneralConfig {
    public ArrayList<SimpleWorld> worlds = Lists.newArrayList(new SimpleWorld("overworld", "Main World"), new SimpleWorld("the_end", "The End"));
    public HashMap<String, String> messages = new Messages().getDefaultMessages();
    public ArrayList<CrateTier> crateTiers = new ArrayList<>(Collections.singletonList(new CrateTier()));
    public double maximumCrateTimeHours = 6;
    public double minimumCrateTimeHours = 1.5;
    public int secondMaximumRadius = 200;
    public int secondMinimumRadius = 50;
    public long removeTimeMillis = 1800000;
    public long unclaimedIntervalMillis = 60000;
    public int minimumPlayers = 5;
    public ArrayList<String> blacklistedCommands = new ArrayList<>(Collections.singletonList("teleport"));
    public ArrayList<String> cooldownCommands = new ArrayList<>(Collections.singletonList(""));
    public String[] legacy_bugged_holos_to_kill = new String[]{"OG Tier 1"};

}
class Messages{
    public HashMap<String, String> getDefaultMessages(){
        HashMap<String, String> messages = new HashMap<>();
        messages.put("blacklisted-command-use", "&dLootCrates &7> &cIf you use this command, " +
                "you will have to forfeit this current loot crate. Type &b'confirm' &cafter the command to continue.");

        messages.put("permission-command-use", "&cYou don't have permission to use this command.");

        messages.put("cooldown-command-use", "&dLootCrates &7> &cThis command is still on cool down for &b<time> &cunless you forfeit the loot crate &e<crate>&c.");
        messages.put("cant-interact-lootcrate", "&dLootCrates &7> &cYou aren't eligible to claim this loot crate.");

        messages.put("lootcrate-holo-text", "&8[&6&kII&r&8] <crate> &bCrate &8[&6&kII&r&8]");
        messages.put("lootcrate-ended", "&dLootCrates &7> &e&n<crate> &chas now ended, it is no longer available to claim!");
        messages.put("lootcrate-unclaimed-available", "&dLootCrates &7> &e&n<crate> &bis now a new loot crate, check /lastcrate for more!");

        messages.put("lootcrate-new-spawn-base", "&dLootCrates &7> &6&k|&r&d&l!&6&k|&r &e&n<crate> &bcrate has just spawned! \n");
        messages.put("lootcrate-new-spawn-location", "&2Location:  &aX: <x>, Y: <y>, Z: <z> &e(<world>)");
        messages.put("lootcrate-new-spawn-drops", " &e&l[VIEW DROPS] ");
        messages.put("lootcrate-new-spawn-forfeit", "&c&l[FORFEIT] ");
        messages.put("lootcrate-new-spawn-forfeit-msg", "&4(!) &cForfeiting this loot crate will remove all restrictions and cool-downs but you will not be able to claim the reward or help other players.");

        messages.put("command-failure-player", "&dLootCrates &7> &cOnly players can run this command.");
        messages.put("command-failure-loading", "&dLootCrates &7> &cPlease be patient, a crate is already being spawned...");
        messages.put("command-failure-crate-active", "&dLootCrates &7> &cThere is no active loot crate currently.");
        messages.put("command-failure-already-forfeited", "&dLootCrates &7> &cYou're already forfeited for this loot crate.");
        messages.put("command-forfeit-forfeited", "&dLootCrates &7> &aYou're now forfeited for the current loot crate, cool-downs and and other restrictions are now gone.");

        messages.put("command-failure-crate-claimed", "&dLootCrates &7> &cThere is no recorded last loot crate yet.");
        messages.put("command-failure-crate-exists", "&dLootCrates &7> &cYou can't force crate when .");
        messages.put("command-failure-crate-name", "&dLootCrates &7> &cThat is not a valid crate tier, check your spelling and the general config.");
        messages.put("command-failure-crate-end-inactive", "&dLootCrates &7> &cThere is no current loot crate to forcefully end.");

        messages.put("command-autoforfeit-on", "&dLootCrates &7> &bYou have &a&nenabled&r &bauto-forfeit, upcoming loot-crate spawns will be forfeited.");
        messages.put("command-autoforfeit-off", "&dLootCrates &7> &bYou have &c&ndisabled&r &bauto-forfeit, upcoming loot-crate spawns will no longer be forfeited.");
        messages.put("command-autoforfeit-shown", "&dLootCrates &7> &bYou have &dauto-forfeit enabled&b, you won't be eligible for this loot crate. You can use the toggle to turn it off.");

        messages.put("command-lastcrate-active", "&dLootCrates &7> &e&n<crate>&r &bloot crate has spawned around &a<time> ago&b, it is located 500 blocks within &dx:<x> y:<y> z:<z> &e(<world>)&b!");
        messages.put("command-lastcrate-inactive", "&dLootCrates &7> &bLast &e&n<crate>&r &bloot crate spawn was around &a<time> ago&b, player who claimed was: &d<claimPlayer>&b!");

        messages.put("command-crate-end-ended", "&dLootCrates &7> &cThe current loot crate has ended, there is no longer an active loot crate.");

        messages.put("broadcast-lootcrate-drop-text", "&8&l• &d<dropName> &7(<dropChance>% chance) ");
        messages.put("broadcast-lootcrate-drop-amount-text", "&8&l•&r &d<dropName> &7(<dropChance>% chance) ");
        messages.put("broadcast-lootcrate-reward-amount-text", "&8&l•&r &eReward quantity: &d<rewardQuantity> rewards &eper claim");


        return messages;
    }
}


