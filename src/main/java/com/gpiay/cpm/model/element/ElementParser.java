package com.gpiay.cpm.model.element;

import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.util.JsonHelper;
import com.gpiay.cpm.util.math.Vector3d;

import java.io.IOException;

public class ElementParser {
    public static BoneInfo parseBone(BedrockModel model, JsonReader reader) throws IOException {
        BoneInfo bone = new BoneInfo();

        JsonHelper.readObject(reader, key -> {
            switch (key) {
                case "name": bone.id = reader.nextString(); break;
                case "parent": bone.parentId = reader.nextString(); break;
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
                    JsonHelper.readArray(reader, () -> {
                        bone.boxes.add(parseBox(model, reader, bone.position));
                    });

                    break;
                }
                default: reader.skipValue();
            }
        });

        return bone;
    }

    public static ModelBox parseBox(BedrockModel model, JsonReader reader, Vector3d pivot) throws IOException {
        int u = 0, v = 0, dx = 0, dy = 0, dz = 0;
        float x = 0, y = 0, z = 0, delta = 0;
        boolean mirror = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
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
                    dx = reader.nextInt();
                    dy = reader.nextInt();
                    dz = reader.nextInt();
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
        return new ModelBox(model.textureWidth, model.textureHeight, u, v, x, y, z, dx, dy, dz, delta, mirror);
    }
}
