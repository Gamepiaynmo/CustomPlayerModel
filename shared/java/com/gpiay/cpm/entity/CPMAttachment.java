package com.gpiay.cpm.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class CPMAttachment implements ICPMAttachment {
    private final LivingEntity entity;

    protected String mainModel = "";
    protected double scale = 1;
    protected final List<String> accessories = Lists.newArrayList();

    public CPMAttachment(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public String getMainModel() { return mainModel; }

    @Override
    public double getScale() { return scale; }

    @Override
    public List<String> getAccessories() {
        return accessories;
    }

    protected boolean isPlayer() {
        return entity instanceof PlayerEntity;
    }

    protected LivingEntity getEntity() {
        return entity;
    }

    protected PlayerEntity getPlayerEntity() {
        return isPlayer() ? (PlayerEntity) entity : null;
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putString("model", mainModel);
        nbt.putDouble("scale", scale);
        ListNBT accessList = nbt.getList("accessories", 8);
        for (int i = 0; i < accessories.size(); i++)
            accessList.add(i, StringNBT.valueOf(accessories.get(i)));
    }

    public void readFromNBT(CompoundNBT nbt) {
        String mainModel = nbt.getString("model");
        double scale = nbt.contains("scale") ? MathHelper.clamp(nbt.getDouble("scale"), 0.01, 100) : 1;
        ListNBT accessList = nbt.getList("accessories", 8);
        List<String> accessories = Lists.newArrayList();
        for (int i = 0; i < accessList.size(); i++)
            accessories.add(accessList.getString(i));
        synchronizeData(mainModel, scale, accessories);
    }
}
