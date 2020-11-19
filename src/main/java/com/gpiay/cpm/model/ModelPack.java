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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

import javax.script.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelPack extends ModelInfo {
    public int refCnt = 0;

    EnumSkeleton skeletonType;
    final Map<String, Float> skeletonParam = Maps.newHashMap();
    final Map<String, ResourceLocation> textures = Maps.newHashMap();
    final Map<String, List<BedrockModel>> modelParts = Maps.newHashMap();
    final Map<EnumAttachment, List<String>> attachments = Maps.newEnumMap(EnumAttachment.class);
    final List<ParticleEmitter> particleEmitters = Lists.newArrayList();
    final List<ItemModel> itemModels = Lists.newArrayList();
    final Map<HandSide, List<String>> firstPersonBones = Maps.newEnumMap(HandSide.class);
    CompiledScript animScript;
    public float shadowSize = -1;

    void allocateAttachment(EnumAttachment attachment) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList());
    }

    void addAttachment(EnumAttachment attachment, String boneName) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(boneName);
    }

    public ResourceLocation getTexture(String texture) {
        return textures.get(texture);
    }

    public ModelInstance instantiate(LivingEntity entity) {
        ModelInstance instance = new ModelInstance(this, entity);
        instance.skeleton = skeletonType.instantiate(skeletonParam);

        Map<String, BoneInfo> boneInfoMap = Maps.newHashMap();
        for (Map.Entry<String, List<BedrockModel>> entry : modelParts.entrySet()) {
            ModelPartInstance modelInstance = new ModelPartInstance();
            instance.partMap.put(entry.getKey(), modelInstance);
            for (BedrockModel model : entry.getValue()) {
                modelInstance.texture = model.texture;
                for (BoneInfo boneInfo : model.bones) {
                    boneInfoMap.put(boneInfo.getId(), boneInfo);
                    ModelBone bone = boneInfo.instantiate(instance, instance::getBone, boneInfoMap::get);
                    instance.boneList.add(bone);
                    instance.boneMap.put(bone.getId(), bone);
                    modelInstance.bones.add(bone);
                }
            }
        }

        for (EnumAttachment attachment : EnumAttachment.values()) {
            List<String> boneList = attachments.get(attachment);
            if (boneList != null) {
                for (String boneName : boneList) {
                    ModelBone bone = instance.boneMap.get(boneName);
                    if (bone != null) {
                        instance.addAttachment(attachment, bone);
                        bone.setCalculateTransform();
                    }
                }
            } else {
                instance.skeleton.addAttachments(attachment, instance);
                for (ModelBone bone : instance.getAttachments(attachment)) {
                    instance.boneMap.put(bone.getId(), bone);
                    instance.boneList.add(bone);
                }
            }
        }

        for (HandSide hand : HandSide.values()) {
            List<String> boneList = firstPersonBones.get(hand);
            if (boneList == null)
                boneList = instance.skeleton.getFirstPersonBones(hand);

            Set<ModelBone> boneSet = Sets.newHashSet();
            for (ModelBone bone : instance.boneList) {
                IModelBone parent = bone.getParent();
                if (boneList.contains(bone.getId()) || boneList.contains(parent.getId()) ||
                        (parent instanceof ModelBone && boneSet.contains(parent)))
                    boneSet.add(bone);
            }

            instance.firstPersonBones.put(hand, boneSet);
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

                instance.evaluateAnimation(instance.initFunc);
            }
        } catch (ScriptException e) {
            CPMMod.warn(new TranslatableException("error.cpm.script.eval", e, name));
        }

        return instance;
    }

    public void release() {
        TextureManager textureManager = Minecraft.getInstance().textureManager;
        for (ResourceLocation texture : textures.values())
            textureManager.deleteTexture(texture);
    }
}
