package dev.blu3.lootcrates.listeners;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import dev.blu3.lootcrates.utils.Utils;
import dev.blu3.lootcrates.utils.objects.CrateTier;
import dev.blu3.lootcrates.utils.objects.LootCrate;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import kotlin.Unit;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static dev.blu3.lootcrates.LootCrates.*;
import static dev.blu3.lootcrates.utils.Utils.getWorld;


public class CrateInteractListener implements AttackBlockCallback {
    public CrateInteractListener() {
        PlatformEvents.RIGHT_CLICK_BLOCK.subscribe(Priority.HIGHEST, rightClickBlock -> {
            if (!isCrateActive()) return Unit.INSTANCE;
            LootCrate crate = dataManager.getCurrentCrate().get();
            BlockPos pos = rightClickBlock.getPos();
            ServerWorld crateWorld = getWorld(crate.location.worldName);
            if(rightClickBlock.getPlayer().getWorld() != crateWorld){
                return Unit.INSTANCE;
            }
            ServerPlayerEntity player = rightClickBlock.getPlayer();
            BlockState state = crateWorld.getBlockState(pos);
            if (!pos.equals(crate.location.exact.getBlockPos()) || state != Blocks.CHEST.getDefaultState())
                return Unit.INSTANCE;
            rightClickBlock.cancel();
            if (!Permissions.check(player, "lootcrates.bypass") && crate.forfeit.contains(player.getUuid())) {
                player.sendMessage(dataManager.getMsg("cant-interact-lootcrate"));
                return Unit.INSTANCE;
            }
            if (crate.claimed) return Unit.INSTANCE;
            handleCrate(crate, player);
            return Unit.INSTANCE;
        });
    }

    public void handleCrate(LootCrate crate, ServerPlayerEntity player) {
        crate.claimed = true;

        CrateTier tier = crate.crateTier;
        List<CrateTier.SimpleReward> tierCmds = tier.getRandomRewards();
        StringBuilder rewards = new StringBuilder();
        for (int i = 0; i < tierCmds.size(); i++) {
            CrateTier.SimpleReward simpleReward = tierCmds.get(i);
            if (i == tierCmds.size() - 1) {
                rewards.append(" ").append(simpleReward.name);
            } else {
                rewards.append(" ").append(simpleReward.name).append(",");
            }

            try {
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), simpleReward.commandString.replace("%player%", player.getName().getString()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.sendMessage(Text.literal(Utils.regex(tier.broadcast).replace("<player>", player.getName().getString())));
        }
        player.sendMessage(Text.literal(Utils.regex(tier.message.replace("<rewards>", rewards.toString()))));

        crate.remove();
        dataManager.setLastCrateDrop(System.currentTimeMillis());
        dataManager.setLastClaimedPlayer(player.getName().getString());
        dataManager.setLastTierName(crate.crateTier.name);
        dataManager.setCurrentCrateEmpty();
    }


    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!isCrateActive()) return ActionResult.PASS;
        LootCrate crate = dataManager.getCurrentCrate().get();
        World crateWorld = getWorld(crate.location.worldName);
        if(crateWorld != world) return ActionResult.PASS;
        BlockState state = crateWorld.getBlockState(pos);
        if (!pos.equals(crate.location.exact.getBlockPos()) || state != Blocks.CHEST.getDefaultState())
            return ActionResult.PASS;

        if (!pos.equals(crate.location.exact.getBlockPos())) return ActionResult.PASS;
        if (!Permissions.check(player, "lootcrates.bypass") && crate.forfeit.contains(player.getUuid())) {
            player.sendMessage(dataManager.getMsg("cant-interact-lootcrate"));
            return ActionResult.FAIL;
        }

        if (crate.claimed) return ActionResult.FAIL;
        handleCrate(crate, (ServerPlayerEntity) player);
        return ActionResult.FAIL;
    }
}
