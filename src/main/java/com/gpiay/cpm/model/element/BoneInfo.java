package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Function;

public class BoneInfo {
    String id;
    String parentId = "none";

    Vector3d position = Vector3d.Zero.cpy();
    Vector3d rotation = Vector3d.Zero.cpy();
    Vector3d scale = Vector3d.One.cpy();
    boolean visible = true;
    Quat4d color = Quat4d.One.cpy();

    public List<ModelBox> boxes = Lists.newArrayList();

    private boolean compiled = false;
    private int glList;

    public String getId() { return id; }

    public ModelBone instantiate(ModelInstance model, Function<String, IModelBone> boneMap, Function<String, BoneInfo> infoMap) {
        ModelBone bone = null;
        if (parentId.equals("none") && id.endsWith("_c")) {
            String orinId = id.substring(0, id.length() - 2);
            IModelBone parent = boneMap.apply(orinId);
            if (parent != null) {
                parentId = orinId;
                bone = new ModelBone(model, id, parent, this);
            }
        }

        if (bone == null) {
            IModelBone parent = boneMap.apply(parentId);
            if (parent == null)
                parent = boneMap.apply("none");
            bone = new ModelBone(model, id, parent, this);
        }

        bone.position = position.cpy();
        BoneInfo parentInfo = infoMap.apply(parentId);
        if (parentInfo == null)
            bone.position.sub(0, 24, 0);
        else bone.position.sub(parentInfo.position);
        bone.position.scl(1, -1, 1).scl(0.0625f);
        if (parentInfo == null)
            bone.position.sub(bone.parent.getPosition());

        bone.rotation = new Vector3d(rotation.y, rotation.x, rotation.z);
        bone.scale = scale.cpy();
        bone.visible = visible;
        bone.color = color.cpy();

        return bone;
    }

    public void render() {
        if (!compiled)
            compile();

        GlStateManager.callList(glList);
    }

    private void compile() {
        glList = GLAllocation.generateDisplayLists(1);
        GlStateManager.newList(glList, GL11.GL_COMPILE);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        for (ModelBox box : boxes)
            box.render(bufferBuilder, 0.0625f);

        GlStateManager.endList();
        this.compiled = true;
    }
}
