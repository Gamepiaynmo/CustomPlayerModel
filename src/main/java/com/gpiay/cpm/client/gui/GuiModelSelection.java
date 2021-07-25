package com.gpiay.cpm.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.client.ModelEntry;
import com.gpiay.cpm.item.TransformationItem;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class GuiModelSelection extends Screen {
    private final List<ModelEntry> modelList = Lists.newArrayList();
    private final List<ModelEntry> searchResult = Lists.newArrayList();
    private TextFieldWidget searchInput;
    private SliderPercentageOption sliderOption;
    private Widget scaleSlider;
    private Button resetButton;
    private Button clearButton;

    private double sliderScale;
    private double sliderPos = 1;
    private boolean sliderUpdated;
    private int sliderUpdateTimer;

    private int scrollPos = 0;
    private int selected = -1;
    private String searchText = "";
    private int scrollHeight, itemCount;

    private int left, right, top, bottom;
    private int entryWidth, entryHeight;
    private final int itemHeight = 10;
    private final int scrollWidth = 9;

    public GuiModelSelection() {
        this(Collections.emptyList());
    }

    public GuiModelSelection(List<ModelEntry> serverModelList) {
        super(new TranslationTextComponent("gui.cpm.title"));

        modelList.addAll(serverModelList);
        Set<String> modelIds = Sets.newHashSet();
        for (ModelEntry entry : serverModelList)
            modelIds.add(entry.id);

        if (!CPMMod.cpmClient.isServerModded || (ClientConfig.SEND_MODELS.get() && ServerConfig.RECEIVE_MODELS.get())) {
            for (ModelEntry info : CPMMod.cpmServer.modelManager.getModelList())
                if (modelIds.add(info.id))
                    modelList.add(info);
        }

        if (!CPMMod.cpmClient.isServerModded || CPMMod.cpmServer.server != null) {
            try {
                CPMMod.startRecordingError();
                for (ModelEntry info : CPMMod.cpmClient.modelManager.getEditingModelList())
                    if (modelIds.add(info.id))
                        modelList.add(info);
                for (Exception e : CPMMod.endRecordingError())
                    throw e;
            } catch (Exception e) {
                CPMMod.print(e);
            }
        }

        modelList.sort(Comparator.comparing(o -> o.info.getName()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        left = width / 2 - 200;
        right = width / 2 - 40;
        top = height / 2 - 100;
        bottom = height / 2 + 90;

        entryWidth = right - left;
        entryHeight = bottom - top;
        itemCount = entryHeight / itemHeight;

        searchInput = new TextFieldWidget(minecraft.font, left, bottom + 2, entryWidth + scrollWidth, itemHeight, new StringTextComponent(""));
        searchInput.setMaxLength(256);
        searchInput.changeFocus(true);
        searchInput.setCanLoseFocus(false);
        this.children.add(searchInput);

        sliderOption = new SliderPercentageOption("gui.cpm.slider", -0.5, 0.5, 0.01f, setting -> sliderScale,
                (setting, value) -> updateSlider(value), (setting, percentage) -> new StringTextComponent(String.format("%.2f", sliderPos)));
        scaleSlider = sliderOption.createButton(minecraft.options, width / 2 + 40, height / 2, 120);
        this.children.add(scaleSlider);

        this.resetButton = new Button(this.width / 2 + 50, height / 2 + 25, 40, 20, new TranslationTextComponent("gui.cpm.reset"), button -> resetScale());
        this.clearButton = new Button(this.width / 2 + 110, height / 2 + 25, 40, 20, new TranslationTextComponent("gui.cpm.clear"), button -> clearModel());
        this.addButton(resetButton);
        this.addButton(clearButton);

        updateSearchEntry();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dWheel) {
        if (dWheel != 0) {
            int delta = dWheel < 0 ? 1 : -1;
            scrollPos = Math.max(0, Math.min(searchResult.size() - 1, scrollPos + delta));
        }

        return super.mouseScrolled(mouseX, mouseY, dWheel);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        int entityX = width / 2 + 100, entityY = height / 2 - 10, deltaY = 0;
        InventoryScreen.renderEntityInInventory(entityX, entityY, 50, entityX - mouseX, entityY - mouseY, minecraft.player);
        scaleSlider.render(matrixStack, mouseX, mouseY, partialTicks);

        if (selected >= 0) {
            for (ITextComponent str : searchResult.get(selected).getModelInfo())
                drawCenteredString(matrixStack, minecraft.font, str, entityX, entityY + 50 + (deltaY += 10), 0xffffffff);
        }

        searchInput.render(matrixStack, mouseX, mouseY, partialTicks);
        GlStateManager._disableDepthTest();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        fill(matrixStack, left, top, right, bottom, 0xbf3f3f3f);

        fill(matrixStack, right, top, right + scrollWidth, bottom, 0xbf000000);
        int scrollTop = top + (searchResult.isEmpty() ? 0 : entryHeight * scrollPos / searchResult.size());
        fill(matrixStack, right, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffffffff);
        fill(matrixStack, right + scrollWidth * 2 / 3, scrollTop, right + scrollWidth, scrollTop + scrollHeight, 0xffbfbfbf);

        for (int i = Math.min(scrollPos + itemCount, searchResult.size()) - 1; i >= scrollPos; i--) {
            ModelEntry model = searchResult.get(i);
            int itemTop = top + (i - scrollPos) * itemHeight;
            if (selected == i) {
                fill(matrixStack, left, itemTop, left + entryWidth, itemTop + itemHeight, 0xffffffff);
                fill(matrixStack, left + 1, itemTop + 1, left + entryWidth - 1, itemTop + itemHeight - 1, 0xff000000);
            }

            List<IReorderingProcessor> str = minecraft.font.split(new StringTextComponent(model.info.getName()), entryWidth - 7);
            int color = model.isLocal ? model.isEditing ? 0xffffff55 : 0xffaaaaaa : 0xffffffff;
            minecraft.font.draw(matrixStack, str.get(0), left + 1, itemTop + 1, color);

        }

        GlStateManager._enableDepthTest();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
        } else {
            searchInput.charTyped(typedChar, keyCode);
        }

        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        searchInput.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseY >= top && mouseY < bottom && mouseX >= left && mouseX < right) {
            int index = ((int) mouseY - top) / itemHeight + scrollPos;
            if (index < searchResult.size())
                setSelected(index, mouseButton);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setSelected(int index, int button) {
        if (index >= 0) {
            ModelEntry model = searchResult.get(index);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                try {
                    if (CPMMod.cpmClient.isServerModded) {
                        if (model.isLocal && model.isEditing) {
                            sendMessage("/" + CPMMod.MOD_ID + " clear", false);
                            CPMMod.startRecordingError();
                            minecraft.player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                    ((ClientCPMCapability) cap).loadEditingModel(model.id));
                            for (Exception e : CPMMod.endRecordingError())
                                throw e;
                        } else {
                            sendMessage("/" + CPMMod.MOD_ID + " select " + model.id, false);
                        }
                    } else {
                        minecraft.player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                cap.setModelId(model.id));
                    }
                } catch (Exception e) {
                    CPMMod.print(e);
                }
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                ItemStack itemStack = minecraft.player.getMainHandItem();
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof TransformationItem) {
                    sendMessage("/" + CPMMod.MOD_ID + " create " + model.id + " " + sliderPos);
                } else {
                    minecraft.keyboardHandler.setClipboard(model.id);
                    Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("gui.cpm.clipboard"));
                }
            }
        }

        selected = index;
    }

    private void updateSlider(double scale) {
        sliderScale = scale;
        sliderPos = Math.pow(10, scale);
        sliderUpdated = true;
    }

    private void resetScale() {
        sliderScale = 0;
        sliderPos = 1;
        sliderUpdated = true;
    }

    private void clearModel() {
        if (CPMMod.cpmClient.isServerModded) {
            sendMessage("/" + CPMMod.MOD_ID + " clear", false);
        } else {
            minecraft.player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                    cap.setModelId(""));
        }
    }

    @Override
    public void tick() {
        searchInput.tick();
        if (!searchInput.getValue().equals(searchText)) {
            searchText = searchInput.getValue().toLowerCase();
            updateSearchEntry();
        }

        if (++sliderUpdateTimer == 4) {
            sliderUpdateTimer = 0;
            if (sliderUpdated) {
                sliderUpdated = false;
                if (CPMMod.cpmClient.isServerModded) {
                    sendMessage("/" + CPMMod.MOD_ID + " scale " + sliderPos, false);
                }
            }
        }
    }

    private void updateSearchEntry() {
        searchResult.clear();
        for (ModelEntry model : modelList) {
            if (model.search(searchText))
                searchResult.add(model);
        }

        scrollHeight = searchResult.isEmpty() ? entryHeight : Math.max(1, entryHeight / searchResult.size());
        scrollPos = Math.min(scrollPos, Math.max(0, searchResult.size() - 1));
        setSelected(-1, 0);
    }
}
