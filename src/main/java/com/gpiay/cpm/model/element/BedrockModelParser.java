package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.util.JsonHelper;
import com.gpiay.cpm.util.exception.TranslatableJsonException;

import java.io.IOException;
import java.util.List;

public class BedrockModelParser {
    public static List<BedrockModel> fromJson(JsonReader reader) throws IOException {
        List<BedrockModel> models = Lists.newArrayList();
        boolean legacy = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if ("format_version".equals(key)) {
                String version = reader.nextString();
                String[] versions = version.split("\\.");
                if (versions.length != 3)
                    throw new TranslatableJsonException("error.cpm.loadModel.unknownVersion", reader, version);
                legacy = Integer.parseInt(versions[0]) < 1 || Integer.parseInt(versions[1]) < 12;
                continue;
            }

            if (legacy && key.startsWith("geometry")) {
                models.add(parseLegacyModel(reader));
            }

            if (!legacy && "minecraft:geometry".equals(key)) {
                JsonHelper.readArray(reader, () -> models.add(parseBedrockModel(reader)));
            }
        }

        reader.endObject();

        return models;
    }

    private static BedrockModel parseLegacyModel(JsonReader reader) throws IOException {
        BedrockModel model = new BedrockModel();

        JsonHelper.readObject(reader, key -> {
            switch (key) {
                case "texturewidth": model.textureWidth = reader.nextInt(); break;
                case "textureheight": model.textureHeight = reader.nextInt(); break;
                case "bones": {
                    JsonHelper.readArray(reader, () -> {
                        model.bones.add(ElementParser.parseBone(model, reader));
                    });

                    break;
                }
                default: reader.skipValue();
            }
        });

        return model;
    }

    private static BedrockModel parseBedrockModel(JsonReader reader) throws IOException {
        BedrockModel model = new BedrockModel();

        JsonHelper.readObject(reader, key -> {
            switch (key) {
                case "description": {
                    JsonHelper.readObject(reader, descKey -> {
                        switch (descKey) {
                            case "texture_width": model.textureWidth = reader.nextInt(); break;
                            case "texture_height": model.textureHeight = reader.nextInt(); break;
                            default: reader.skipValue();
                        }
                    });

                    break;
                }
                case "bones": {
                    JsonHelper.readArray(reader, () -> {
                        model.bones.add(ElementParser.parseBone(model, reader));
                    });

                    break;
                }
                default: reader.skipValue();
            }
        });

        return model;
    }

    public static void setBedrockModelTexture(BedrockModel model, String texture) {
        model.texture = texture;
    }
}
