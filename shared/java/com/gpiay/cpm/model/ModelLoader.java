package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.element.BedrockModelParser;
import com.gpiay.cpm.model.element.ItemModel;
import com.gpiay.cpm.model.element.ModelPart;
import com.gpiay.cpm.model.element.ParticleEmitter;
import com.gpiay.cpm.model.skeleton.EnumSkeleton;
import com.gpiay.cpm.util.JsonHelper;
import com.gpiay.cpm.util.exception.TranslatableException;
import com.gpiay.cpm.util.exception.TranslatableJsonException;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.script.Compilable;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModelLoader {

    static String getStem(String filename) {
        int index = filename.lastIndexOf(".");
        if (index > 0)
            filename = filename.substring(0, index);
        return filename;
    }

    static String getSuffix(String filename) {
        int index = filename.lastIndexOf(".");
        if (index > 0)
            filename = filename.substring(index + 1);
        return filename;
    }

    public static ModelPack fromDirectory(File directory) throws IOException, ScriptException {
        return loadFromEntry(ModelInfo.loadFromDirectory(directory));
    }

    public static ModelPack fromZipFile(String id, File zipFile) throws IOException, ScriptException {
        return loadFromEntry(ModelInfo.loadFromZipFile(id, zipFile));
    }

    public static ModelPack fromZipMemory(String id, byte[] data) throws IOException, ScriptException {
        return loadFromEntry(ModelInfo.loadFromZipMemory(id, data));
    }

    static ModelPack loadFromEntry(ModelInfo.ModelLoadData loadData) throws IOException, ScriptException {
        ModelInfo.IResource mainModel = null;
        List<ModelInfo.IResource> modelParts = Lists.newArrayList();
        List<ModelInfo.IResource> textures = Lists.newArrayList();
        ModelInfo.IResource script = null;

        for (Pair<String, ModelInfo.IResource> pair : loadData.resources) {
            String name = pair.getKey();
            switch (getSuffix(name)) {
                case "json":
                    if (name.equalsIgnoreCase("model.json"))
                        mainModel = pair.getValue();
                    else modelParts.add(pair.getValue());
                    break;
                case "jpg":
                case "png":
                case "bmp":
                    textures.add(pair.getValue());
                    break;
                case "js":
                    if (name.equalsIgnoreCase("animation.js"))
                        script = pair.getValue();
                    break;
            }
        }

        if (mainModel == null)
            throw new TranslatableException("error.cpm.loadModel.noModel");

        return fromResource(loadData.modelId, mainModel, modelParts, textures, script);
    }

    private static ModelPack fromResource(String id, ModelInfo.IResource mainModel, List<ModelInfo.IResource> modelParts, List<ModelInfo.IResource> textures, ModelInfo.IResource script) throws IOException, ScriptException {
        JsonReader mainModelJson = new JsonReader(mainModel.getReader());
        mainModelJson.setLenient(true);

        ModelPack modelPack = fromJson(id, mainModelJson, modelParts, textures, script);
        mainModelJson.close();
        modelPack.id = id;
        return modelPack;
    }

    private static ModelPack fromJson(String id, JsonReader mainModel, List<ModelInfo.IResource> modelParts, List<ModelInfo.IResource> textures, ModelInfo.IResource script) throws IOException, ScriptException {
        ModelPack modelPack = new ModelPack();

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ModelInfo.IResource texture : textures) {
            String textureName = texture.getName();
            ResourceLocation location = new ResourceLocation(CPMMod.MOD_ID, (id + "/" + textureName).toLowerCase());
            modelPack.textures.put(textureName, location);
            if (textureManager.getTexture(location) == null) {
                CustomTexture customTexture = CustomTexture.fromInputStream(texture.openInputStream());
                textureManager.register(location, customTexture);
            }
        }

        if (script != null) {
            modelPack.animScript = ((Compilable) CPMMod.cpmClient.scriptEngine).compile(script.getReader());
        }

        JsonHelper.readObject(mainModel, key -> {
            switch (key) {
                case "skeleton": {
                    JsonHelper.readObject(mainModel, skeletonKey -> {
                        switch (skeletonKey) {
                            case "type": {
                                String skeletonId = mainModel.nextString();
                                Optional<EnumSkeleton> type = EnumSkeleton.getBySkeletonId(skeletonId);
                                modelPack.skeletonType = type.orElseThrow(() ->
                                        new TranslatableJsonException("error.cpm.loadModel.unknownSkeleton", mainModel, skeletonId));
                                break;
                            }
                            case "param": {
                                JsonHelper.readObject(mainModel, name -> {
                                    modelPack.skeletonParam.put(name, (float) mainModel.nextDouble());
                                });

                                break;
                            }
                            case "attachments": {
                                JsonHelper.readObject(mainModel, attachKey -> {
                                    EnumAttachment attachment = EnumAttachment.getByAttachmentId(attachKey)
                                            .orElseThrow(() -> new TranslatableJsonException("error.cpm.loadModel.unknownAttachment", mainModel, attachKey));

                                    modelPack.allocateAttachment(attachment);
                                    JsonHelper.readArray(mainModel, () -> {
                                        modelPack.addAttachment(attachment, mainModel.nextString());
                                    });
                                });
                                break;
                            }
                            case "left_arm": {
                                List<String> bones = Lists.newArrayList();
                                JsonHelper.readArray(mainModel, () -> {
                                    bones.add(mainModel.nextString());
                                });

                                modelPack.firstPersonBones.put(HandSide.LEFT, bones);
                                break;
                            }
                            case "right_arm": {
                                List<String> bones = Lists.newArrayList();
                                JsonHelper.readArray(mainModel, () -> {
                                    bones.add(mainModel.nextString());
                                });

                                modelPack.firstPersonBones.put(HandSide.RIGHT, bones);
                                break;
                            }
                            case "eye_position": {
                                modelPack.eyePositionBone = mainModel.nextString();
                                break;
                            }
                            default: mainModel.skipValue();
                        }
                    });

                    break;
                }
                case "parts": {
                    Map<String, JsonReader> partJsons = Maps.newHashMap();
                    for (ModelInfo.IResource modelPart : modelParts) {
                        JsonReader reader = new JsonReader(modelPart.getReader());
                        reader.setLenient(true);
                        partJsons.put(modelPart.getName(), reader);
                    }

                    JsonHelper.readArray(mainModel, () -> {
                        String partName = null;
                        boolean doubleFace = false;

                        mainModel.beginObject();
                        while (mainModel.hasNext()) {
                            String partKey = mainModel.nextName();
                            switch (partKey) {
                                case "double_face": {
                                    doubleFace = mainModel.nextBoolean();
                                    break;
                                }
                                case "name": {
                                    partName = mainModel.nextString();
                                    JsonReader partJson = partJsons.get(partName);
                                    if (partJson == null)
                                        throw new TranslatableJsonException("error.cpm.loadModel.partNotfound", mainModel, partName);
                                    modelPack.modelParts.put(partName, BedrockModelParser.fromJson(partJson, doubleFace));
                                    break;
                                }
                                case "texture": {
                                    String textureName = mainModel.nextString();
                                    ResourceLocation texture = modelPack.textures.get(textureName);
                                    if (texture == null)
                                        texture = new ResourceLocation(textureName);

                                    if (textureManager.getTexture(texture) == null)
                                        throw new TranslatableJsonException("error.cpm.loadModel.noTexture", mainModel, textureName);

                                    for (ModelPart part : modelPack.modelParts.get(partName))
                                        part.texture = textureName;
                                    break;
                                }
                                default: mainModel.skipValue();
                            }
                        }

                        mainModel.endObject();
                    });

                    for (JsonReader reader : partJsons.values())
                        reader.close();

                    break;
                }
                case "shadow_size": {
                    modelPack.shadowSize = (float) mainModel.nextDouble();
                    break;
                }
                case "particles": {
                    JsonHelper.readArray(mainModel, () -> {
                        ParticleEmitter particle = new ParticleEmitter();
                        JsonHelper.readObject(mainModel, partKey -> {
                            switch (partKey) {
                                case "name":
                                    particle.name = mainModel.nextString();
                                    break;
                                case "parent":
                                    particle.parentName = mainModel.nextString();
                                    break;
                                case "texture":
                                    particle.texture = mainModel.nextString();
                                    ResourceLocation texture = modelPack.textures.get(particle.texture);
                                    if (texture == null)
                                        texture = new ResourceLocation(particle.texture);

                                    if (textureManager.getTexture(texture) == null)
                                        throw new TranslatableJsonException("error.cpm.loadModel.noTexture", mainModel, particle.texture);
                                    break;
                                case "position_range":
                                    mainModel.beginArray();
                                    particle.posRange = new Vector3d(
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble());
                                    mainModel.endArray();
                                    break;
                                case "direction_range":
                                    particle.dirRange = mainModel.nextDouble();
                                    break;
                                case "angle":
                                    mainModel.beginArray();
                                    particle.angle = new double[]{
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble()};
                                    mainModel.endArray();
                                    break;
                                case "speed":
                                    mainModel.beginArray();
                                    particle.speed = new double[]{
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble()};
                                    mainModel.endArray();
                                    break;
                                case "rotation_speed":
                                    mainModel.beginArray();
                                    particle.rotSpeed = new double[]{
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble()};
                                    mainModel.endArray();
                                    break;
                                case "life_span":
                                    mainModel.beginArray();
                                    particle.lifeSpan = new double[]{
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble()};
                                    mainModel.endArray();
                                    break;
                                case "density":
                                    particle.density = mainModel.nextDouble();
                                    break;
                                case "animation":
                                    mainModel.beginArray();
                                    particle.animation = new int[]{
                                            mainModel.nextInt(),
                                            mainModel.nextInt()};
                                    mainModel.endArray();
                                    break;
                                case "color":
                                    mainModel.beginArray();
                                    for (int i = 0; i < 4; i++) {
                                        mainModel.beginArray();
                                        particle.color[i] = new double[]{
                                                mainModel.nextDouble(),
                                                mainModel.nextDouble()};
                                        mainModel.endArray();
                                    }
                                    mainModel.endArray();
                                    break;
                                case "size":
                                    mainModel.beginArray();
                                    particle.size = new double[]{
                                            mainModel.nextDouble(),
                                            mainModel.nextDouble()};
                                    mainModel.endArray();
                                    break;
                                case "gravity":
                                    particle.gravity = mainModel.nextDouble();
                                    break;
                                case "collide":
                                    particle.collide = mainModel.nextBoolean();
                                    break;
                                default:
                                    mainModel.skipValue();
                                    break;
                            }
                        });

                        modelPack.particleEmitters.add(particle);
                    });
                    break;
                }
                case "items": {
                    JsonHelper.readArray(mainModel, () -> {
                        ItemModel itemModel = new ItemModel();
                        JsonHelper.readObject(mainModel, itemKey -> {
                            switch (itemKey) {
                                case "name":
                                    itemModel.name = mainModel.nextString();
                                    break;
                                case "parent":
                                    itemModel.parentName = mainModel.nextString();
                                    break;
                                case "item":
                                    itemModel.itemId = mainModel.nextString();
                                    break;
                                case "enchanted":
                                    itemModel.enchanted = mainModel.nextBoolean();
                                    break;
                                default:
                                    mainModel.skipValue();
                                    break;
                            }
                        });

                        modelPack.itemModels.add(itemModel);
                    });
                    break;
                }
                case "addons": {
                    JsonHelper.readObject(mainModel, boneName -> {
                        String attachName = mainModel.nextString();
                        EnumAttachment attachment = EnumAttachment.getByAttachmentId(attachName)
                                .orElseThrow(() -> new TranslatableJsonException("error.cpm.loadModel.unknownAttachment", mainModel, attachName));
                        modelPack.addons.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(boneName);
                    });
                    break;
                }
                default: {
                    if (ModelInfo.parseModelInfo(mainModel, key, modelPack))
                        mainModel.skipValue();
                    break;
                }
            }
        });

        modelPack.validate();
        return modelPack;
    }
}
