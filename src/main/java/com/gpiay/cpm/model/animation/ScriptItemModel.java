package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.element.ItemModelInstance;

public class ScriptItemModel {
    private final ItemModelInstance item;

    public ScriptItemModel(ItemModelInstance item) {
        this.item = item;
    }

    public ScriptBone getBone() { return new ScriptBone(item.bone); }

    public String getItem() { return item.itemId; }
    public void setItem(String itemId) { item.setItemId(itemId); }

    public boolean isEnchanted() { return item.enchanted; }
    public void setEnchanted(boolean value) { item.setEnchanted(value); }
}
