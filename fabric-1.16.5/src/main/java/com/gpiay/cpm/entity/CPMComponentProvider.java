package com.gpiay.cpm.entity;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class CPMComponentProvider implements EntityComponentInitializer {
    public static final ComponentKey<ICPMComponent> ATTACHMENT =
            ComponentRegistry.getOrCreate(new ResourceLocation("cpm", "attachment"), ICPMComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(LivingEntity.class, ATTACHMENT, (livingEntity -> livingEntity.level.isClientSide ?
                new ClientCPMComponent(livingEntity) : new ServerCPMComponent(livingEntity)));

        registry.setRespawnCopyStrategy(ATTACHMENT, RespawnCopyStrategy.LOSSLESS_ONLY);
    }
}
