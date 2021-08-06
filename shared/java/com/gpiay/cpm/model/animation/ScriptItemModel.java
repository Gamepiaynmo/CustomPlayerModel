package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.element.ItemModel;

public class ScriptItemModel {
    private final ItemModel.Instance item;

    public ScriptItemModel(ItemModel.Instance item) {
        this.item = item;
    }

    public ScriptBone getBone() { return new ScriptBone(item.parent); }

    public String getItem() { return item.itemId; }
    public void setItem(String itemId) { item.setItemId(itemId); }

    public boolean isEnchanted() { return item.enchanted; }
    public void setEnchanted(boolean value) { item.setEnchanted(value); }
}
