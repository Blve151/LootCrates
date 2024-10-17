package dev.blu3.lootcrates;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.blu3.lootcrates.config.DataManager;
import dev.blu3.lootcrates.listeners.ChunkLoadListener;
import dev.blu3.lootcrates.listeners.CrateInteractListener;
import dev.blu3.lootcrates.listeners.PlayerCommandListener;
import dev.blu3.lootcrates.utils.Task;
import dev.blu3.lootcrates.utils.objects.CrateLocation;
import dev.blu3.lootcrates.utils.objects.CrateTier;
import dev.blu3.lootcrates.utils.objects.LootCrate;
import dev.blu3.lootcrates.utils.objects.SimpleWorld;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static dev.blu3.lootcrates.utils.Utils.getWorld;
import static dev.blu3.lootcrates.utils.Utils.timeDiffFormat;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class LootCrates implements ModInitializer {

//    TODO

    // Add Cobblemon boss battles to claim crate

    public static DataManager dataManager = new DataManager();
    public static MinecraftServer server = null;
    public static final Map<UUID, HashMap<String, Long>> cooldowns = new HashMap<>();

    public static Random random = new Random();
    public static Logger logger = new Logger(Logger.Level.INFO);

    private static final String BASE_PERMISSION = "lootcrates.command.";


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);

        dataManager.load(Optional.empty());

        new PlayerCommandListener().register();
        new ChunkLoadListener().register();

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (isCrateActive()) {
                LootCrate crate = dataManager.getCurrentCrate().get();
                return !crate.location.exact.getBlockPos().equals(pos);
            }
            return true;
        });

        AttackBlockCallback.EVENT.register(new CrateInteractListener());

        // Reload
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("cratereload")
                .executes(context -> {
                    if (!canUseCommand(context.getSource(), "cratereload")) return 1;
                    dataManager.load(Optional.ofNullable(context.getSource()));
                    return 1;
                })));

        // Auto-forfeit
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("autoforfeit")
                .executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (!canUseCommand(src, "autoforfeit")) return 1;

                    ServerPlayerEntity player = src.getPlayer();
                    if (player == null) {
                        src.sendMessage(dataManager.getMsg("command-failure-player"));
                        return 1;
                    }
                    if (player.getCommandTags().contains("lootcrates.autoforfeit")) {
                        player.removeScoreboardTag("lootcrates.autoforfeit");
                        player.sendMessage(dataManager.getMsg("command-autoforfeit-off"));
                    } else {
                        player.addCommandTag("lootcrates.autoforfeit");
                        player.sendMessage(dataManager.getMsg("command-autoforfeit-on"));
                    }
                    return 1;
                })));

        // Forfeit
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("forfeitcrate")
                .executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (!canUseCommand(src, "forfeitcrate")) return 1;

                    ServerPlayerEntity player = src.getPlayer();
                    if (player == null) {
                        src.sendMessage(dataManager.getMsg("command-failure-player"));
                        return 1;
                    }
                    if (!isCrateActive()) {
                        src.sendMessage(dataManager.getMsg("command-failure-crate-active"));
                        return 1;
                    }
                    LootCrate crate = dataManager.getCurrentCrate().get();
                    if (crate.forfeit.contains(player.getUuid())) {
                        player.sendMessage(dataManager.getMsg("command-failure-already-forfeited"));
                        return 1;
                    }
                    crate.forfeit.add(player.getUuid());
                    dataManager.setCurrentCrate(crate);
                    player.sendMessage(dataManager.getMsg("command-forfeit-forfeited"));
                    return 1;
                })));

        // TP
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("tpcrate")
                .executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (!canUseCommand(src, "tpcrate")) return 1;

                    ServerPlayerEntity player = src.getPlayer();
                    if (player == null) {
                        src.sendMessage(dataManager.getMsg("command-failure-player"));
                        return 1;
                    }
                    if (!isCrateActive()) {
                        src.sendMessage(dataManager.getMsg("command-failure-crate-active"));
                        return 1;
                    }
                    CrateLocation loc = dataManager.getCurrentCrate().get().location;
                    ServerWorld crateWorld = getWorld(loc.worldName);
                    crateWorld.getWorldChunk(loc.exact.getBlockPos()).setLoadedToWorld(true);
                    if (player.getServerWorld() != crateWorld) {
                        if (player.moveToWorld(crateWorld) != null) {
                            Task.builder()
                                    .delay(20)
                                    .execute(() -> player.setPosition(loc.exact.getVec3()))
                                    .build();
                        }
                    } else {
                        player.setPosition(loc.exact.getVec3());
                    }
                    return 1;
                })));

        // LastCrate
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("lastcrate")
                .executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (!canUseCommand(src, "lastcrate")) return 1;

                    String msg;

                    if (!isCrateActive()) {
                        if (dataManager.getLastClaimedPlayer().equals("N/A") || dataManager.getLastTierName().equals("N/A")) {
                            src.sendMessage(dataManager.getMsg("command-failure-crate-claimed"));
                            return 1;
                        }
                        msg = dataManager.getMsgStr("command-lastcrate-inactive")
                                .replace("<crate>", dataManager.getLastTierName())
                                .replace("<time>", timeDiffFormat((System.currentTimeMillis() - dataManager.getLastCrateDrop()) / 1000, false))
                                .replace("<claimPlayer>", dataManager.getLastClaimedPlayer());
                    } else {
                        LootCrate crate = dataManager.getCurrentCrate().get();
                        BlockPos pos = crate.location.around.getBlockPos();
                        msg = dataManager.getMsgStr("command-lastcrate-active")
                                .replace("<crate>", crate.crateTier.name)
                                .replace("<time>", timeDiffFormat((System.currentTimeMillis() - crate.startTime) / 1000, false))
                                .replace("<x>", pos.getX() + "").replace("<y>", pos.getY() + "")
                                .replace("<z>", pos.getZ() + "")
                                .replace("<world>", SimpleWorld.getDisplayFromRegistry(crate.location.worldName));
                    }

                    src.sendMessage(Text.literal(msg));
                    return 1;
                })));

        // EndCrate
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("endcrate")
                .executes(context -> {
                    ServerCommandSource src = context.getSource();
                    if (!canUseCommand(src, "endcrate")) return 1;

                    if (!isCrateActive()) {
                        src.sendMessage(dataManager.getMsg("command-failure-crate-end-inactive"));
                        return 1;
                    }

                    LootCrate crate = dataManager.getCurrentCrate().get();
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        player.sendMessage(dataManager.getMsg("command-crate-end-ended"));
                    }
                    crate.remove();
                    dataManager.setCurrentCrateEmpty();
                    return 1;
                })));


        // ForceCrate
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("forcecrate")
                .then(argument("crateTier", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerCommandSource src = context.getSource();
                            if (!canUseCommand(src, "forcecrate")) return 1;
                            String inputTier = StringArgumentType.getString(context, "crateTier");
                            CrateTier createTier = null;
                            for (CrateTier crateTier : dataManager.getGeneral().crateTiers) {
                                if (crateTier.name.equals(inputTier)) {
                                    createTier = crateTier;
                                    break;
                                }
                            }
                            if (createTier == null) {
                                src.sendMessage(dataManager.getMsg("command-failure-crate-name"));
                                return 1;
                            }
                            if (isCrateActive()) {
                                dataManager.getCurrentCrate().get().remove();
                                dataManager.setCurrentCrateEmpty();
                            }
                            dataManager.getCrateManager().spawnCrate(createTier, Optional.ofNullable(src));
                            return 1;
                        }))));


        Task.builder()
                .infinite()
                .delay(60 * 20L)
                .interval(60 * 20L)
                .execute(() -> {
                    dataManager.getCrateManager().run();
                })
                .build();
    }

    public static boolean isCrateActive() {
        return dataManager.getCurrentCrate().isPresent();
    }


    private boolean canUseCommand(ServerCommandSource source, String name) {
        if (!source.isExecutedByPlayer()) return true;
        if (!Permissions.check(source, BASE_PERMISSION + name)) {
            source.sendError(dataManager.getMsg("permission-command-use"));
            return false;
        }
        return true;
    }

}
