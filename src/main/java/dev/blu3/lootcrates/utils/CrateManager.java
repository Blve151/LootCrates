package dev.blu3.lootcrates.utils;

import dev.blu3.lootcrates.LootCrates;
import dev.blu3.lootcrates.utils.objects.CrateTier;
import dev.blu3.lootcrates.utils.objects.LootCrate;
import dev.blu3.lootcrates.utils.objects.SimpleWorld;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static dev.blu3.lootcrates.LootCrates.*;
import static dev.blu3.lootcrates.utils.Utils.timeDiffFormat;


public class CrateManager implements Runnable {

    private AtomicReference<Boolean> generatingPosition = new AtomicReference<>(false);

    public void spawnCrate(CrateTier crateTier, Optional<ServerCommandSource> src) {
        if (generatingPosition.get()) {
            src.ifPresent(source -> {
                source.sendError(dataManager.getMsg("command-failure-loading"));
            });
            return;
        }
        generatingPosition = new AtomicReference<>(true);
        LootCrate crate = new LootCrate(crateTier, true);
        BlockPos pos = crate.location.around.getBlockPos();
        String x = String.valueOf(pos.getX());
        String y = String.valueOf(pos.getY());
        String z = String.valueOf(pos.getZ());

        ArrayList<String> dropTexts = new ArrayList<>();

        crateTier.getRewardCommands().stream().sorted((o1, o2) -> Integer.compare(o2.chance, o1.chance)).forEach(drop -> {
            dropTexts.add(dataManager.getMsgStr("broadcast-lootcrate-drop-text").replace("<dropName>", drop.name).replace("<dropChance>", drop.chance + ""));
        });

        dropTexts.add(dataManager.getMsgStr("broadcast-lootcrate-reward-amount-text").replace("<rewardQuantity>", crateTier.cmdAmount + ""));


        MutableText broadcast = Text.literal(dataManager.getMsgStr("lootcrate-new-spawn-base").replace("<crate>", crateTier.name));
        broadcast.append(Text.empty());
        broadcast.append(Text.literal(dataManager.getMsgStr("lootcrate-new-spawn-location")
                .replace("<x>", x)
                .replace("<y>", y).replace("<z>", z)
                .replace("<world>", SimpleWorld.getDisplayFromRegistry(crate.location.worldName))));
        broadcast.append(Text.empty());

        StringBuilder dropText = new StringBuilder();
        for (String dt : dropTexts) {
            dropText.append(dt);
        }

        MutableText dropHover = dataManager.getMsg("lootcrate-new-spawn-drops").copy();
        dropHover.setStyle(dropHover.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(dropText.toString()))));
        broadcast.append(dropHover);

        MutableText forfeitHover = dataManager.getMsg("lootcrate-new-spawn-forfeit").copy();
        forfeitHover.setStyle(forfeitHover.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/forfeitcrate")));
        forfeitHover.setStyle(forfeitHover.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, dataManager.getMsg("lootcrate-new-spawn-forfeit-msg"))));
        broadcast.append(forfeitHover);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(broadcast);
            if (player.getCommandTags().contains("lootcrates.autoforfeit")) {
                player.sendMessage(dataManager.getMsg("command-autoforfeit-shown"));
                crate.forfeit.add(player.getUuid());
            }
        }
        dataManager.setCurrentCrate(crate);
        dataManager.getCrateManager().run();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (dataManager.getCurrentCrate().get().forfeit.contains(player.getUuid())) continue;
            player.setMovementSpeed(0.1f);
        }
        this.generatingPosition = new AtomicReference<>(false);
    }

    private long intervalTime;

    @Override
    public void run() {
        if (dataManager.getNextCrateDrop() < 0) setRandomNextCrateDrop();
        if (!isCrateActive()) {
            if (System.currentTimeMillis() >= dataManager.getNextCrateDrop()) {
                if (server.getPlayerManager().getPlayerList().size() >= dataManager.getGeneral().minimumPlayers) {
                    if (!this.generatingPosition.get()) spawnCrate(dataManager.getRandomTier(), Optional.empty());
                }
            }
        } else {
            LootCrate lastCrate = dataManager.getCurrentCrate().get();
            BlockPos pos = dataManager.getCurrentCrate().get().location.exact.getBlockPos();
            if (!server.getOverworld().getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
                server.getOverworld().setBlockState(pos, Blocks.CHEST.getDefaultState(), 1);
            }

            if (System.currentTimeMillis() - lastCrate.startTime > dataManager.getGeneral().removeTimeMillis) {
                setRandomNextCrateDrop();
                lastCrate.remove();
                dataManager.setLastTierName(lastCrate.crateTier.name);
                dataManager.setLastClaimedPlayer("N/A");
                dataManager.setLastCrateDrop(System.currentTimeMillis());
                dataManager.setCurrentCrateEmpty();
                server.getPlayerManager().broadcast(dataManager.getMsg("lootcrate-ended"), true);
            } else {
                if (System.currentTimeMillis() - dataManager.getCurrentCrate().get().startTime > 15 * 60 * 1000) {
                    if (System.currentTimeMillis() - intervalTime > dataManager.getGeneral().unclaimedIntervalMillis) {
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            if (!lastCrate.forfeit.contains(player.getUuid())) {
                                player.sendMessage(Text.literal(dataManager.getMsgStr("lootcrate-unclaimed-available").replace("<crate>", lastCrate.crateTier.name)));
                            }
                        }
                        intervalTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    public static void setRandomNextCrateDrop() {
        long timeFromNow = (long) (LootCrates.random.nextInt((int) ((dataManager.getGeneral().maximumCrateTimeHours - dataManager.getGeneral().minimumCrateTimeHours) * 3600000))
                + dataManager.getGeneral().minimumCrateTimeHours * 3600000);
        timeFromNow += System.currentTimeMillis();
        logger.warn("Next lootcrate should spawn in " + timeDiffFormat((timeFromNow - System.currentTimeMillis()) / 1000, true));
        dataManager.setNextCrateDrop(timeFromNow);
    }
}