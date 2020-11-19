package com.gpiay.cpm.client.gui;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.ModelInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

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

    private String setColor(ITextComponent text, TextFormatting color) {
        text.getStyle().setColor(color);
        return text.getFormattedText();
    }

    public List<String> getModelInfo() {
        List<String> str = Lists.newArrayListWithCapacity(5);
        str.add(setColor(new TranslationTextComponent("gui.cpm.model.name", info.getName()), TextFormatting.GOLD));
        if (!info.getVersion().isEmpty())
            str.add(setColor(new TranslationTextComponent("gui.cpm.model.version", info.getVersion()), TextFormatting.WHITE));
        if (!info.getAuthor().isEmpty())
            str.add(setColor(new TranslationTextComponent("gui.cpm.model.author", String.join(" ", info.getAuthor())), TextFormatting.WHITE));
        if (!info.getDescription().isEmpty())
            str.add(setColor(new StringTextComponent(info.getDescription()), TextFormatting.WHITE));
        if (!info.getUrl().isEmpty()) {
            ITextComponent text = new StringTextComponent(info.getUrl());
            text.getStyle().setUnderlined(true);
            text.getStyle().setColor(TextFormatting.BLUE);
        }
        if (isLocal)
            str.add(setColor(new TranslationTextComponent("gui.cpm.model.local"), TextFormatting.GRAY));
        if (isEditing)
            str.add(setColor(new TranslationTextComponent("gui.cpm.model.edit"), TextFormatting.YELLOW));
        return str;
    }
}
