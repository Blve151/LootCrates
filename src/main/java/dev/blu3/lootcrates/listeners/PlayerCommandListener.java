package dev.blu3.lootcrates.listeners;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.blu3.lootcrates.LootCrates;
import dev.blu3.lootcrates.enums.ActionResult;
import dev.blu3.lootcrates.events.interfaces.ServerCommandCallback;
import dev.blu3.lootcrates.utils.objects.LootCrate;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static dev.blu3.lootcrates.LootCrates.*;

public class PlayerCommandListener implements ServerCommandCallback {


    public void register(){
        ServerCommandCallback.EVENT.register(this);
    }

    private String getTimeString(int seconds) {
        long hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        int secs = seconds % 60;
        StringBuilder s = new StringBuilder();
        if (hours != 0) {
            s.append(hours).append('h');
        }
        if (minutes != 0) {
            s.append(minutes).append('m');
            return s.toString();
        }
        if (hours == 0) {
            s.append(secs);
            s.append('s');
        }
        return s.toString();
    }

    @Override
    public ActionResult execute(ServerPlayerEntity player, String command) {
        if(Permissions.check(player, "lootcrates.bypass")) return ActionResult.PASS;
        if(!isCrateActive()) return ActionResult.PASS;
        String[] splitCmd = command.split(" ");
        String root = splitCmd[0];
        String end = splitCmd[splitCmd.length - 1];
        LootCrate crate = dataManager.getCurrentCrate().get();
        if(crate.forfeit.contains(player.getUuid())) return ActionResult.PASS;
        if(dataManager.getGeneral().blacklistedCommands.contains(root)){
            if(end.equalsIgnoreCase("confirm")){
                crate.forfeit.add(player.getUuid());
                dataManager.setCurrentCrate(crate);
            }else{
                player.sendMessage(dataManager.getMsg("blacklisted-command-use"));
                return ActionResult.FAIL;
            }
        } else if (dataManager.getGeneral().cooldownCommands.contains(root)){
            HashMap<String, Long> cmds = new HashMap<>();
            if(!LootCrates.cooldowns.containsKey(player.getUuid())){
                cmds.put(root, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
                cooldowns.put(player.getUuid(), cmds);
            }else{
                cmds = cooldowns.get(player.getUuid());
                long when = cmds.get(root);
                long now = System.currentTimeMillis();

                if (now < when) {
                    player.sendMessage(Text.literal(dataManager.getMsgStr("cooldown-command-use")
                            .replace("<time>", getTimeString((int) TimeUnit.MILLISECONDS.toSeconds(when - now)))
                            .replace("<crate>", crate.crateTier.name)));
                    return ActionResult.FAIL;
                } else {
                    cmds.remove(root);
                    cooldowns.put(player.getUuid(), cmds);
                }
            }
        }
        return ActionResult.PASS;
    }
}
