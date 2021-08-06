package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import com.gpiay.cpm.util.JsonHelper;
import com.gpiay.cpm.util.exception.TranslatableException;
import com.gpiay.cpm.util.exception.TranslatableJsonException;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ModelInfo {
    String name = "";
    String version = "";
    List<String> author = Lists.newArrayList();
    String description = "";
    String url = "";

    public boolean isCached;
    public double defaultScale = 1;

    public final Map<Pose, EntitySize> entitySize = Maps.newHashMap();
    public final Map<Pose, Pair<Float, Float>> entityEyeHeight = Maps.newHashMap();

    public static Pose getPoseFromName(JsonReader reader, String name) {
        switch (name) {
            case "standing": return Pose.STANDING;
            case "sneaking":
            case "crouching": return Pose.CROUCHING;
            case "swimming": return Pose.SWIMMING;
            default: throw new TranslatableJsonException("error.cpm.loadModel.unknownPose", reader, name);
        }
    }

    public static Pose filterPose(Pose pose) {
        if (pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK)
            return Pose.SWIMMING;
        if (pose == Pose.DYING)
            return Pose.SLEEPING;
        return pose;
    }

    interface IResource {
        String getName();
        InputStream openInputStream() throws IOException;
        default Reader getReader() throws IOException {
            return new InputStreamReader(openInputStream());
        }
    }

    static class ModelLoadData {
        final String modelId;
        final List<Pair<String, IResource>> resources;

        public ModelLoadData(String modelId, List<Pair<String, IResource>> resources) {
            this.modelId = modelId;
            this.resources = resources;
        }
    }

    public static ModelInfo fromDirectory(File directory) throws IOException {
        return loadFromEntry(loadFromDirectory(directory));
    }

    public static ModelInfo fromZipFile(String id, File zipFile) throws IOException {
        return loadFromEntry(loadFromZipFile(id, zipFile));
    }

    public static ModelInfo fromZipMemory(String id, byte[] data) throws IOException {
        return loadFromEntry(loadFromZipMemory(id, data));
    }

    static ModelLoadData loadFromDirectory(File directory) {
        class FileResource implements IResource {
            private final File file;
            public FileResource(File file) {
                this.file = file;
            }

            @Override
            public String getName() {
                return ModelLoader.getStem(file.getName());
            }

            @Override
            public InputStream openInputStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(file));
            }
        }

        List<Pair<String, IResource>> resources = Lists.newArrayList();
        for (File subFile : Objects.requireNonNull(directory.listFiles()))
            resources.add(new ImmutablePair<>(subFile.getName(), new FileResource(subFile)));

        return new ModelLoadData(directory.getName(), resources);
    }

    static ModelLoadData loadFromZipFile(String id, File zipFile) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        ZipFile file = new ZipFile(zipFile);

        class ZipResource implements IResource {
            private final ZipEntry entry;

            public ZipResource(ZipEntry entry) {
                this.entry = entry;
            }

            @Override
            public String getName() {
                return ModelLoader.getStem(entry.getName());
            }

            @Override
            public InputStream openInputStream() throws IOException {
                return file.getInputStream(entry);
            }
        }

        ZipEntry entry;
        List<Pair<String, IResource>> resources = Lists.newArrayList();
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;

            resources.add(new ImmutablePair<>(entry.getName(), new ZipResource(entry)));
        }

        return new ModelLoadData(id, resources);
    }

    static ModelLoadData loadFromZipMemory(String id, byte[] data) throws IOException {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data));

        byte[] buffer = new byte[1024];
        class MemoryResource implements IResource {
            private final String name;
            private final byte[] data;

            public MemoryResource(ZipEntry zipEntry) throws IOException {
                name = ModelLoader.getStem(zipEntry.getName());
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                int cnt = 0;
                while ((cnt = zip.read(buffer, 0, 1024)) > 0)
                    array.write(buffer, 0, cnt);
                array.close();
                data = array.toByteArray();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public InputStream openInputStream() throws IOException {
                return new ByteArrayInputStream(data);
            }
        }

        ZipEntry entry;
        List<Pair<String, IResource>> resources = Lists.newArrayList();
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory())
                continue;

            resources.add(new ImmutablePair<>(entry.getName(), new MemoryResource(entry)));
        }

        return new ModelLoadData(id, resources);
    }

    static ModelInfo loadFromEntry(ModelLoadData loadData) throws IOException {
        IResource mainModel = null;

        for (Pair<String, IResource> pair : loadData.resources) {
            String name = pair.getKey();
            if (name.equalsIgnoreCase("model.json")) {
                mainModel = pair.getValue();
                break;
            }
        }

        if (mainModel == null)
            throw new TranslatableException("error.cpm.loadModel.noModel");

        JsonReader mainModelJson = new JsonReader(mainModel.getReader());
        mainModelJson.setLenient(true);

        ModelInfo modelInfo = loadFromJson(mainModelJson);
        mainModelJson.close();
        return modelInfo;
    }

    private static ModelInfo loadFromJson(JsonReader mainModel) throws IOException {
        ModelInfo modelInfo = new ModelInfo();

        JsonHelper.readObject(mainModel, key -> {
            if (parseModelInfo(mainModel, key, modelInfo))
                mainModel.skipValue();
        });

        return modelInfo;
    }

    static boolean parseModelInfo(JsonReader mainModel, String key, ModelInfo modelInfo) throws IOException {
        switch (key) {
            case "name":
                modelInfo.name = mainModel.nextString();
                break;
            case "version":
                modelInfo.version = mainModel.nextString();
                break;
            case "author":
                JsonHelper.readArray(mainModel, () -> modelInfo.author.add(mainModel.nextString()));
                break;
            case "description":
                modelInfo.description = mainModel.nextString();
                break;
            case "url":
                modelInfo.url = mainModel.nextString();
                break;
            case "default_scale":
                modelInfo.defaultScale = mainModel.nextDouble();
                break;
            case "size": {
                JsonHelper.readObject(mainModel, pose -> {
                    mainModel.beginArray();
                    EntitySize size = EntitySize.scalable(
                            (float) mainModel.nextDouble(),
                            (float) mainModel.nextDouble());
                    mainModel.endArray();
                    modelInfo.entitySize.put(getPoseFromName(mainModel, pose), size);
                });
                break;
            }
            case "eye_height": {
                JsonHelper.readObject(mainModel, pose -> {
                    mainModel.beginArray();
                    Pair<Float, Float> eyeHeight = ImmutablePair.of(
                            (float) mainModel.nextDouble(),
                            (float) mainModel.nextDouble());
                    mainModel.endArray();
                    modelInfo.entityEyeHeight.put(getPoseFromName(mainModel, pose), eyeHeight);
                });
                break;
            }
            default:
                return true;
        }

        return false;
    }

    public EntitySize getEntitySize(LivingEntity entity, Pose pose, EntitySize size, double scale) {
        EntitySize newSize = entitySize.get(filterPose(pose));
        if (newSize == null)
            newSize = size;
        else if (entity.isBaby())
            newSize = newSize.scale(0.5f);
        return newSize.scale((float) scale);
    }

    public float getEntityEyeHeight(LivingEntity entity, Pose pose, float eyeHeight, double scale) {
        Pair<Float, Float> height = entityEyeHeight.get(filterPose(pose));
        if (height != null) return (entity.isBaby() ? height.getRight() : height.getLeft()) * (float) scale;
        return eyeHeight * (float) scale;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public void toBuffer(PacketBuffer buffer) {
        buffer.writeUtf(name);
        buffer.writeUtf(version);
        buffer.writeUtf(description);
        buffer.writeUtf(url);
        buffer.writeInt(author.size());
        for (String author : author) {
            buffer.writeUtf(author);
        }
    }

    public void fromBuffer(PacketBuffer buffer) {
        author.clear();
        name = buffer.readUtf();
        version = buffer.readUtf();
        description = buffer.readUtf();
        url = buffer.readUtf();
        int authors = buffer.readInt();
        for (int i = 0; i < authors; i++)
            author.add(buffer.readUtf());
    }
}
