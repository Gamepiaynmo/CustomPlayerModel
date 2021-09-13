package com.gpiay.cpm.client.render.item;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.WitchHeldItemLayer;
import net.minecraft.client.renderer.entity.model.WitchModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class CPMWitchItemLayer<T extends LivingEntity> extends CPMItemLayer<T, WitchModel<T>> {
    public CPMWitchItemLayer(IEntityRenderer<T, WitchModel<T>> entityRendererIn, WitchHeldItemLayer<T> orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(T entityIn) {
        return entityIn.getMainHandItem();
    }
}
