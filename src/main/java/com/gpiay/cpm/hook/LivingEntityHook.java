package com.gpiay.cpm.hook;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.ServerCPMCapability;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;

public class LivingEntityHook {
    public static EntitySize getEntitySize(LivingEntity entity, EntitySize orinSize, Pose pose) {
        ICPMCapability capability = entity.getCapability(CPMCapability.CAPABILITY).orElse(null);
        if (ServerConfig.CUSTOM_BOUNDING_BOX.get() && capability != null) {
            if (entity.world.isRemote) {
                ModelInstance model = ((ClientCPMCapability) capability).getModel();
                if (model != null)
                    return model.getModelPack().getEntitySize(entity, pose, orinSize, capability.getScale());
            } else {
                ModelInfo model = ((ServerCPMCapability) capability).getModel();
                if (model != null)
                    return model.getEntitySize(entity, pose, orinSize, capability.getScale());
            }
        }

        return orinSize;
    }

    public static float getEntityEyeHeight(LivingEntity entity, float orinHeight, Pose pose, EntitySize sizeIn) {
        ICPMCapability capability = entity.getCapability(CPMCapability.CAPABILITY).orElse(null);
        if (ServerConfig.CUSTOM_EYE_HEIGHT.get() && capability != null) {
            if (entity.world.isRemote) {
                ModelInstance model = ((ClientCPMCapability) capability).getModel();
                if (model != null)
                    return model.getModelPack().getEntityEyeHeight(entity, pose, orinHeight, capability.getScale());
            } else {
                ModelInfo model = ((ServerCPMCapability) capability).getModel();
                if (model != null)
                    return model.getEntityEyeHeight(entity, pose, orinHeight, capability.getScale());
            }
        }

        return orinHeight;
    }
}
