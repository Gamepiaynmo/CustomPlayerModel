package com.gpiay.cpm.client.render;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.AccessoryInstance;
import com.gpiay.cpm.model.ModelInstance;
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
        this.initialShadowSize = entityRenderer.shadowRadius;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityRenderer.shadowRadius == lastShadowSize)
            entityRenderer.shadowRadius = initialShadowSize;

        AttachmentProvider.getEntityAttachment(entityIn).ifPresent(attach -> {
            ClientCPMAttachment clientAttach = (ClientCPMAttachment) attach;
            ModelInstance model = clientAttach.getModel();
            if (model != null && model.isReady()) {
                model.render(matrixStackIn, bufferIn, packedLightIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, attach.getScale());
                for (AccessoryInstance instance : clientAttach.getAccessoryModels())
                    instance.render(matrixStackIn, bufferIn, packedLightIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, attach.getScale());

                float shadowSize = model.getModelPack().shadowSize;
                if (shadowSize >= 0)
                    entityRenderer.shadowRadius = shadowSize;
                entityRenderer.shadowRadius *= (float) attach.getScale();
            }
        });

        lastShadowSize = entityRenderer.shadowRadius;
    }
}
