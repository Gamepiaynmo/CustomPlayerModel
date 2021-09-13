package com.gpiay.cpm.client.render.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeldBlockLayer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.item.ItemStack;

public class CPMEndermanItemLayer extends CPMItemLayer<EndermanEntity, EndermanModel<EndermanEntity>> {
    public CPMEndermanItemLayer(IEntityRenderer<EndermanEntity, EndermanModel<EndermanEntity>> entityRendererIn, HeldBlockLayer orinLayer) {
        super(entityRendererIn, orinLayer);
    }

    @Override
    protected ItemStack getHeldItem(EndermanEntity entityIn) {
        BlockState blockState = entityIn.getCarriedBlock();
        return blockState != null ? new ItemStack(blockState.getBlock()) : ItemStack.EMPTY;
    }
}
