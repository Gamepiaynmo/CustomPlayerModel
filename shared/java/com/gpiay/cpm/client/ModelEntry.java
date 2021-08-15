package com.gpiay.cpm.client;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.ModelInfo;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ModelEntry {
    public final ModelInfo info;
    public final boolean isLocal;
    public boolean isEditing = false;
    private List<IFormattableTextComponent> modelInfo;

    public ModelEntry(ModelInfo info, boolean local) {
        this.info = info;
        this.isLocal = local;
    }

    public boolean search(String text) {
        if (info.id.contains(text)) return true;
        if (info.getName().contains(text)) return true;
        if (info.getVersion().contains(text)) return true;
        if (info.getDescription().contains(text)) return true;
        if (info.getUrl().contains(text)) return true;
        for (String author : info.getAuthor())
            if (author.contains(text))
                return true;

        return false;
    }

    private IFormattableTextComponent setColor(IFormattableTextComponent text, TextFormatting color) {
        return text.setStyle(text.getStyle().applyFormat(color));
    }

    public List<IFormattableTextComponent> getModelInfo() {
        if (modelInfo == null) {
            modelInfo = Lists.newArrayListWithCapacity(5);
            modelInfo.add(setColor(new TranslationTextComponent("gui.cpm.model.name", info.getName()), TextFormatting.GOLD));
            modelInfo.add(setColor(new TranslationTextComponent(info.isAccessory ? "gui.cpm.accessory" : "gui.cpm.model"), TextFormatting.WHITE));
            if (!info.getVersion().isEmpty())
                modelInfo.add(setColor(new TranslationTextComponent("gui.cpm.model.version", info.getVersion()), TextFormatting.WHITE));
            if (!info.getAuthor().isEmpty())
                modelInfo.add(setColor(new TranslationTextComponent("gui.cpm.model.author", String.join(" ", info.getAuthor())), TextFormatting.WHITE));
            if (!info.getDescription().isEmpty())
                modelInfo.add(setColor(new StringTextComponent(info.getDescription()), TextFormatting.WHITE));
            if (!info.getUrl().isEmpty()) {
                IFormattableTextComponent text = new StringTextComponent(info.getUrl());
                text.setStyle(text.getStyle().withUnderlined(true).applyFormat(TextFormatting.BLUE));
                modelInfo.add(text);
            }
            if (isLocal)
                modelInfo.add(setColor(new TranslationTextComponent("gui.cpm.model.local"), TextFormatting.GRAY));
            if (isEditing)
                modelInfo.add(setColor(new TranslationTextComponent("gui.cpm.model.edit"), TextFormatting.YELLOW));
        }

        return modelInfo;
    }
}
