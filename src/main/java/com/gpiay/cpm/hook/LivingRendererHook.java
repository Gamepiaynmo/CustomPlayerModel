package com.gpiay.cpm.hook;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.client.render.CPMLayer;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;
import java.util.function.Consumer;

public class LivingRendererHook {
    private static boolean enabled = false;
    private static Consumer<MatrixStack> callback;

    public static void enableHook(Consumer<MatrixStack> cb) {
        enabled = true;
        callback = cb;
    }

    public static boolean isHookEnabled() {
        return enabled;
    }

    public static boolean onMatrixCallback(MatrixStack matrixStack) {
        if (enabled) {
            callback.accept(matrixStack);
            enabled = false;
            return false;
        }

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean isRenderModel(LivingRenderer livingRenderer, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
            int packedLightIn, LivingEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
        Optional<ICPMCapability> capability = entityIn.getCapability(CPMCapability.CAPABILITY).resolve();
        boolean ret = !capability.isPresent() || ((ClientCPMCapability) capability.get()).getModel() == null;
        if (!ret && entityIn.isSpectator()) {
            for (Object layerRenderer : livingRenderer.layerRenderers) {
                if (layerRenderer instanceof CPMLayer)
                    ((CPMLayer) layerRenderer).render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing,
                            limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }

        return ret;
    }
}
