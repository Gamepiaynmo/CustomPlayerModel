package com.gpiay.cpm.client.render;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class CPMHideLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private final LayerRenderer<T, M> orinLayer;

    public CPMHideLayer(IEntityRenderer<T, M> entityRendererIn, LayerRenderer<T, M> orinLayer) {
        super(entityRendererIn);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(entityIn).orElse(null);
        ModelInstance model = attachment != null ? attachment.getModel() : null;
        if (model == null) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
