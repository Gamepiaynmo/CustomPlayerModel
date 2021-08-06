package com.gpiay.cpm.client.render.item;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolenFlowerLayer;
import net.minecraft.client.renderer.entity.model.IronGolemModel;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CPMIronGolemItemLayer extends CPMItemLayer<IronGolemEntity, IronGolemModel<IronGolemEntity>> {
    public CPMIronGolemItemLayer(IEntityRenderer<IronGolemEntity, IronGolemModel<IronGolemEntity>> entityRendererIn, IronGolenFlowerLayer orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(IronGolemEntity entityIn) {
        return entityIn.getOfferFlowerTick() != 0 ? new ItemStack(Items.POPPY) : ItemStack.EMPTY;
    }
}
