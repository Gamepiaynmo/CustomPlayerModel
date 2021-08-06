package com.gpiay.cpm;

import com.gpiay.cpm.client.CPMClient;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.item.TransformationItem;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.server.CPMCommand;
import com.gpiay.cpm.server.CPMServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

public class CPMEntry implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
	public static final Item transformationWand = new TransformationItem(new FabricItemSettings().durability(10));

	public static final ItemGroup CPM_ITEMS = FabricItemGroupBuilder.create(
				new ResourceLocation(CPMMod.MOD_ID, "cpm"))
			.icon(() -> new ItemStack(transformationWand))
			.appendItems(itemStacks -> {
				itemStacks.add(new ItemStack(transformationWand));
			})
			.build();

	@Override
	public void onInitialize() {
		CPMMod.cpmServer = new CPMServer();

		CPMConfig.registerConfig();
		NetworkHandler.registerPackets();

		Registry.register(Registry.ITEM, new ResourceLocation(CPMMod.MOD_ID, "transformation_wand"), transformationWand);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> CPMMod.cpmServer.server = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> CPMMod.cpmServer.server = null);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			CPMCommand.registerCommand(dispatcher);
		});
	}

	@Override
	public void onInitializeClient() {
		CPMMod.cpmClient = new CPMClient();

		ClientTickEvents.END_CLIENT_TICK.register(client -> CPMMod.cpmClient.onClientTick());

		CPMMod.cpmClient.selectModelKey = new KeyBinding(
				"key.cpm.selectModel", GLFW.GLFW_KEY_M, "key.cpm.category");
		KeyBindingHelper.registerKeyBinding(CPMMod.cpmClient.selectModelKey);
		for (int i = 0; i < CPMMod.customKeyCount; i++) {
			CPMMod.cpmClient.customKeys[i] = new KeyBinding("key.cpm.customKey" + (i + 1),
					i < 4 ? GLFW.GLFW_KEY_F6 + i : -1, "key.cpm.category");
			KeyBindingHelper.registerKeyBinding(CPMMod.cpmClient.customKeys[i]);
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> CPMMod.cpmClient.registerRenderLayers());
	}

	@Override
	public void onInitializeServer() {

	}
}
