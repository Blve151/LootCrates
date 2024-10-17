package dev.blu3.lootcrates.utils.objects;

import dev.blu3.lootcrates.LootCrates;

public class SimpleWorld {
    public String registryName;
    public String displayName;

    public SimpleWorld(String registryName, String displayName) {
        this.registryName = registryName;
        this.displayName = displayName;
    }

    public static String getDisplayFromRegistry (String registryName){
        for (SimpleWorld world : LootCrates.dataManager.getGeneral().worlds) {
            if(world.registryName.equals(registryName)){
                return world.displayName;
            }
        }
        return "Default";
    }
}
