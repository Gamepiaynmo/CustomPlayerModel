package com.gpiay.cpm.server;

import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public class CPMServer {
    public MinecraftServer server = null;

    public ServerModelManager modelManager = new ServerModelManager(this);

    public final Set<EntityType<?>> blacklist = Sets.newHashSet();

    public CPMServer() {
        if (!CPMMod.MODEL_DIR.isDirectory() && !CPMMod.MODEL_DIR.mkdirs())
            CPMMod.LOGGER.warn("Create model directory failed.");
        if (!CPMMod.CACHE_DIR.isDirectory() && !CPMMod.CACHE_DIR.mkdirs())
            CPMMod.LOGGER.warn("Create cache directory failed.");

        ModelManager.initLocalModels();
        modelManager.initLocalModelInfo();
    }
}
