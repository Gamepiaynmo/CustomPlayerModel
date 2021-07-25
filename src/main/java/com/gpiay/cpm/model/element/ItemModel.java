package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.IComponent;
import com.gpiay.cpm.model.ModelInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModel extends ModelElement {
    public String parentName = "none";
    public String itemId;
    public boolean enchanted;

    @Override
    public Instance instantiate(ModelInstance model) {
        IModelBone parent = findParent(model, parentName);
        Instance instance = new Instance(name, model, parent);
        instance.itemId = itemId;
        instance.enchanted = enchanted;
        instance.stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));
        if (enchanted)
            instance.stack.enchant(Enchantments.UNBREAKING, 0);
        return instance;
    }

    public static class Instance extends ModelElement.Instance implements ModelElement.IParented, IComponent {
        public final IModelBone parent;

        public String itemId;
        public boolean enchanted;
        public ItemStack stack;

        public Instance(String name, ModelInstance model, IModelBone parent) {
            super(name, model);
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn,
                float red, float green, float blue, float alpha) {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.GROUND, packedLightIn, packedOverlayIn, matrixStackIn, bufferIn);
        }

        @Override
        public IModelBone getParent() {
            return parent;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
            ResourceLocation location = new ResourceLocation(itemId);
            if (!stack.isEmpty() || !ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(location)) {
                stack = new ItemStack(ForgeRegistries.ITEMS.getValue(location));
                if (enchanted)
                    stack.enchant(Enchantments.UNBREAKING, 0);
            }
        }

        public void setEnchanted(boolean enchanted) {
            this.enchanted = enchanted;
            if (stack.isEnchanted() && !enchanted)
                stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));
            else if (!stack.isEnchanted() && enchanted)
                stack.enchant(Enchantments.UNBREAKING, 0);
        }
    }
}
