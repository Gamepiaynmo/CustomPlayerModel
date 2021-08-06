package com.gpiay.cpm.model.animation;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class ScriptItem {
    public static final ScriptItem empty = new ScriptItem(ItemStack.EMPTY);

    private final ItemStack item;

    public ScriptItem(ItemStack item) {
        this.item = item;
    }

    public String getItem() {
        ResourceLocation location = Registry.ITEM.getKey(item.getItem());
        return location.toString();
    }

    public int getCount() {
        return item.getCount();
    }

    public boolean isEmpty() {
        return item.isEmpty();
    }

}
