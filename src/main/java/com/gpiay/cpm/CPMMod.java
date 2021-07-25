package com.gpiay.cpm;

import com.google.common.collect.Lists;
import com.gpiay.cpm.client.CPMClient;
import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.item.CPMItems;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.server.CPMServer;
import com.gpiay.cpm.server.CommonConfig;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

@Mod(CPMMod.MOD_ID)
public class CPMMod {
    public static final String MOD_ID = "cpm";
    public static final String CPM_DIR = "custom-model/";
    public static final File CACHE_DIR = new File(CPM_DIR + "cache");
    public static final File MODEL_DIR = new File(CPM_DIR + "models");

    public static final Logger LOGGER = LogManager.getLogger("CustomPlayerModel");

    public static CPMServer cpmServer = null;
    public static CPMClient cpmClient = null;

    public static final int customKeyCount = 16;

    private static final List<Exception> errorRecord = Lists.newArrayList();

    public CPMMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInterModProcess);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);
        cpmServer = new CPMServer();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
            cpmClient = new CPMClient();
        });

        CPMItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        Networking.registerMessages();
        CPMCapabilityProvider.registerCapability();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        cpmClient.registerRenderLayers();
    }

    public void onInterModProcess(InterModProcessEvent event) {
        cpmServer.processApplyEntities();
    }

    public static void startRecordingError() {
        errorRecord.clear();
    }

    public static List<Exception> endRecordingError() {
        return errorRecord;
    }

    public static void warn(Exception e) {
        CPMMod.LOGGER.warn(e.getMessage(), e);
        errorRecord.add(e);
    }

    public static void print(Exception e) {
        IFormattableTextComponent text = new StringTextComponent(e.toString());
        text.setStyle(text.getStyle().applyFormat(TextFormatting.RED));
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }
}
