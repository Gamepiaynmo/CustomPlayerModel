package com.gpiay.cpm.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.ModelLoader;
import com.gpiay.cpm.model.ModelPack;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.QueryModelPacket;
import com.gpiay.cpm.server.ModelManager;
import com.gpiay.cpm.util.exception.TranslatableException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.util.*;

public class ClientModelManager extends ModelManager {
    protected final Map<String, ModelPack> modelPacks = Maps.newHashMap();
    private final Set<String> queriedModels = Sets.newHashSet();
    public static ModelPack editingModelPack = null;

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
                        Minecraft.getInstance().submit(() -> {
                            ClientPlayerEntity playerEntity = Minecraft.getInstance().player;
                            if (playerEntity != null) {
                                AttachmentProvider.getEntityAttachment(playerEntity).ifPresent(attachment -> {
                                    ClientCPMAttachment clientAttach = (ClientCPMAttachment) attachment;
                                    if (clientAttach.getMainModel().isEmpty() && clientAttach.getModel() != null) {
                                        try {
                                            clientAttach.setMainModel("");
                                            clearEditingModelPack();

                                            CPMMod.startRecordingError();
                                            clientAttach.loadEditingModel(cpmClient.editingModel.getName());
                                            for (Exception e : CPMMod.endRecordingError())
                                                throw e;
                                            Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("text.cpm.modelReloaded"));
                                        } catch (Exception e) {
                                            CPMMod.print(e);
                                        }
                                    }
                                });
                            }
                        });
                    }

                    file = file.getParentFile();
                } while (file != null);
            }
        }, 1000);

        this.cpmClient = cpmClient;
    }

    private Optional<ModelPack> loadModelPackFromCache(String modelId) {
        File modelFile = findModelFileFromCache(modelId);
        if (modelFile != null) {
            try {
                ModelPack modelPack = ModelLoader.fromZipFile(modelId, modelFile);
                return Optional.of(modelPack);
            } catch (Exception e) {
                CPMMod.warn(e);
                deleteCachedModel(modelId);
            }
        }

        return Optional.empty();
    }

    public Optional<ModelPack> loadEditingModelPack(String modelId) {
        File modelDir = new File(CPMMod.MODEL_DIR, modelId);
        if (modelDir.isDirectory()) {
            cpmClient.editingModel = modelDir;
            try {
                ModelPack modelPack = ModelLoader.fromDirectory(modelDir);
                return Optional.of(modelPack);
            } catch (Exception e) {
                CPMMod.warn(e);
            }
        }

        return Optional.empty();
    }

    public Optional<ModelPack> getModelPack(String modelId) {
        ModelPack res = modelPacks.get(modelId);
        if (res != null)
            return Optional.of(res);

        Optional<ModelPack> opt = loadModelPackFromCache(modelId);
        if (opt.isPresent()) {
            queriedModels.remove(modelId);
            modelPacks.put(modelId, opt.get());
            return opt;
        }

        if (CPMMod.cpmClient.isServerModded) {
            if (!queriedModels.contains(modelId)) {
                NetworkHandler.send(new QueryModelPacket(modelId));
                queriedModels.add(modelId);
            }
        } else {
            CPMMod.warn(new TranslatableException("error.cpm.loadModel.notfound", modelId));
        }

        return Optional.empty();
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
                } catch (Exception e) {
                    CPMMod.warn(e);
                }
            }

            return modelList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static void clearEditingModelPack() {
        if (editingModelPack != null) {
            editingModelPack.release();
            editingModelPack = null;
        }
    }

    static int tickTimer = 0;
    public void tick() {
        ClientWorld world = Minecraft.getInstance().level;

        if (++tickTimer % 1200 == 0) {
            if (world != null) {
                for (ModelPack modelPack : modelPacks.values())
                    modelPack.refCnt = 0;

                for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
                    AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment -> {
                        ModelInstance model = ((ClientCPMAttachment) attachment).getModel();
                        if (model != null)
                            model.getModelPack().refCnt++;
                    });
                }

                Iterator<Map.Entry<String, ModelPack>> iter = modelPacks.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, ModelPack> entry = iter.next();
                    if (entry.getValue().refCnt <= 0) {
                        entry.getValue().release();
                        iter.remove();
                    }
                }
            }

            tickTimer = 0;
        }

        if (world == null) {
            if (!modelPacks.isEmpty()) {
                for (ModelPack modelPack : modelPacks.values())
                    modelPack.release();

                modelPacks.clear();
            }
            
            clearEditingModelPack();
        }
    }
}
