package com.gpiay.cpm.server;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ICPMAttachment;
import com.gpiay.cpm.entity.ServerCPMAttachment;
import com.gpiay.cpm.item.TransformationItem;
import com.gpiay.cpm.util.function.ExceptionFunction;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.function.Predicate;

public class CPMCommand {
    private static final SimpleCommandExceptionType ERROR_ITEM = new SimpleCommandExceptionType(new TranslationTextComponent("error.cpm.wrongItem"));

    private static Predicate<CommandSource> permission(String node) {
        return source -> {
            try {
                return CPMConfig.checkCommandPermission(source.getPlayerOrException(), node);
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
                context.getSource().sendFailure(new StringTextComponent(e.toString()));
                return 0;
            }
        };
    }

    private static ServerPlayerEntity getPlayerOrNull(CommandSource source) {
        try {
            return source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public static void registerCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(CPMMod.MOD_ID)
                .then(Commands.literal("model")
                        .then(Commands.literal("select")
                                .then(Commands.argument("model", StringArgumentType.word())
                                        .requires(permission("cpm.command.selectSelf"))
                                        .executes(execute(context -> {
                                            String modelId = context.getArgument("model", String.class);
                                            if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(attachment ->
                                                        ((ServerCPMAttachment) attachment).setMainModel(modelId, getPlayerOrNull(context.getSource())));
                                            }
                                            return 1;
                                        }))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .requires(permission("cpm.command.selectOthers"))
                                                .executes(execute(context -> {
                                                    String modelId = context.getArgument("model", String.class);
                                                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                                    if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                        for (Entity entity : entities) {
                                                            boolean[] shouldBreak = {false};
                                                            AttachmentProvider.getEntityAttachment(entity).ifPresent(attach -> {
                                                                ServerCPMAttachment attachment = (ServerCPMAttachment) attach;
                                                                attachment.setMainModel(modelId, getPlayerOrNull(context.getSource()));
                                                                if (!attachment.getMainModel().equals(modelId))
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
                                    AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(attachment ->
                                            attachment.setMainModel(""));
                                    return 1;
                                }))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .requires(permission("cpm.command.selectOthers"))
                                        .executes(execute(context -> {
                                            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                            for (Entity entity : entities) {
                                                AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment ->
                                                        attachment.setMainModel(""));
                                            }
                                            return entities.size();
                                        })))))
                .then(Commands.literal("accessory")
                        .then(Commands.literal("add")
                                .then(Commands.argument("model", StringArgumentType.word())
                                        .requires(permission("cpm.command.selectSelf"))
                                        .executes(execute(context -> {
                                            String modelId = context.getArgument("model", String.class);
                                            if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(attachment ->
                                                        ((ServerCPMAttachment) attachment).addAccessory(modelId, getPlayerOrNull(context.getSource())));
                                            }
                                            return 1;
                                        }))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .requires(permission("cpm.command.selectOthers"))
                                                .executes(execute(context -> {
                                                    String modelId = context.getArgument("model", String.class);
                                                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                                    if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                        for (Entity entity : entities) {
                                                            boolean[] shouldBreak = {false};
                                                            AttachmentProvider.getEntityAttachment(entity).ifPresent(attach -> {
                                                                ServerCPMAttachment attachment = (ServerCPMAttachment) attach;
                                                                attachment.addAccessory(modelId, getPlayerOrNull(context.getSource()));
                                                                if (!attachment.getAccessories().contains(modelId))
                                                                    shouldBreak[0] = true;
                                                            });

                                                            if (shouldBreak[0])
                                                                break;
                                                        }
                                                    }
                                                    return entities.size();
                                                })))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("model", StringArgumentType.word())
                                        .requires(permission("cpm.command.selectSelf"))
                                        .executes(execute(context -> {
                                            String modelId = context.getArgument("model", String.class);
                                            if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(attachment ->
                                                        attachment.removeAccessory(modelId));
                                            }
                                            return 1;
                                        }))
                                        .then(Commands.argument("targets", EntityArgument.entities())
                                                .requires(permission("cpm.command.selectOthers"))
                                                .executes(execute(context -> {
                                                    String modelId = context.getArgument("model", String.class);
                                                    Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                                    if (hasPermission(modelId, context.getSource().getPlayerOrException())) {
                                                        for (Entity entity : entities) {
                                                            AttachmentProvider.getEntityAttachment(entity).ifPresent(attach -> {
                                                                ServerCPMAttachment attachment = (ServerCPMAttachment) attach;
                                                                attachment.removeAccessory(modelId);
                                                            });
                                                        }
                                                    }
                                                    return entities.size();
                                                })))))
                        .then(Commands.literal("clear")
                                .requires(permission("cpm.command.selectSelf"))
                                .executes(execute(context -> {
                                    AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(ICPMAttachment::clearAccessories);
                                    return 1;
                                }))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .requires(permission("cpm.command.selectOthers"))
                                        .executes(execute(context -> {
                                            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                            for (Entity entity : entities)
                                                AttachmentProvider.getEntityAttachment(entity).ifPresent(ICPMAttachment::clearAccessories);
                                            return entities.size();
                                        })))))
                .then(Commands.literal("scale")
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg())
                                .requires(permission("cpm.command.scaleSelf"))
                                .executes(execute(context -> {
                                    AttachmentProvider.getEntityAttachment(context.getSource().getPlayerOrException()).ifPresent(attachment ->
                                            attachment.setScale(context.getArgument("scale", Double.class)));
                                    return 1;
                                }))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .requires(permission("cpm.command.scaleOthers"))
                                        .executes(execute(context -> {
                                            double scale = context.getArgument("scale", Double.class);
                                            Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
                                            for (Entity entity : entities) {
                                                AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment ->
                                                        attachment.setScale(scale));
                                            }
                                            return entities.size();
                                        })))))
                .then(Commands.literal("create")
                        .requires(permission("cpm.command.createItem"))
                        .then(Commands.argument("model", StringArgumentType.word())
                                .executes(execute(context -> {
                                    PlayerEntity player = context.getSource().getPlayerOrException();
                                    ItemStack itemStack = player.getMainHandItem();
                                    if (!itemStack.isEmpty() && itemStack.getItem() instanceof TransformationItem) {
                                        TransformationItem transItem = ((TransformationItem) itemStack.getItem());
                                        transItem.setModel(itemStack, context.getArgument("model", String.class));
                                    }
                                    else throw ERROR_ITEM.create();
                                    return 1;
                                }))
                                .then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.01))
                                        .executes(execute(context -> {
                                            PlayerEntity player = context.getSource().getPlayerOrException();
                                            ItemStack itemStack = player.getMainHandItem();
                                            if (!itemStack.isEmpty() && itemStack.getItem() instanceof TransformationItem) {
                                                TransformationItem transItem = ((TransformationItem) itemStack.getItem());
                                                transItem.setModel(itemStack, context.getArgument("model", String.class));
                                                transItem.setScale(itemStack, context.getArgument("scale", Double.class));
                                            }
                                            else throw ERROR_ITEM.create();
                                            return 1;
                                        })))))
                .then(Commands.literal("refresh")
                        .requires(permission("cpm.command.refreshModels"))
                        .executes(execute(context -> {
                            ModelManager.initLocalModels();
                            return 1;
                        }))));
    }
}
