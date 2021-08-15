package com.gpiay.cpm.item;

import com.gpiay.cpm.CPMMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CPMItems extends ItemGroup {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CPMMod.MOD_ID);
    public static final ItemGroup ITEM_GROUP = new CPMItems();

    public static final RegistryObject<Item> transformationWand = ITEMS.register(
            "transformation_wand",
            () -> new TransformationWand(new Item.Properties()
                    .tab(CPMItems.ITEM_GROUP)
                    .durability(10)));

    public CPMItems() {
        super(CPMMod.MOD_ID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(transformationWand.get());
    }
}
