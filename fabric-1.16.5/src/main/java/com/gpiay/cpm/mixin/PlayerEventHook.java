package com.gpiay.cpm.mixin;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.CPMComponentProvider;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.ServerInitPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerEventHook {
    @Inject(at = @At(value = "TAIL"), method = "placeNewPlayer")
    private  void onPlayerJoin(NetworkManager networkManager, ServerPlayerEntity serverPlayerEntity, CallbackInfo info) {
        NetworkHandler.send(serverPlayerEntity, new ServerInitPacket());
    }

    @Inject(at = @At(value = "RETURN"), method = "respawn")
    private void onPlayerRespawn(ServerPlayerEntity serverPlayerEntity, boolean isEnd, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (!isEnd)
            CPMComponentProvider.ATTACHMENT.sync(serverPlayerEntity);
    }
}
