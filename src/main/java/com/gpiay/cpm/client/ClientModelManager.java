package com.gpiay.cpm.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.gui.ModelEntry;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelLoader;
import com.gpiay.cpm.model.ModelManager;
import com.gpiay.cpm.model.ModelPack;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.QueryModelPacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.util.exception.TranslatableException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.util.*;

public class ClientModelManager extends ModelManager {
    protected final Map<String, ModelPack> modelPacks = Maps.newHashMap();
    private final Set<String> queriedModels = Sets.newHashSet();

    private final CPMClient cpmClient;
    public ClientModelManager(CPMClient cpmClient) {
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
                do {
                    if (file.equals(cpmClient.editingModel)) {
                        Minecraft.getInstance().enqueue(() -> {
                            ClientPlayerEntity playerEntity = Minecraft.getInstance().player;
                            if (playerEntity != null) {
                                ClientCPMCapability capability = (ClientCPMCapability) playerEntity.getCapability(CPMCapability.CAPABILITY).orElse(null);
                                if (capability.getModelId().isEmpty() && capability.getModel() != null) {
                                    capability.loadEditingModel(cpmClient.editingModel.getName());
                                }
                            }
                        });
                    }

                    file = file.getParentFile();
                } while (file != null);
            }
        }, 1000);

        this.cpmClient = cpmClient;
    }

    private LazyOptional<ModelPack> loadModelPack(String modelId) {
        File modelFile = findModelFile(modelId);
        if (modelFile != null) {
            try {
                ModelPack modelPack = ModelLoader.fromZipFile(modelId, modelFile);
                return LazyOptional.of(() -> modelPack);
            } catch (Exception e) {
                CPMMod.warn(e);
                deleteCachedModel(modelId);
            }
        }

        return LazyOptional.empty();
    }

    protected LazyOptional<ModelPack> loadEditingModelPack(String modelId) {
        File modelDir = new File(CPMMod.MODEL_DIR, modelId);
        if (modelDir.isDirectory()) {
            cpmClient.editingModel = modelDir;
            try {
                ModelPack modelPack = ModelLoader.fromDirectory(modelDir);
                return LazyOptional.of(() -> modelPack);
            } catch (Exception e) {
                CPMMod.warn(e);
            }
        }

        return LazyOptional.empty();
    }

    protected LazyOptional<ModelPack> getModelPack(String modelId) {
        ModelPack res = modelPacks.get(modelId);
        if (res != null)
            return LazyOptional.of(() -> res);

        LazyOptional<ModelPack> opt = loadModelPack(modelId);
        if (opt.isPresent()) {
            queriedModels.remove(modelId);
            modelPacks.put(modelId, opt.orElseThrow(() -> new RuntimeException("This should never happen.")));
            return opt;
        }

        if (CPMMod.cpmClient.isServerModded) {
            if (!queriedModels.contains(modelId)) {
                Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new QueryModelPacket(modelId));
                queriedModels.add(modelId);
            }
        } else {
            CPMMod.warn(new TranslatableException("error.cpm.loadModel.notfound", modelId));
        }

        return LazyOptional.empty();
    }

    public List<ModelEntry> getEditingModelList() {
        try {
            List<ModelEntry> modelList = Lists.newArrayList();
            for (File file : Objects.requireNonNull(CPMMod.MODEL_DIR.listFiles())) {
                try {
                    if (file.isDirectory()) {
                        ModelInfo modelInfo = ModelInfo.fromDirectory(file);
                        ModelEntry modelEntry = new ModelEntry(file.getName(), modelInfo, true);
                        modelEntry.isEditing = true;
                        modelList.add(modelEntry);
                    }
                } catch (Exception ignored) {
                }
            }

            return modelList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    static int tickTimer = 0;
    public void tick() {
        if (++tickTimer % 1200 == 0) {
            Iterator<Map.Entry<String, ModelPack>> iter =  modelPacks.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ModelPack> entry = iter.next();
                if (entry.getValue().getRefCnt() <= 0) {
                    entry.getValue().release();
                    iter.remove();
                }
            }

            tickTimer = 0;
        }
    }
}
