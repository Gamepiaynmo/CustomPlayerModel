package com.gpiay.cpm.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ModelEntry;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.QueryModelPacket;
import com.gpiay.cpm.util.HashHelper;
import com.gpiay.cpm.util.exception.TranslatableException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ServerModelManager extends ModelManager {
    protected static final Map<String, ModelInfo> modelInfos = Maps.newHashMap();
    protected static final Map<String, String> permissionNodes = Maps.newHashMap();

    private final CPMServer cpmServer;
    public ServerModelManager(CPMServer cpmServer) {
        super(file -> true, new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                triggerReload(file);
                super.onFileCreate(file);
            }

            @Override
            public void onFileChange(File file) {
                triggerReload(file);
                super.onFileChange(file);
            }

            @Override
            public void onFileDelete(File file) {
                triggerReload(file);
                super.onFileDelete(file);
            }

            private void triggerReload(File file) {
                if (file.getParentFile().equals(CPMMod.MODEL_DIR)) {
                    ModelManager.onModelFileUpdate(file);
                    if (modelFiles.containsKey(file)) {
                        try {
                            String hash = HashHelper.hashCode(file);
                            modelInfos.put(hash, ModelInfo.fromZipFile(hash, file));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }, 8000);

        this.cpmServer = cpmServer;
    }

    public void initLocalModelInfo() {
        for (File modelFile : modelFiles.keySet()) {
            try {
                String hash = HashHelper.hashCode(modelFile);
                modelInfos.put(hash, ModelInfo.fromZipFile(hash, modelFile));
            } catch (Exception ignored) {
            }
        }

        for (CommonConfig.ModelPermission node : CommonConfig.MODEL_PERMISSIONS.get()) {
            String permissionNode = "cpm.model." + node.node;
            PermissionAPI.registerNode(permissionNode, DefaultPermissionLevel.ALL, "Model Permission Node");
            for (String model : node.models)
                permissionNodes.put(model, permissionNode);
        }
    }

    public boolean hasPermission(String modelId, PlayerEntity player) {
        if (permissionNodes.containsKey(modelId)) {
            return PermissionAPI.hasPermission(player, permissionNodes.get(modelId));
        }

        return true;
    }

    public List<ModelEntry> getModelList() {
        return getModelList(true, null);
    }

    public List<ModelEntry> getModelList(PlayerEntity player) {
        return getModelList(false, null);
    }

    private List<ModelEntry> getModelList(boolean local, PlayerEntity player) {
        List<ModelEntry> modelList = Lists.newArrayList();
        for (Map.Entry<String, ModelInfo> entry : modelInfos.entrySet()) {
            String modelId = entry.getKey();
            ModelInfo modelInfo = entry.getValue();

            if (!modelInfo.isCached) {
                if (local || hasPermission(modelId, player))
                    modelList.add(new ModelEntry(modelId, modelInfo, local));
            }
        }

        return modelList;
    }

    private LazyOptional<ModelInfo> loadModelInfoFromCache(String modelId) {
        File modelFile = findModelFileFromCache(modelId);
        if (modelFile != null) {
            try {
                ModelInfo modelInfo = ModelInfo.fromZipFile(modelId, modelFile);
                modelInfo.isCached = true;
                return LazyOptional.of(() -> modelInfo);
            } catch (Exception e) {
                CPMMod.warn(e);
                deleteCachedModel(modelId);
            }
        }

        return LazyOptional.empty();
    }

    protected LazyOptional<ModelInfo> getModelInfo(String modelId, ServerPlayerEntity sender) {
        ModelInfo res = modelInfos.get(modelId);
        if (res != null)
            return LazyOptional.of(() -> res);

        LazyOptional<ModelInfo> opt = loadModelInfoFromCache(modelId);
        if (opt.isPresent()) {
            modelInfos.put(modelId, opt.orElseThrow(() -> new RuntimeException("This should never happen.")));
            return opt;
        }

        if (sender != null) {
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new QueryModelPacket(modelId));
        } else {
            CPMMod.warn(new TranslatableException("error.cpm.loadModel.notfound", modelId));
        }

        return LazyOptional.empty();
    }
}
