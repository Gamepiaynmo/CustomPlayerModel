package com.gpiay.cpm.client.render.item;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.PandaHeldItemLayer;
import net.minecraft.client.renderer.entity.model.PandaModel;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class CPMPandaItemLayer extends CPMItemLayer<PandaEntity, PandaModel<PandaEntity>> {
    public CPMPandaItemLayer(IEntityRenderer<PandaEntity, PandaModel<PandaEntity>> entityRendererIn, PandaHeldItemLayer orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(PandaEntity entityIn) {
        return entityIn.getItemBySlot(EquipmentSlotType.MAINHAND);
    }
}
