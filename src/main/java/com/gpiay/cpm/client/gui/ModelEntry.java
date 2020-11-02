package com.gpiay.cpm.client.gui;

import com.gpiay.cpm.model.ModelInfo;

public class ModelEntry {
    public final String id;
    public final ModelInfo info;
    public final boolean isLocal;
    public boolean isEditing = false;

    public ModelEntry(String id, ModelInfo info, boolean local) {
        this.id = id;
        this.info = info;
        this.isLocal = local;
    }

    public boolean search(String text) {
        if (id.contains(text)) return true;
        if (info.getName().contains(text)) return true;
        if (info.getVersion().contains(text)) return true;
        if (info.getDescription().contains(text)) return true;
        if (info.getUrl().contains(text)) return true;
        for (String author : info.getAuthor())
            if (author.contains(text))
                return true;

        return false;
    }
}
