package com.gpiay.cpm.model.skeleton;

import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.model.skeleton.model.CustomQuadrupedModel;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.entity.LivingEntity;

public class QuadrupedBaseSkeleton<M extends CustomQuadrupedModel> extends Skeleton<M> {
    protected final VanillaBone head, body, leftFrontLeg, rightFrontLeg, leftBackLeg, rightBackLeg;

    public QuadrupedBaseSkeleton(M model) {
        super(model);

        head = registerVanillaBone("head");
        body = registerVanillaBone("body");
        rightBackLeg = registerVanillaBone("right_back_leg");
        leftBackLeg = registerVanillaBone("left_back_leg");
        rightFrontLeg = registerVanillaBone("right_front_leg");
        leftFrontLeg = registerVanillaBone("left_front_leg");
    }

    @Override
    protected void adjustBones(LivingEntity entity, double scale, float partialTicks, boolean firstPerson) {
        super.adjustBones(entity, scale, partialTicks, firstPerson);

        if (entity.isChild()) {
            none.offset(0, scale * entityModel.headPivotHeight / 2, scale * entityModel.headOffset / 2);
            body.scale(0.5).offset(0, 12, 0);
            rightBackLeg.scale(0.5).offset(0, 12, 0);
            leftBackLeg.scale(0.5).offset(0, 12, 0);
            rightFrontLeg.scale(0.5).offset(0, 12, 0);
            leftFrontLeg.scale(0.5).offset(0, 12, 0);
        }
    }

    @Override
    public void addAttachments(EnumAttachment attachment, ModelInstance instance) {
        switch (attachment) {
            case HELMET:
            case SKULL:
                addBuiltinAttachment(attachment, instance, "head");
                break;
            case CHESTPLATE_BODY:
            case CAPE:
                addBuiltinAttachment(attachment, instance, "body");
            case ELYTRA:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(0, entityModel.bodyPivotHeight - 24, 0), new Vector3d(0, 90, 0));
                break;
            case LEGGINGS_LEFT:
                addBuiltinAttachment(attachment, instance, "left_front_leg", "builtin_leggings_front_left");
                addBuiltinAttachment(attachment, instance, "left_back_leg", "builtin_leggings_back_left");
                break;
            case BOOTS_LEFT:
                addBuiltinAttachment(attachment, instance, "left_front_leg", "builtin_boots_front_left");
                addBuiltinAttachment(attachment, instance, "left_back_leg", "builtin_boots_back_left");
                break;
            case LEGGINGS_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_front_leg", "builtin_leggings_front_right");
                addBuiltinAttachment(attachment, instance, "right_back_leg", "builtin_leggings_back_right");
                break;
            case BOOTS_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_front_leg", "builtin_boots_front_right");
                addBuiltinAttachment(attachment, instance, "right_back_leg", "builtin_boots_back_right");
                break;
            case ITEM_LEFT:
                addBuiltinAttachment(attachment, instance, "left_front_leg", new Vector3d(0, -entityModel.legLength, 0));
                break;
            case ITEM_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_front_leg", new Vector3d(0, -entityModel.legLength, 0));
                break;
            case PARROT_LEFT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(entityModel.legInterval / 2 + 1, entityModel.headPivotHeight - 24, 0));
                break;
            case PARROT_RIGHT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(-entityModel.legInterval / 2 - 1, entityModel.headPivotHeight - 24, 0));
                break;
            default:
                super.addAttachments(attachment, instance);
                break;
        }
    }
}
