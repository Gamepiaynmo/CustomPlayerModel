package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.element.*;
import com.gpiay.cpm.model.skeleton.EnumSkeleton;
import com.gpiay.cpm.util.exception.TranslatableException;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.script.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelPack extends ModelInfo {
    public int refCnt = 0;

    EnumSkeleton skeletonType;
    final Map<String, Float> skeletonParam = Maps.newHashMap();
    final Map<String, ResourceLocation> textures = Maps.newHashMap();
    final Map<String, List<ModelPart>> modelParts = Maps.newHashMap();
    final Map<EnumAttachment, List<String>> attachments = Maps.newEnumMap(EnumAttachment.class);
    final List<ParticleEmitter> particleEmitters = Lists.newArrayList();
    final List<ItemModel> itemModels = Lists.newArrayList();
    final Map<HandSide, List<String>> firstPersonBones = Maps.newEnumMap(HandSide.class);
    final Map<EnumAttachment, List<String>> addons = Maps.newEnumMap(EnumAttachment.class);
    String eyePositionBone = "";
    CompiledScript animScript;
    public float shadowSize = -1;

    void allocateAttachment(EnumAttachment attachment) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList());
    }

    void addAttachment(EnumAttachment attachment, String boneName) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(boneName);
    }

    public ResourceLocation getTexture(String texture) {
        ResourceLocation location = textures.get(texture);
        return location == null ? MissingTextureSprite.getLocation() : location;
    }

    public boolean hasTexture(String texture) {
        return textures.containsKey(texture);
    }

    private void instantiateBase(ModelBase instance) {
        for (Map.Entry<String, List<ModelPart>> entry : modelParts.entrySet()) {
            for (ModelPart model : entry.getValue()) {
                ModelPart.Instance modelInstance = model.instantiate(instance);
                instance.modelPartMap.put(entry.getKey(), modelInstance);
                for (ModelBone boneInfo : model.bones) {
                    ModelBone.Instance bone = boneInfo.instantiate(instance);
                    instance.boneList.add(bone);
                    instance.boneMap.put(bone.name, bone);
                    modelInstance.bones.add(bone);
                }
            }
        }

        for (ParticleEmitter particleEmitter : particleEmitters) {
            instance.particleEmitters.put(particleEmitter.name, particleEmitter.instantiate(instance));
        }

        for (ItemModel itemModel : itemModels) {
            instance.itemModels.put(itemModel.name, itemModel.instantiate(instance));
        }

        instance.scriptContext = new SimpleScriptContext();
        instance.scriptContext.setBindings(CPMMod.cpmClient.scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
        try {
            if (animScript != null) {
                animScript.eval(instance.scriptContext);
                Bindings bindings = instance.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
                instance.initFunc = (JSObject) bindings.get("init");
                instance.updateFunc = (JSObject) bindings.get("update");
                instance.tickFunc = (JSObject) bindings.get("tick");
            }
        } catch (ScriptException e) {
            CPMMod.warn(new TranslatableException("error.cpm.script.eval", e, name));
        }
    }

    public AccessoryInstance instantiateAccessory(LivingEntity entity) {
        AccessoryInstance instance = new AccessoryInstance(this, entity);
        instantiateBase(instance);

        for (EnumAttachment attachment : addons.keySet()) {
            for (String boneName : addons.get(attachment)) {
                ModelBone.Instance bone = instance.boneMap.get(boneName);
                if (bone == null) continue;

                instance.addons.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(bone);
            }
        }

        instance.initMatrix();
        instance.evaluateAnimation(instance.initFunc, defaultScale);
        return instance;
    }

    public ModelInstance instantiateModel(LivingEntity entity) {
        ModelInstance instance = new ModelInstance(this, entity);
        instance.skeleton = skeletonType.instantiate(skeletonParam);
        instantiateBase(instance);
        instance.stuckBones = instance.boneList.stream().filter(bone -> bone.modelBone != null
                && !bone.modelBone.boxes.isEmpty()).collect(Collectors.toList());

        for (EnumAttachment attachment : EnumAttachment.values()) {
            List<String> boneList = attachments.get(attachment);
            if (boneList != null) {
                instance.allocateAttachment(attachment);
                for (String boneName : boneList) {
                    ModelBone.Instance bone = instance.boneMap.get(boneName);
                    if (bone != null) {
                        instance.addAttachment(attachment, bone);
                        bone.setCalculateTransform();
                    }
                }
            } else {
                instance.skeleton.addAttachments(attachment, instance);
                for (ModelBone.Instance bone : instance.getAttachments(attachment)) {
                    instance.boneMap.put(bone.name, bone);
                    instance.boneList.add(bone);
                }
            }
        }

        for (HandSide hand : HandSide.values()) {
            List<String> boneList = firstPersonBones.get(hand);
            if (boneList == null)
                boneList = instance.skeleton.getFirstPersonBones(hand);

            Set<ModelBone.Instance> boneSet = Sets.newHashSet();
            for (ModelBone.Instance bone : instance.boneList) {
                IModelBone parent = bone.getParent();
                if (boneList.contains(bone.name) || boneList.contains(parent.getName()) ||
                        (parent instanceof ModelBone.Instance && boneSet.contains(parent)))
                    boneSet.add(bone);
            }

            instance.firstPersonBones.put(hand, boneSet);
        }

        instance.eyePositionBone = instance.boneMap.get(eyePositionBone);
        if (instance.eyePositionBone != null)
            instance.eyePositionBone.setCalculateTransform();

        instance.initMatrix();
        instance.evaluateAnimation(instance.initFunc, defaultScale);
        return instance;
    }

    public void release() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (ResourceLocation texture : textures.values())
            textureManager.release(texture);
    }

    public void validate() {
        super.validate();
        if (!isAccessory && skeletonType == null)
            throw new TranslatableException("");
    }
}
