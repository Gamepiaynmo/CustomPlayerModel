package com.gpiay.cpm.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.util.HashHelper;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ModelManager {
    protected static final BiMap<File, String> modelFiles = HashBiMap.create();

    public ModelManager(FileFilter filter, FileAlterationListenerAdaptor adaptor, int interval) {
        FileAlterationObserver observer = new FileAlterationObserver(CPMMod.MODEL_DIR, filter);
        observer.addListener(adaptor);

        FileAlterationMonitor fileMonitor = new FileAlterationMonitor(interval, observer);
        try {
            fileMonitor.start();
        } catch (Exception e) {
            CPMMod.LOGGER.warn("Launch file monitor failed.", e);
        }
    }

    public static void initLocalModels() {
        modelFiles.clear();
        try {
            for (File file : Objects.requireNonNull(CPMMod.MODEL_DIR.listFiles())) {
                try {
                    if (file.isFile()) {
                        String hash = HashHelper.hashCode(file);
                        modelFiles.put(file, hash);
                    }
                } catch (Exception e) {
                    CPMMod.LOGGER.warn("Error while loading model: " + file, e);
                }
            }
        } catch (Exception e) {
            CPMMod.LOGGER.warn("Error while initializing local models.", e);
        }
    }

    protected static void onModelFileUpdate(File file) {
        if (file.exists() && file.isFile()) {
            try {
                modelFiles.forcePut(file, HashHelper.hashCode(file));
            } catch (IOException ignored) {
                modelFiles.remove(file);
            }
        } else {
            modelFiles.remove(file);
        }
    }

    public static File findModelFile(String modelId) {
        File modelFile = modelFiles.inverse().get(modelId);
        if (modelFile == null) {
            File modelDir = new File(CPMMod.CACHE_DIR, modelId);
            File[] files = modelDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        if (file.isFile() && HashHelper.hashCode(file).equals(modelId))
                            modelFile = file;
                    } catch (Exception ignored) {
                        modelFiles.remove(file);
                    }
                }
            }
        }

        return modelFile;
    }

    public static void saveToCache(byte[] data) {
        try {
            String modelId = HashHelper.hashCode(data);
            File directory = new File(CPMMod.CACHE_DIR, modelId);
            directory.mkdirs();

            File modelFile = new File(directory, modelId + ".zip");
            FileOutputStream output = new FileOutputStream(modelFile);
            output.write(data);
            output.close();
        } catch (IOException ignored) {
        }
    }

    public static void deleteCachedModel(String modelId) {
        deleteDirectory(new File(CPMMod.CACHE_DIR, modelId));
    }

    public static void deleteDirectory(File directory) {
        if (!directory.isDirectory()){
            directory.delete();
        } else{
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files){
                    if (file.isDirectory()){
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            directory.delete();
        }
    }
}
