package com.gpiay.cpm.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public interface ICPMAttachment {
    String getMainModel();
    double getScale();
    List<String> getAccessories();
    void setAccessories(List<String> accessories);
    void addAccessory(String accessory);
    void removeAccessory(String accessory);
    void clearAccessories();

    void setMainModel(String mainModel);
    void setScale(double scale);

    EntitySize changeEntitySize(Pose pose, EntitySize orinSize);
    float changeEyeHeight(Pose pose, EntitySize entitySize, float orinHeight);
    Vector3d changeEyePosition(float partial);

    void update();

    void synchronizeData(String mainModel, Double scale, List<String> accessories);

    void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float animPos,
            float animSpeed, float age, float headYaw, float headPitch, float partial);
    boolean renderFirstPerson(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, HandSide hand);

    void writeToNBT(CompoundNBT nbt);
    void readFromNBT(CompoundNBT nbt);
}
