package com.gpiay.cpm.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class CPMArrowLayer<T extends LivingEntity, M extends PlayerModel<T>> extends CPMStuckLayer<T, M> {
    ArrowLayer<T, M> orinLayer;

    public CPMArrowLayer(LivingRenderer<T, M> p_i226041_1_, ArrowLayer<T, M> orinLayer) {
        super(p_i226041_1_, orinLayer);
        this.orinLayer = orinLayer;
    }

    @Override
    public int numStuck(T p_225631_1_) {
        return orinLayer.numStuck(p_225631_1_);
    }

    @Override
    public void renderStuckItem(MatrixStack p_225632_1_, IRenderTypeBuffer p_225632_2_, int p_225632_3_, Entity p_225632_4_, float p_225632_5_, float p_225632_6_, float p_225632_7_, float p_225632_8_) {
        orinLayer.renderStuckItem(p_225632_1_, p_225632_2_, p_225632_3_, p_225632_4_, p_225632_5_, p_225632_6_, p_225632_7_, p_225632_8_);
    }
}
