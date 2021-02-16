package com.gpiay.cpm.hook;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.CommonConfig;
import com.gpiay.cpm.server.ServerCPMCapability;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;

import java.util.Optional;

public class LivingEntityHook {
    public static EntitySize getEntitySize(LivingEntity entity, EntitySize orinSize, Pose pose) {
        Optional<ICPMCapability> capability = entity.getCapability(CPMCapability.CAPABILITY).resolve();
        if (ServerConfig.CUSTOM_BOUNDING_BOX.get() && capability.isPresent()) {
            if (entity.world.isRemote) {
                if (CPMMod.cpmClient.isServerModded) {
                    ModelInstance model = ((ClientCPMCapability) capability.get()).getModel();
                    if (model != null)
                        return model.getModelPack().getEntitySize(entity, pose, orinSize, capability.get().getScale());
                }
            } else {
                ModelInfo model = ((ServerCPMCapability) capability.get()).getModel();
                if (model != null)
                    return model.getEntitySize(entity, pose, orinSize, capability.get().getScale());
            }
        }

        return orinSize;
    }

    public static float getEntityEyeHeight(LivingEntity entity, float orinHeight, Pose pose, EntitySize sizeIn) {
        Optional<ICPMCapability> capability = entity.getCapability(CPMCapability.CAPABILITY).resolve();
        if (ServerConfig.CUSTOM_EYE_HEIGHT.get() && capability.isPresent()) {
            if (entity.world.isRemote) {
                if (CPMMod.cpmClient.isServerModded) {
                    ModelInstance model = ((ClientCPMCapability) capability.get()).getModel();
                    if (model != null)
                        return model.getModelPack().getEntityEyeHeight(entity, pose, orinHeight, capability.get().getScale());
                }
            } else {
                ModelInfo model = ((ServerCPMCapability) capability.get()).getModel();
                if (model != null)
                    return model.getEntityEyeHeight(entity, pose, orinHeight, capability.get().getScale());
            }
        }

        return orinHeight;
    }
}
