package com.gpiay.cpm.server;

import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelManager;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.UpdateModelPacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.CPMCapabilityProvider;
import com.gpiay.cpm.server.capability.ICPMCapability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.List;
import java.util.Set;

public class CPMServer {
    public MinecraftServer server = null;

    public ServerModelManager modelManager = new ServerModelManager(this);

    public final Set<EntityType<?>> applyEntityTypes = Sets.newHashSet();
    private final Set<EntityType<?>> removeEntityTypes = Sets.newHashSet();

    public CPMServer() {
        if (!CPMMod.MODEL_DIR.isDirectory() && !CPMMod.MODEL_DIR.mkdirs())
            CPMMod.LOGGER.warn("Create model directory failed.");
        if (!CPMMod.CACHE_DIR.isDirectory() && !CPMMod.CACHE_DIR.mkdirs())
            CPMMod.LOGGER.warn("Create cache directory failed.");

        ModelManager.initLocalModels();
        modelManager.initLocalModelInfo();
        MinecraftForge.EVENT_BUS.register(this);

        PermissionAPI.registerNode("cpm.command.selectSelf", CommonConfig.SELECT_SELF.get(), "Permission for selecting Own Model.");
        PermissionAPI.registerNode("cpm.command.selectOthers", CommonConfig.SELECT_OTHERS.get(), "Permission for selecting Other's Model.");
        PermissionAPI.registerNode("cpm.command.scaleSelf", CommonConfig.SCALE_SELF.get(), "Permission for resizing Own Model.");
        PermissionAPI.registerNode("cpm.command.scaleOthers", CommonConfig.SCALE_OTHERS.get(), "Permission for resizing Other's Model.");
        PermissionAPI.registerNode("cpm.command.refreshModels", CommonConfig.REFRESH.get(), "Permission for refreshing Local Model Files.");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        CPMCommand.registerCommand(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        server = null;
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    public void processApplyEntities() {
        InterModComms.getMessages(CPMMod.MOD_ID).forEach(message -> {
            Object object = message.getMessageSupplier().get();
            if (object instanceof String) {
                ResourceLocation location = new ResourceLocation((String) object);
                if (ForgeRegistries.ENTITIES.containsKey(location)) {
                    EntityType<?> type = ForgeRegistries.ENTITIES.getValue(location);
                    switch (message.getMethod()) {
                        case "addEntityType":
                            applyEntityTypes.add(type);
                            break;
                        case "removeEntityType":
                            removeEntityTypes.add(type);
                            break;
                    }
                }
            }
        });

        List<? extends String> entities = CommonConfig.APPLY_ENTITIES.get();
        for (String entity : entities) {
            ResourceLocation location = new ResourceLocation(entity);
            if (ForgeRegistries.ENTITIES.containsKey(location))
                applyEntityTypes.add(ForgeRegistries.ENTITIES.getValue(location));
        }

        applyEntityTypes.removeAll(removeEntityTypes);
    }

    @SubscribeEvent
    public void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof LivingEntity && applyEntityTypes.contains(entity.getType())) {
            event.addCapability(new ResourceLocation(CPMMod.MOD_ID, "model"), new CPMCapabilityProvider((LivingEntity) entity));
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        LazyOptional<ICPMCapability> oldSpeedCap = event.getOriginal().getCapability(CPMCapability.CAPABILITY);
        LazyOptional<ICPMCapability> newSpeedCap = event.getPlayer().getCapability(CPMCapability.CAPABILITY);
        newSpeedCap.ifPresent((newCap) -> {
            oldSpeedCap.ifPresent((oldCap) -> {
                newCap.deserializeNBT(oldCap.serializeNBT());
            });
        });

        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                new UpdateModelPacket(event.getPlayer()));
    }

    @SubscribeEvent
    public void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        Entity entity = event.getTarget();
        if (entity instanceof LivingEntity) {
            entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> {
                if (((ServerCPMCapability) cap).getModel() != null)
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                            new UpdateModelPacket((LivingEntity) entity));
            });
        }
    }
}
