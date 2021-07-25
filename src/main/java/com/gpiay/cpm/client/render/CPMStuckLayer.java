package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

public abstract class CPMStuckLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    StuckInBodyLayer<T, M> orinLayer;

    public CPMStuckLayer(LivingRenderer<T, M> p_i226041_1_, StuckInBodyLayer<T, M> orinLayer) {
        super(p_i226041_1_);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            int i = this.numStuck(entityIn);
            Random random = new Random(entityIn.getId());
            matrixStackIn.pushPose();
            model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());

            for (int j = 0; j < i; ++j) {
                matrixStackIn.pushPose();
                ModelBone.Instance bone = model.getRandomBone(random);
                model.setupBoneTransform(matrixStackIn, bone);
                List<ModelRenderer> boxes = bone.modelBone.boxes;
                ModelRenderer modelrenderer = boxes.get(random.nextInt(boxes.size()));
                if (modelrenderer.cubes.size() <= 0)
                    continue;
                ModelRenderer.ModelBox modelrenderer$modelbox = modelrenderer.cubes.get(0);
                modelrenderer.translateAndRotate(matrixStackIn);
                float f = random.nextFloat();
                float f1 = random.nextFloat();
                float f2 = random.nextFloat();
                float f3 = MathHelper.lerp(f, modelrenderer$modelbox.minX, modelrenderer$modelbox.maxX) / 16.0F;
                float f4 = MathHelper.lerp(f1, modelrenderer$modelbox.minY, modelrenderer$modelbox.maxY) / 16.0F;
                float f5 = MathHelper.lerp(f2, modelrenderer$modelbox.minZ, modelrenderer$modelbox.maxZ) / 16.0F;
                matrixStackIn.translate((double)f3, (double)f4, (double)f5);
                f = -1.0F * (f * 2.0F - 1.0F);
                f1 = -1.0F * (f1 * 2.0F - 1.0F);
                f2 = -1.0F * (f2 * 2.0F - 1.0F);
                this.renderStuckItem(matrixStackIn, bufferIn, packedLightIn, entityIn, f, f1, f2, partialTicks);
                matrixStackIn.popPose();
            }

            matrixStackIn.popPose();
        }
    }
}
