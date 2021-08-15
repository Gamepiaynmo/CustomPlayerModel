package com.gpiay.cpm.event;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ServerCPMCapability;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.UpdateModelPacket;
import com.gpiay.cpm.entity.CPMCapabilityProvider;
import com.gpiay.cpm.server.CPMCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerEvents {
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        CPMMod.cpmServer.server = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        CPMMod.cpmServer.server = null;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CPMCommand.registerCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    @SubscribeEvent
    public void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof LivingEntity && !CPMMod.cpmServer.blacklist.contains(entity.getType())) {
            event.addCapability(new ResourceLocation(CPMMod.MOD_ID, "model"), new CPMCapabilityProvider((LivingEntity) entity));
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        AttachmentProvider.getEntityAttachment(event.getOriginal()).ifPresent((oldCap) -> {
            AttachmentProvider.getEntityAttachment(event.getPlayer()).ifPresent((newCap) -> {
                CompoundNBT nbt = new CompoundNBT();
                oldCap.writeToNBT(nbt);
                newCap.readFromNBT(nbt);
            });
        });
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    @SubscribeEvent
    public void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        Entity entity = event.getTarget();
        if (entity instanceof LivingEntity) {
            AttachmentProvider.getEntityAttachment(entity).ifPresent(cap -> {
                if (((ServerCPMCapability) cap).getModel() != null)
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                            new UpdateModelPacket((LivingEntity) entity));
            });
        }
    }
}
