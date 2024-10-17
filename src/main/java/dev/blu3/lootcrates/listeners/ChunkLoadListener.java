package dev.blu3.lootcrates.listeners;

import dev.blu3.lootcrates.LootCrates;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.chunk.WorldChunk;

import static dev.blu3.lootcrates.LootCrates.dataManager;

public class ChunkLoadListener implements ServerChunkEvents.Load{

    public void register(){
        ServerChunkEvents.CHUNK_LOAD.register(this);
    }
    @Override
    public void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        chunk.loadEntities();
        for (ArmorStandEntity armorStandEntity : world.getEntitiesByType(TypeFilter.instanceOf(ArmorStandEntity.class), armorStandEntity -> {
            System.out.println("Found hologram, custom name present? " + (armorStandEntity.getCustomName() == null ? ": None." : armorStandEntity.getCustomName().getString()));
            if (armorStandEntity.getCommandTags().contains("lootcrates")) {
                if(!LootCrates.isCrateActive()){
                    return true;
                }else{
                    return dataManager.getCurrentCrate().get().holoUUID != armorStandEntity.getUuid();
                }
            }else{
                if(armorStandEntity.getCustomName() == null) return false;
                for (String s : dataManager.getGeneral().legacy_bugged_holos_to_kill) {
                    if(s.equals(armorStandEntity.getCustomName().getString())) return true;
                }
            }
            return false;
        })) {
            armorStandEntity.kill();
        }
    }
}
