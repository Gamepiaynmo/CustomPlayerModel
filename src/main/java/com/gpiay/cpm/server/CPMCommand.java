package com.gpiay.cpm.server;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelManager;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.util.function.ExceptionFunction;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.Collection;
import java.util.function.Predicate;

public class CPMCommand {
    private static Predicate<CommandSource> permission(String node) {
        return source -> {
            try {
                return PermissionAPI.hasPermission(source.asPlayer(), node);
            } catch (CommandSyntaxException e) {
                return true;
            }
        };
    }

    private static boolean hasPermission(String modelId, PlayerEntity player) {
        return CPMMod.cpmServer.modelManager.hasPermission(modelId, player);
    }

    private static Command<CommandSource> execute(ExceptionFunction<CommandContext<CommandSource>, Integer, Exception> executor) {
        return context -> {
            try {
                CPMMod.startRecordingError();
                int result = executor.apply(context);
                for (Exception e : CPMMod.endRecordingError())
                    throw e;

                return result;
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Exception e) {
                String message = e.getLocalizedMessage();
                ITextComponent text = new StringTextComponent(message == null ? "" : message);
                text.getStyle().setColor(TextFormatting.RED);
                context.getSource().sendFeedback(text, false);
                return 0;
            }
        };
    }

    private static ServerPlayerEntity getPlayerOrNull(CommandSource source) {
        try {
            return source.asPlayer();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public static void registerCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(CPMMod.MOD_ID)
                .then(Commands.literal("select")
                        .then(Commands.argument("model", StringArgumentType.word())
                                .requires(permission("cpm.command.selectSelf"))
                                .executes(execute(context -> {
                                    String modelId = context.getArgument("model", String.class);
                                    if (hasPermission(modelId, context.getSource().asPlayer())) {
                                        context.getSource().asPlayer().getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                                ((ServerCPMCapability) cap).setModelId(modelId, getPlayerOrNull(context.getSource())));
                                    }
                                    return 1;
                                }))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .requires(permission("cpm.command.selectOthers"))
                                        .executes(execute(context -> {
                                            String modelId = context.getArgument("model", String.class);
                                            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                            if (hasPermission(modelId, context.getSource().asPlayer())) {
                                                for (Entity entity : entities) {
                                                    boolean[] shouldBreak = {false};
                                                    entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> {
                                                        ServerCPMCapability capability = (ServerCPMCapability) cap;
                                                        capability.setModelId(modelId, getPlayerOrNull(context.getSource()));
                                                        if (!capability.getModelId().equals(modelId))
                                                            shouldBreak[0] = true;
                                                    });

                                                    if (shouldBreak[0])
                                                        break;
                                                }
                                            }
                                            return entities.size();
                                        })))))
                .then(Commands.literal("clear")
                        .requires(permission("cpm.command.selectSelf"))
                        .executes(execute(context -> {
                            context.getSource().asPlayer().getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                    cap.setModelId(""));
                            return 1;
                        }))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .requires(permission("cpm.command.selectOthers"))
                                .executes(execute(context -> {
                                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                    for (Entity entity : entities) {
                                        entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                                cap.setModelId(""));
                                    }
                                    return entities.size();
                                }))))
                .then(Commands.literal("scale")
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg())
                                .requires(permission("cpm.command.scaleSelf"))
                                .executes(execute(context -> {
                                    context.getSource().asPlayer().getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                            cap.setScale(context.getArgument("scale", Double.class)));
                                    return 1;
                                }))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .requires(permission("cpm.command.scaleOthers"))
                                        .executes(execute(context -> {
                                            double scale = context.getArgument("scale", Double.class);
                                            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                            for (Entity entity : entities) {
                                                entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                                        cap.setScale(scale));
                                            }
                                            return entities.size();
                                        })))))
                .then(Commands.literal("refresh")
                        .requires(permission("cpm.command.refreshModels"))
                        .executes(execute(context -> {
                            ModelManager.initLocalModels();
                            return 1;
                        }))));
    }
}
