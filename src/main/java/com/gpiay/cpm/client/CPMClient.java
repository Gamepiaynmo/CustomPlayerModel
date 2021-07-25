package com.gpiay.cpm.client;

import com.google.common.collect.Sets;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.gui.GuiModelSelection;
import com.gpiay.cpm.client.render.*;
import com.gpiay.cpm.client.render.item.*;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.QueryModelListPacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import com.gpiay.cpm.util.math.Matrix4d;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.EntityModel;
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
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import javax.script.ScriptEngine;
import java.io.File;
import java.util.List;
import java.util.Set;

public class CPMClient {
    public KeyBinding selectModelKey = new KeyBinding(
            "key.cpm.selectModel", KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
            InputMappings.getKey("key.keyboard.m"), "key.cpm.category");
    public KeyBinding[] customKeys = new KeyBinding[CPMMod.customKeyCount];

    public boolean isServerModded = false;

    public ClientModelManager modelManager = new ClientModelManager(this);
    public File editingModel = null;

    private final Set<EntityRenderer<? extends LivingEntity>> processedRenderers = Sets.newHashSet();

    public final ScriptEngine scriptEngine;

    public CPMClient() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        MinecraftForge.EVENT_BUS.register(this);

        ClientRegistry.registerKeyBinding(selectModelKey);
        for (int i = 0; i < CPMMod.customKeyCount; i++) {
            customKeys[i] = new KeyBinding("key.cpm.customKey" + (i + 1), KeyConflictContext.IN_GAME,
                    KeyModifier.NONE, i < 4 ? InputMappings.getKey("key.keyboard.f" + (i + 6)) :
                    InputMappings.UNKNOWN, "key.cpm.category");
            ClientRegistry.registerKeyBinding(customKeys[i]);
        }

        NashornScriptEngineFactory scriptEngineFactory = new NashornScriptEngineFactory();
        scriptEngine = scriptEngineFactory.getScriptEngine(s -> false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void processRenderer(EntityRenderer renderer) {
        if (renderer instanceof LivingRenderer && !processedRenderers.contains(renderer)) {
            LivingRenderer livingRenderer = (LivingRenderer) renderer;
            List<LayerRenderer> layers = livingRenderer.layers;
            layers.add(0, new CPMLayer(livingRenderer));

            for (int i = 1; i < layers.size(); i++) {
                LayerRenderer layer = layers.get(i);

                if (layer instanceof HeldItemLayer) layer = new CPMHeldItemLayer(livingRenderer, (HeldItemLayer) layer);
                else if (layer instanceof BipedArmorLayer) layer = new CPMArmorLayer(livingRenderer, (BipedArmorLayer) layer);
                else if (layer instanceof ArrowLayer) layer = new CPMArrowLayer(livingRenderer, (ArrowLayer) layer);
                else if (layer instanceof CapeLayer) layer = new CPMCapeLayer(livingRenderer, (CapeLayer) layer);
                else if (layer instanceof ElytraLayer) layer = new CPMElytraLayer(livingRenderer, (ElytraLayer) layer);
                else if (layer instanceof HeadLayer) layer = new CPMHeadLayer(livingRenderer, (HeadLayer) layer);
                else if (layer instanceof ParrotVariantLayer) layer = new CPMParrotLayer(livingRenderer, (ParrotVariantLayer) layer);
                else if (layer instanceof BeeStingerLayer) layer = new CPMBeeStingerLayer(livingRenderer, (BeeStingerLayer) layer);
                else if (layer instanceof DolphinCarriedItemLayer) layer = new CPMDolphinItemLayer(livingRenderer, (DolphinCarriedItemLayer) layer);
                else if (layer instanceof FoxHeldItemLayer) layer = new CPMFoxItemLayer(livingRenderer, (FoxHeldItemLayer) layer);
                else if (layer instanceof WitchHeldItemLayer) layer = new CPMWitchItemLayer(livingRenderer, (WitchHeldItemLayer) layer);
                else if (layer instanceof HeldBlockLayer) layer = new CPMEndermanItemLayer(livingRenderer, (HeldBlockLayer) layer);
                else if (layer instanceof IronGolenFlowerLayer) layer = new CPMIronGolemItemLayer(livingRenderer, (IronGolenFlowerLayer) layer);
                else if (layer instanceof PandaHeldItemLayer) layer = new CPMPandaItemLayer(livingRenderer, (PandaHeldItemLayer) layer);
                else if (!(layer instanceof SpinAttackEffectLayer)) layer = new CPMHideLayer(livingRenderer, layer);

                layers.set(i, layer);
            }

            processedRenderers.add(renderer);
        }
    }

    public void registerRenderLayers() {
        EntityRendererManager rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();

        for (PlayerRenderer renderer : rendererManager.getSkinMap().values()) {
            processRenderer(renderer);
        }
    }

    public void showModelSelectionGui(List<ModelEntry> modelList) {
        Minecraft.getInstance().setScreen(new GuiModelSelection(modelList));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientWorld world = mc.level;
        modelManager.tick();

        if (world != null && !mc.isPaused() && event.phase == TickEvent.Phase.END) {
            if (selectModelKey.consumeClick()) {
                if (isServerModded) {
                    Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new QueryModelListPacket());
                } else mc.setScreen(new GuiModelSelection());
            }

            boolean[] keyState = new boolean[CPMMod.customKeyCount];
            for (int i = 0; i < CPMMod.customKeyCount; i++)
                keyState[i] = customKeys[i].isDown();
            mc.player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                    ((ClientCPMCapability) cap).updateCustomKeyState(keyState));

            for (Entity entity : world.entitiesForRendering()) {
                entity.getCapability(CPMCapability.CAPABILITY).ifPresent(ICPMCapability::update);
            }
        }
    }

    @SubscribeEvent
    public void onPreRenderLiving(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event) {
        event.getEntity().getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            if (((ClientCPMCapability) capability).getModel() != null) {
                ModelInstance.baseModelView = new Matrix4d(event.getMatrixStack().last().pose()).inv();
            }
        });
    }
}
