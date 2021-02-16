package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

public class CPMLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private final LivingRenderer<T, M> entityRenderer;
    private float initialShadowSize;
    private float lastShadowSize;

    public CPMLayer(IEntityRenderer<T, M> entityRendererIn) {
        super(entityRendererIn);
        this.entityRenderer = (LivingRenderer<T, M>) entityRendererIn;
        this.initialShadowSize = entityRenderer.shadowSize;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityRenderer.shadowSize == lastShadowSize)
            entityRenderer.shadowSize = initialShadowSize;

        entityIn.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> {
            ModelInstance model = ((ClientCPMCapability) cap).getModel();
            if (model != null) {
                model.render(matrixStackIn, bufferIn, packedLightIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, cap.getScale());

                float shadowSize = model.getModelPack().shadowSize;
                if (shadowSize >= 0)
                    entityRenderer.shadowSize = shadowSize;
                entityRenderer.shadowSize *= (float) cap.getScale();
            }
        });

        lastShadowSize = entityRenderer.shadowSize;
    }
}
