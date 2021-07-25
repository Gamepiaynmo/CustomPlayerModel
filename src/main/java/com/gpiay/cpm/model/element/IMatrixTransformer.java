package com.gpiay.cpm.model.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.LivingEntity;

public interface IMatrixTransformer {
    void transform(LivingEntity entity, MatrixStack matrixStack, float partial);
}
