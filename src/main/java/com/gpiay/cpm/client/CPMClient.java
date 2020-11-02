package com.gpiay.cpm.client;

import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.gui.GuiModelSelection;
import com.gpiay.cpm.client.gui.ModelEntry;
import com.gpiay.cpm.client.render.*;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.QueryModelListPacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import com.gpiay.cpm.util.math.Matrix4d;
import com.mojang.blaze3d.platform.GlStateManager;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.opengl.GL11;

import javax.script.ScriptEngine;
import java.io.File;
import java.util.List;
import java.util.Set;

public class CPMClient {
    public KeyBinding selectModelKey = new KeyBinding(
            "key.cpm.selectModel", KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
            InputMappings.getInputByName("key.keyboard.m"), "key.cpm.category");
    public KeyBinding[] customKeys = new KeyBinding[]{
            new KeyBinding("key.cpm.customKey1", KeyConflictContext.IN_GAME, KeyModifier.NONE,
                    InputMappings.getInputByName("key.keyboard.f6"), "key.cpm.category"),
            new KeyBinding("key.cpm.customKey2", KeyConflictContext.IN_GAME, KeyModifier.NONE,
                    InputMappings.getInputByName("key.keyboard.f7"), "key.cpm.category"),
            new KeyBinding("key.cpm.customKey3", KeyConflictContext.IN_GAME, KeyModifier.NONE,
                    InputMappings.getInputByName("key.keyboard.f8"), "key.cpm.category"),
            new KeyBinding("key.cpm.customKey4", KeyConflictContext.IN_GAME, KeyModifier.NONE,
                    InputMappings.getInputByName("key.keyboard.f9"), "key.cpm.category")
    };

    public boolean isServerModded = false;

    public ClientModelManager modelManager = new ClientModelManager(this);
    public File editingModel = null;

    private final Set<LivingRenderer<? extends LivingEntity, ? extends EntityModel<?>>> processedRenderers = Sets.newHashSet();

    public final ScriptEngine scriptEngine;

    public CPMClient() {
        MinecraftForge.EVENT_BUS.register(this);

        ClientRegistry.registerKeyBinding(selectModelKey);
        for (KeyBinding customKey : customKeys)
            ClientRegistry.registerKeyBinding(customKey);

        NashornScriptEngineFactory scriptEngineFactory = new NashornScriptEngineFactory();
        scriptEngine = scriptEngineFactory.getScriptEngine(s -> false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <E extends LivingEntity, M extends EntityModel<E>> void processRenderer(LivingRenderer<E, M> renderer) {
        if (renderer != null && !processedRenderers.contains(renderer)) {
            List<LayerRenderer<E, M>> layers = renderer.layerRenderers;
            layers.add(0, new CPMRenderLayer<>(renderer));

            for (int i = 1; i < layers.size(); i++) {
                LayerRenderer layer = layers.get(i);

                if (layer instanceof HeldItemLayer) layer = new CPMHeldItemLayer(renderer, (HeldItemLayer) layer);
                else if (layer instanceof BipedArmorLayer) layer = new CPMArmorLayer(renderer, (BipedArmorLayer) layer);
                else if (layer instanceof ArrowLayer) layer = new CPMArrowLayer(renderer, (ArrowLayer) layer);
                else if (layer instanceof CapeLayer) layer = new CPMCapeLayer(renderer, (CapeLayer) layer);
                else if (layer instanceof ElytraLayer) layer = new CPMElytraLayer(renderer, (ElytraLayer) layer);
                else if (layer instanceof HeadLayer) layer = new CPMHeadLayer(renderer, (HeadLayer) layer);
                else if (layer instanceof ParrotVariantLayer) layer = new CPMParrotLayer(renderer, (ParrotVariantLayer) layer);
                else layer = new CPMHideLayer(renderer, layer);

                layers.set(i, layer);
            }

            processedRenderers.add(renderer);
        }
    }

    public void registerRenderLayers() {
        EntityRendererManager rendererManager = Minecraft.getInstance().getRenderManager();

        for (PlayerRenderer renderer : rendererManager.getSkinMap().values()) {
            processRenderer(renderer);
        }
    }

    public void showModelSelectionGui(List<ModelEntry> modelList) {
        Minecraft.getInstance().displayGuiScreen(new GuiModelSelection(modelList));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientWorld world = mc.world;
        if (world != null && !mc.isGamePaused() && event.phase == TickEvent.Phase.END) {
            modelManager.tick();
            if (selectModelKey.isPressed()) {
                if (isServerModded) {
                    Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new QueryModelListPacket());
                } else mc.displayGuiScreen(new GuiModelSelection());
            }

            boolean[] keyState = new boolean[CPMMod.customKeyCount];
            for (int i = 0; i < CPMMod.customKeyCount; i++)
                keyState[i] = customKeys[i].isKeyDown();
            mc.player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> {
                ((ClientCPMCapability) cap).updateCustomKeyState(keyState);
            });

            for (Entity entity : world.getAllEntities()) {
                entity.getCapability(CPMCapability.CAPABILITY).ifPresent(ICPMCapability::update);
            }
        }
    }

    @SubscribeEvent
    public void onPreRenderLiving(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event) {
        event.getEntity().getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            if (((ClientCPMCapability) capability).getModel() != null) {
                ModelInstance.baseModelView = new Matrix4d(GlStateManager.getMatrix4f(GL11.GL_MODELVIEW_MATRIX)).inv();
                if (event.getX() == 0 && event.getY() == 0 && event.getZ() == 0 && event.getPartialRenderTick() == 1)
                    ModelInstance.isRenderingInventory = true;
                for (RendererModel bone : event.getRenderer().getEntityModel().boxList)
                    bone.isHidden = true;
            }
        });
    }

    @SubscribeEvent
    public void onPostRenderLiving(RenderLivingEvent.Post<? extends LivingEntity, ? extends EntityModel<?>> event) {
        event.getEntity().getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            if (((ClientCPMCapability) capability).getModel() != null) {
                for (RendererModel bone : event.getRenderer().getEntityModel().boxList)
                    bone.isHidden = false;
                ModelInstance.isRenderingInventory = false;
            }
        });
    }
}
