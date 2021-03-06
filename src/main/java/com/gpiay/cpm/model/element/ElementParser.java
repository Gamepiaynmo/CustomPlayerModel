package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.util.JsonHelper;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.model.ModelRenderer;

import java.io.IOException;
import java.util.List;

public class ElementParser {
    public static ModelBone parseBone(ModelPart model, JsonReader reader) throws IOException {
        ModelBone bone = new ModelBone();

        JsonHelper.readObject(reader, key -> {
            switch (key) {
                case "name": bone.name = reader.nextString(); break;
                case "parent": bone.parentName = reader.nextString(); break;
                case "pivot": {
                    reader.beginArray();
                    bone.position = new Vector3d(
                            reader.nextDouble(),
                            reader.nextDouble(),
                            reader.nextDouble());
                    reader.endArray();
                    break;
                }
                case "rotation": {
                    reader.beginArray();
                    bone.rotation = new Vector3d(
                            reader.nextDouble(),
                            reader.nextDouble(),
                            reader.nextDouble());
                    reader.endArray();
                    break;
                }
                case "cubes": {
                    ModelRenderer identity = new ModelRenderer(0, 0, 0, 0);
                    JsonHelper.readArray(reader, () -> {
                        Either<ModelRenderer, ModelRenderer.ModelBox> box = parseBox(model, reader, bone.position);
                        box.ifLeft(bone.boxes::add);
                        box.ifRight(identity.cubeList::add);
                    });

                    if (!identity.cubeList.isEmpty())
                        bone.boxes.add(identity);

                    break;
                }
                default: reader.skipValue();
            }
        });

        return bone;
    }

    public static Either<ModelRenderer, ModelRenderer.ModelBox> parseBox(ModelPart model, JsonReader reader, Vector3d pivot) throws IOException {
        int u = 0, v = 0;
        float x = 0, y = 0, z = 0, dx = 0, dy = 0, dz = 0, delta = 0;
        boolean mirror = false;

        ModelRenderer bone = new ModelRenderer(0, 0, 0, 0);

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "pivot": {
                    reader.beginArray();
                    bone.rotationPointX = (float) (reader.nextDouble() - pivot.x);
                    bone.rotationPointY = (float) (pivot.y - reader.nextDouble());
                    bone.rotationPointZ = (float) (reader.nextDouble() - pivot.z);
                    reader.endArray();
                    break;
                }
                case "rotation": {
                    reader.beginArray();
                    bone.rotateAngleY = (float) reader.nextDouble();
                    bone.rotateAngleX = (float) reader.nextDouble();
                    bone.rotateAngleZ = (float) reader.nextDouble();
                    reader.endArray();
                    break;
                }
                case "origin": {
                    reader.beginArray();
                    x = (float) reader.nextDouble();
                    y = (float) reader.nextDouble();
                    z = (float) reader.nextDouble();
                    reader.endArray();
                    break;
                }
                case "size": {
                    reader.beginArray();
                    dx = (float) reader.nextDouble();
                    dy = (float) reader.nextDouble();
                    dz = (float) reader.nextDouble();
                    reader.endArray();
                    break;
                }
                case "uv": {
                    reader.beginArray();
                    u = reader.nextInt();
                    v = reader.nextInt();
                    reader.endArray();
                    break;
                }
                case "inflate": delta = (float) reader.nextDouble(); break;
                case "mirror": mirror = reader.nextBoolean(); break;
                default: reader.skipValue();
            }
        }

        reader.endObject();
        x -= (float) pivot.x;
        y = (float) pivot.y - y - dy;
        z -= (float) pivot.z;

        ModelRenderer.ModelBox box = new ModelRenderer.ModelBox(u, v, x, y, z, dx, dy, dz, delta, delta, delta, mirror,
                model.textureWidth, model.textureHeight);
        List<ModelRenderer.TexturedQuad> quads = Lists.newArrayList();
        if (dy != 0 || dz != 0) {
            quads.add(box.quads[0]);
            if (dx != 0)
                quads.add(box.quads[1]);
        }
        if (dx != 0 || dz != 0) {
            quads.add(box.quads[2]);
            if (dy != 0)
                quads.add(box.quads[3]);
        }
        if (dx != 0 || dy != 0) {
            quads.add(box.quads[4]);
            if (dz != 0)
                quads.add(box.quads[5]);
        }

        box.quads = quads.toArray(new ModelRenderer.TexturedQuad[0]);
        boolean identity = bone.rotateAngleX == 0 && bone.rotateAngleY == 0 && bone.rotateAngleZ == 0
                && bone.rotationPointX == 0 && bone.rotationPointY == 0 && bone.rotationPointZ == 0;
        if (identity) return Either.right(box);

        bone.cubeList.add(box);
        return Either.left(bone);
    }
}
