package com.gpiay.cpm.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Collections;
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

    public abstract void setAccessories(List<String> toRemove, List<String> toAdd);

    protected <T> List<T> subtract(List<T> A, List<T> B) {
        List<T> res = Lists.newArrayList(A);
        res.removeAll(B);
        return res;
    }

    @Override
    public void setAccessories(List<String> accessories) {
        List<String> toRemove = subtract(this.accessories, accessories);
        List<String> toAdd = subtract(accessories, this.accessories);
        if (!toRemove.isEmpty() || !toAdd.isEmpty())
            setAccessories(toRemove, toAdd);
    }
    @Override
    public void addAccessory(String accessory) {
        setAccessories(Collections.emptyList(), Lists.newArrayList(accessory));
    }
    @Override
    public void removeAccessory(String accessory) {
        setAccessories(Lists.newArrayList(accessory), Collections.emptyList());
    }
    @Override
    public void clearAccessories() {
        setAccessories(accessories, Collections.emptyList());
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putString("model", mainModel);
        nbt.putDouble("scale", scale);
        ListNBT accessList = new ListNBT();
        for (int i = 0; i < accessories.size(); i++)
            accessList.add(i, StringNBT.valueOf(accessories.get(i)));
        nbt.put("accessories", accessList);
    }

    public void readFromNBT(CompoundNBT nbt) {
        String mainModel = nbt.contains("model") ? nbt.getString("model") : null;
        Double scale = nbt.contains("scale") ? MathHelper.clamp(nbt.getDouble("scale"), 0.01, 100) : null;
        List<String> accessories = null;
        if (nbt.contains("accessories")) {
            accessories = Lists.newArrayList();
            ListNBT accessList = nbt.getList("accessories", 8);
            for (int i = 0; i < accessList.size(); i++)
                accessories.add(accessList.getString(i));
        }
        synchronizeData(mainModel, scale, accessories);
    }
}
