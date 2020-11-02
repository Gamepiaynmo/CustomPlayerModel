package com.gpiay.cpm.model.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModelInstance {
    public String name;
    public String itemId;
    public boolean enchanted;
    public ItemStack stack;
    public final IModelBone bone;

    public ItemModelInstance(IModelBone bone) {
        this.bone = bone;
    }

    public void render() {
        if (bone.isVisible())
            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
        ResourceLocation location = new ResourceLocation(itemId);
        if (!stack.isEmpty() || !ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(location)) {
            stack = new ItemStack(ForgeRegistries.ITEMS.getValue(location));
            if (enchanted)
                stack.addEnchantment(Enchantments.UNBREAKING, 0);
        }
    }

    public void setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
        if (stack.isEnchanted() && !enchanted)
            stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));
        else if (!stack.isEnchanted() && enchanted)
            stack.addEnchantment(Enchantments.UNBREAKING, 0);
    }
}
