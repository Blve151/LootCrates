package dev.blu3.lootcrates.events.interfaces;

import com.mojang.brigadier.StringReader;
import dev.blu3.lootcrates.enums.ActionResult;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public interface ServerCommandCallback {
    Event<ServerCommandCallback> EVENT = EventFactory.createArrayBacked(ServerCommandCallback.class,
            (listeners) -> (player, command) -> {
                for (ServerCommandCallback listener : listeners) {
                    return listener.execute(player, command);
                }
                return ActionResult.PASS;
            });


    ActionResult execute(ServerPlayerEntity player, String command);

}
