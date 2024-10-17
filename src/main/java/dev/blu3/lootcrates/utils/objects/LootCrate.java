package dev.blu3.lootcrates.utils.objects;


import dev.blu3.lootcrates.utils.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.UUID;

import static dev.blu3.lootcrates.LootCrates.dataManager;
import static dev.blu3.lootcrates.utils.Utils.getWorld;

public class LootCrate {

    public boolean claimed = false;
    public CrateTier crateTier;
    public HashSet<UUID> forfeit = new HashSet<>();
    public long startTime;
    public UUID holoUUID;
    public CrateLocation location;

    public LootCrate() {
    }

    public LootCrate(CrateTier crateTier) {
        this.crateTier = crateTier;
    }

    public LootCrate(CrateTier crateTier, boolean spawn) {
        this.crateTier = crateTier;
        if(spawn) this.spawn();
    }

    public void spawn() {
        this.startTime = System.currentTimeMillis();
        this.location = new CrateLocation();
        this.location.generatePos();
        BlockPos pos = location.exact.getBlockPos();
        ServerWorld level = getWorld(this.location.worldName);
        WorldChunk chunk = level.getWorldChunk(pos);
        BlockPos pos2 = pos.mutableCopy().up(1);
        ArmorStandEntity holoEntity = new ArmorStandEntity(level, pos2.getX() - 0.10, pos2.getY() - 1.75, pos2.getZ() - 0.10);
        holoEntity.setInvisible(true);
        holoEntity.setInvulnerable(true);
        holoEntity.setFrozenTicks(-1);
        holoEntity.setMovementSpeed(0f);
        holoEntity.setCustomNameVisible(true);
        holoEntity.addCommandTag("lootcrate");
        holoEntity.setCustomName(Text.literal((dataManager.getMsgStr("lootcrate-holo-text").replace("<crate>", Utils.regex(crateTier.holo_name)))));
        holoUUID = holoEntity.getUuid();
        chunk.setLoadedToWorld(true);
        chunk.loadEntities();
        level.setBlockState(pos, Blocks.CHEST.getDefaultState(), 1);

        level.spawnEntity(holoEntity);
    }

    public void remove() {
        ServerWorld level = getWorld(this.location.worldName);
        BlockPos pos = location.exact.getBlockPos();
        WorldChunk chunk = level.getWorldChunk(pos);
        level.setBlockState(pos, Blocks.AIR.getDefaultState(), 1);
        chunk.setLoadedToWorld(true);
        chunk.loadEntities();
        level.iterateEntities().forEach(entity -> {
            if(entity.getUuid().equals(holoUUID)) entity.kill();
        });
    }

}
