package com.gpiay.cpm.client.render.item;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.DolphinCarriedItemLayer;
import net.minecraft.client.renderer.entity.model.DolphinModel;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;

public class CPMDolphinItemLayer extends CPMItemLayer<DolphinEntity, DolphinModel<DolphinEntity>> {
    public CPMDolphinItemLayer(IEntityRenderer<DolphinEntity, DolphinModel<DolphinEntity>> entityRendererIn, DolphinCarriedItemLayer orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(DolphinEntity entityIn) {
        return entityIn.getMainArm() == HandSide.RIGHT ? entityIn.getMainHandItem() : entityIn.getOffhandItem();
    }
}
