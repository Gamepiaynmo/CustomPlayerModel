package com.gpiay.cpm.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ModelEntry;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.QueryModelPacket;
import com.gpiay.cpm.util.HashHelper;
import com.gpiay.cpm.util.exception.TranslatableException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerModelManager extends ModelManager {
    protected static final Map<String, ModelInfo> modelInfos = Maps.newHashMap();
    public static final Map<String, String> permissionNodes = Maps.newHashMap();

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
    }

    public boolean hasPermission(String modelId, PlayerEntity player) {
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
                    modelList.add(new ModelEntry(modelInfo, local));
            }
        }

        return modelList;
    }

    private Optional<ModelInfo> loadModelInfoFromCache(String modelId) {
        File modelFile = findModelFileFromCache(modelId);
        if (modelFile != null) {
            try {
                ModelInfo modelInfo = ModelInfo.fromZipFile(modelId, modelFile);
                modelInfo.isCached = true;
                return Optional.of(modelInfo);
            } catch (Exception e) {
                CPMMod.warn(e);
                deleteCachedModel(modelId);
            }
        }

        return Optional.empty();
    }

    public Optional<ModelInfo> getModelInfo(String modelId, ServerPlayerEntity sender) {
        ModelInfo res = modelInfos.get(modelId);
        if (res != null)
            return Optional.of(res);

        Optional<ModelInfo> opt = loadModelInfoFromCache(modelId);
        if (opt.isPresent()) {
            modelInfos.put(modelId, opt.get());
            return opt;
        }

        if (sender != null) {
            NetworkHandler.send(sender, new QueryModelPacket(modelId));
        } else {
            CPMMod.warn(new TranslatableException("error.cpm.loadModel.notfound", modelId));
        }

        return Optional.empty();
    }
}
