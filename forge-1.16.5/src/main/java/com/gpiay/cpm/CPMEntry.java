package com.gpiay.cpm;

import com.gpiay.cpm.client.CPMClient;
import com.gpiay.cpm.config.ClientConfig;
import com.gpiay.cpm.event.ClientEvents;
import com.gpiay.cpm.config.CommonConfig;
import com.gpiay.cpm.config.ServerConfig;
import com.gpiay.cpm.event.ServerEvents;
import com.gpiay.cpm.item.CPMItems;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.server.*;
import com.gpiay.cpm.entity.CPMCapabilityProvider;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod(CPMMod.MOD_ID)
public class CPMEntry {
    public CPMEntry() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInterModProcess);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);
        CPMMod.cpmServer = new CPMServer();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());

        PermissionAPI.registerNode("cpm.command.selectSelf", CommonConfig.SELECT_SELF.get(), "Permission for selecting own model.");
        PermissionAPI.registerNode("cpm.command.selectOthers", CommonConfig.SELECT_OTHERS.get(), "Permission for selecting other's model.");
        PermissionAPI.registerNode("cpm.command.scaleSelf", CommonConfig.SCALE_SELF.get(), "Permission for resizing own model.");
        PermissionAPI.registerNode("cpm.command.scaleOthers", CommonConfig.SCALE_OTHERS.get(), "Permission for resizing other's model.");
        PermissionAPI.registerNode("cpm.command.refreshModels", CommonConfig.REFRESH.get(), "Permission for refreshing local model files.");
        PermissionAPI.registerNode("cpm.command.createItem", CommonConfig.CREATE_ITEM.get(), "Permission for creating model changing items.");

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        for (CommonConfig.ModelPermission node : CommonConfig.MODEL_PERMISSIONS.get()) {
            String permissionNode = "cpm.model." + node.node;
            PermissionAPI.registerNode(permissionNode, DefaultPermissionLevel.ALL, "Model Permission Node");
            for (String model : node.models)
                ServerModelManager.permissionNodes.put(model, permissionNode);
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
            CPMMod.cpmClient = new CPMClient();
            MinecraftForge.EVENT_BUS.register(new ClientEvents());

            CPMMod.cpmClient.selectModelKey = new KeyBinding(
                    "key.cpm.selectModel", KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                    InputMappings.getKey("key.keyboard.m"), "key.cpm.category");
            ClientRegistry.registerKeyBinding(CPMMod.cpmClient.selectModelKey);
            for (int i = 0; i < CPMMod.customKeyCount; i++) {
                CPMMod.cpmClient.customKeys[i] = new KeyBinding("key.cpm.customKey" + (i + 1), KeyConflictContext.IN_GAME,
                        KeyModifier.NONE, i < 4 ? InputMappings.getKey("key.keyboard.f" + (i + 6)) :
                        InputMappings.UNKNOWN, "key.cpm.category");
                ClientRegistry.registerKeyBinding(CPMMod.cpmClient.customKeys[i]);
            }
        });

        CPMItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
        CPMCapabilityProvider.registerCapability();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        CPMMod.cpmClient.registerRenderLayers();
    }

    public void onInterModProcess(InterModProcessEvent event) {
        InterModComms.getMessages(CPMMod.MOD_ID).forEach(message -> {
            Object object = message.getMessageSupplier().get();
            if (object instanceof String) {
                ResourceLocation location = new ResourceLocation((String) object);
                if (ForgeRegistries.ENTITIES.containsKey(location)) {
                    EntityType<?> type = ForgeRegistries.ENTITIES.getValue(location);
                    if ("blacklist".equals(message.getMethod())) {
                        CPMMod.cpmServer.blacklist.add(type);
                    }
                }
            }
        });

        List<? extends String> entities = CommonConfig.BLACKLIST.get();
        for (String entity : entities) {
            ResourceLocation location = new ResourceLocation(entity);
            if (ForgeRegistries.ENTITIES.containsKey(location))
                CPMMod.cpmServer.blacklist.add(ForgeRegistries.ENTITIES.getValue(location));
        }
    }

    public boolean hasPermission(String modelId, PlayerEntity player) {
        if (ServerModelManager.permissionNodes.containsKey(modelId)) {
            return PermissionAPI.hasPermission(player, ServerModelManager.permissionNodes.get(modelId));
        }

        return true;
    }
}
