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
    public static ModelBone parseBone(ModelPart model, JsonReader reader, boolean doubleFace) throws IOException {
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
                        Either<ModelRenderer, ModelRenderer.ModelBox> box = parseBox(model, reader, bone.position, doubleFace);
                        box.ifLeft(bone.boxes::add);
                        box.ifRight(identity.cubes::add);
                    });

                    if (!identity.cubes.isEmpty())
                        bone.boxes.add(identity);

                    break;
                }
                default: reader.skipValue();
            }
        });

        return bone;
    }

    public static Either<ModelRenderer, ModelRenderer.ModelBox> parseBox(ModelPart model, JsonReader reader, Vector3d pivot, boolean doubleFace) throws IOException {
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
                    bone.x = (float) (reader.nextDouble() - pivot.x);
                    bone.y = (float) (pivot.y - reader.nextDouble());
                    bone.z = (float) (reader.nextDouble() - pivot.z);
                    reader.endArray();
                    break;
                }
                case "rotation": {
                    reader.beginArray();
                    bone.yRot = (float) reader.nextDouble();
                    bone.xRot = (float) reader.nextDouble();
                    bone.zRot = (float) reader.nextDouble();
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
            quads.add(box.polygons[0]);
            if (dx != 0 || doubleFace)
                quads.add(box.polygons[1]);
        }
        if (dx != 0 || dz != 0) {
            quads.add(box.polygons[2]);
            if (dy != 0 || doubleFace)
                quads.add(box.polygons[3]);
        }
        if (dx != 0 || dy != 0) {
            quads.add(box.polygons[4]);
            if (dz != 0 || doubleFace)
                quads.add(box.polygons[5]);
        }

        box.polygons = quads.toArray(new ModelRenderer.TexturedQuad[0]);
        boolean identity = bone.xRot == 0 && bone.yRot == 0 && bone.zRot == 0
                && bone.x == 0 && bone.y == 0 && bone.z == 0;
        if (identity) return Either.right(box);

        bone.cubes.add(box);
        return Either.left(bone);
    }
}
