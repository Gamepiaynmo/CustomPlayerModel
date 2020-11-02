package com.gpiay.cpm.model.skeleton;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;
import java.util.function.Function;

public enum EnumSkeleton {
    PLAYER("biped", BipedSkeleton::new),
    ZOMBIE("zombie", ZombieSkeleton::new),
    SLIME("slime", SlimeSkeleton::new),
    QUADRUPED("quadruped", QuadrupedSkeleton::new);

    private final String skeletonId;
    private final Function<Map<String, Float>, Skeleton<? extends EntityModel<LivingEntity>>> factory;

    EnumSkeleton(String id, Function<Map<String, Float>, Skeleton<? extends EntityModel<LivingEntity>>> factory) {
        this.skeletonId = id;
        this.factory = factory;
    }

    public static LazyOptional<EnumSkeleton> getBySkeletonId(String id) {
        for (EnumSkeleton skeleton : values()) {
            if (skeleton.skeletonId.equals(id))
                return LazyOptional.of(() -> skeleton);
        }

        return LazyOptional.empty();
    }

    public Skeleton<? extends EntityModel<LivingEntity>> instantiate(Map<String, Float> param) {
        return factory.apply(param);
    }
}
