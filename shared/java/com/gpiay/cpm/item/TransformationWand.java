package com.gpiay.cpm.item;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ServerCPMAttachment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class TransformationWand extends Item implements TransformationItem {
    public TransformationWand(Item.Properties properties) {
        super(properties);
    }

    public boolean isFoil(ItemStack itemStack) {
        return hasCPMData(itemStack);
    }

    protected boolean useOnEntity(ItemStack itemStack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (isFoil(itemStack)) {
            applyOnEntity(itemStack, entity);
            itemStack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(hand));
            return true;
        }

        return false;
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity entity, Hand hand) {
        if (entity.level.isClientSide)
            return ActionResultType.PASS;

        return useOnEntity(stack, playerIn, entity, hand) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    }
}
