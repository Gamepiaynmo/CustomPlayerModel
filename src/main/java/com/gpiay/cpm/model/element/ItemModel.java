package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.ModelInstance;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModel {
    public String name;
    public String parentId = "none";
    public String itemId;
    public boolean enchanted;

    public ItemModelInstance instantiate(ModelInstance model) {
        IModelBone bone = model.getBone(parentId);
        if (bone == null)
            bone = model.getBone("none");
        ItemModelInstance instance = new ItemModelInstance(bone);
        if (bone instanceof ModelBone)
            ((ModelBone) bone).setCalculateTransform();

        instance.name = name;
        instance.itemId = itemId;
        instance.enchanted = enchanted;
        instance.stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));
        if (enchanted)
            instance.stack.addEnchantment(Enchantments.UNBREAKING, 0);

        return instance;
    }
}
