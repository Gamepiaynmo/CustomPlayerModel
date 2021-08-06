package com.gpiay.cpm;

import com.google.common.collect.Lists;
import com.gpiay.cpm.client.CPMClient;
import com.gpiay.cpm.server.CPMServer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class CPMMod {
    public static final String MOD_ID = "cpm";
    public static final String CPM_DIR = "custom-model/";
    public static final File CACHE_DIR = new File(CPM_DIR + "cache");
    public static final File MODEL_DIR = new File(CPM_DIR + "models");

    public static final Logger LOGGER = LogManager.getLogger("CustomPlayerModel");

    public static CPMServer cpmServer = null;
    public static CPMClient cpmClient = null;

    public static final int customKeyCount = 8;

    private static final List<Exception> errorRecord = Lists.newArrayList();

    public static void startRecordingError() {
        errorRecord.clear();
    }

    public static List<Exception> endRecordingError() {
        return errorRecord;
    }

    public static void warn(Exception e) {
        CPMMod.LOGGER.warn(e.getMessage(), e);
        errorRecord.add(e);
    }

    public static void print(Exception e) {
        IFormattableTextComponent text = new StringTextComponent(e.toString());
        text.setStyle(text.getStyle().applyFormat(TextFormatting.RED));
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }
}
