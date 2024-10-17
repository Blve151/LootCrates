package dev.blu3.lootcrates.mixin;


import dev.blu3.lootcrates.enums.ActionResult;
import dev.blu3.lootcrates.events.interfaces.ServerCommandCallback;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution",
            at = @At(target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
                    value = "INVOKE"), cancellable = true)
    public void onCommand(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        if(ServerCommandCallback.EVENT.invoker().execute(player, packet.comp_808()) == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
