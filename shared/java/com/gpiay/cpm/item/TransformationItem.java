package com.gpiay.cpm.item;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ServerCPMAttachment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public class TransformationItem extends Item {
    public TransformationItem(Item.Properties properties) {
        super(properties);
    }

    public boolean isFoil(ItemStack itemStack) {
        return !getModelId(itemStack).isEmpty();
    }

    protected String getModelId(ItemStack itemStack) {
        return itemStack.getOrCreateTag().getString("model");
    }

    protected double getScale(ItemStack itemStack) {
        double scale = itemStack.getOrCreateTag().getDouble("scale");
        return scale == 0 ? 1 : Math.max(scale, 0.01);
    }

    protected boolean useOnEntity(ItemStack itemStack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (isFoil(itemStack)) {
            AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment -> {
                ServerCPMAttachment serverCapability = (ServerCPMAttachment) attachment;
                serverCapability.readFromNBT(itemStack.getOrCreateTag());
            });

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
