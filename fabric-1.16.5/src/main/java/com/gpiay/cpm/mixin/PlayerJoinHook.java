package com.gpiay.cpm.mixin;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.ServerInitPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerJoinHook {
    @Inject(at = @At(value = "TAIL"), method = "placeNewPlayer", cancellable = true)
    private  void onPlayerJoin(NetworkManager networkManager, ServerPlayerEntity serverPlayerEntity, CallbackInfo info) {
        NetworkHandler.send(new ServerInitPacket());
    }
}
