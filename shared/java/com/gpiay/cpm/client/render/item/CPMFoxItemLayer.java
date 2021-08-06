package com.gpiay.cpm.client.render.item;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class CPMFoxItemLayer extends CPMItemLayer<FoxEntity, FoxModel<FoxEntity>> {
    public CPMFoxItemLayer(IEntityRenderer<FoxEntity, FoxModel<FoxEntity>> entityRendererIn, FoxHeldItemLayer orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(FoxEntity entityIn) {
        return entityIn.getItemBySlot(EquipmentSlotType.MAINHAND);
    }
}
