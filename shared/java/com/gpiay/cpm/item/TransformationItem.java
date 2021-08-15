package com.gpiay.cpm.item;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ServerCPMAttachment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public interface TransformationItem {
    default boolean hasCPMData(ItemStack itemStack) { return itemStack.getOrCreateTag().contains(CPMMod.MOD_ID); }
    default CompoundNBT getCPMData(ItemStack itemStack) { return itemStack.getOrCreateTag().getCompound(CPMMod.MOD_ID); }
    default void setCPMData(ItemStack itemStack, CompoundNBT data) { itemStack.getOrCreateTag().put(CPMMod.MOD_ID, data); }

    default boolean hasModel(ItemStack itemStack) { return getCPMData(itemStack).contains("model"); }
    default String getModel(ItemStack itemStack) { return getCPMData(itemStack).getString("model"); }
    default void setModel(ItemStack itemStack, String model) {
        CompoundNBT data = getCPMData(itemStack);
        data.putString("model", model);
        setCPMData(itemStack, data);
    }

    default boolean hasScale(ItemStack itemStack) { return getCPMData(itemStack).contains("scale"); }
    default double getScale(ItemStack itemStack) {
        CompoundNBT data = getCPMData(itemStack);
        return data.contains("scale") ? MathHelper.clamp(data.getDouble("scale"), 0.01, 100) : 1;
    }
    default void setScale(ItemStack itemStack, double scale) {
        CompoundNBT data = getCPMData(itemStack);
        data.putDouble("scale", scale);
        setCPMData(itemStack, data);
    }

    default boolean hasAccessories(ItemStack itemStack) { return getCPMData(itemStack).contains("accessories"); }
    default List<String> getAccessories(ItemStack itemStack) {
        List<String> accessories = Lists.newArrayList();
        ListNBT accessList = getCPMData(itemStack).getList("accessories", 8);
        for (int i = 0; i < accessList.size(); i++)
            accessories.add(accessList.getString(i));
        return accessories;
    }
    default void setAccessories(ItemStack itemStack, List<String> accessories) {
        CompoundNBT data = getCPMData(itemStack);
        ListNBT accessList = new ListNBT();
        for (int i = 0; i < accessories.size(); i++)
            accessList.add(i, StringNBT.valueOf(accessories.get(i)));
        data.put("accessories", accessList);
        setCPMData(itemStack, data);
    }

    default void applyOnEntity(ItemStack itemStack, LivingEntity entity) {
        if (hasCPMData(itemStack)) {
            AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment -> {
                ServerCPMAttachment serverAttach = (ServerCPMAttachment) attachment;
                serverAttach.readFromNBT(getCPMData(itemStack));
            });
        }
    }
}
