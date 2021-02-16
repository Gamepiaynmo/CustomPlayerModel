function initializeCoreMod() {
    return {
        'LivingRenderer#render': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.LivingRenderer',
                'methodName': 'func_225623_a_',
                'methodDesc': '(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching LivingRenderer#render');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var Label = Java.type('org.objectweb.asm.Label');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

                var beginPos = ASMAPI.findFirstMethodCall(method,
                    ASMAPI.MethodType.VIRTUAL,
                    'net/minecraft/client/renderer/entity/model/EntityModel',
                    'setLivingAnimations', '(Lnet/minecraft/entity/Entity;FFF)V');
                while (beginPos.getOpcode() !== Opcodes.ALOAD || beginPos.var !== 0)
                    beginPos = beginPos.getPrevious()

                var middlePos = ASMAPI.findFirstMethodCall(method,
                    ASMAPI.MethodType.VIRTUAL,
                    'net/minecraft/client/renderer/entity/model/EntityModel',
                    'render', '(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V');

                var endPos = ASMAPI.findFirstMethodCall(method,
                    ASMAPI.MethodType.SPECIAL,
                    'net/minecraft/client/renderer/entity/EntityRenderer',
                    'render', '(Lnet/minecraft/entity/Entity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V');

                var middleInsn = new LabelNode(new Label());
                var endInsn = new LabelNode(new Label());

                var beginInsn = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    ASMAPI.buildMethodCall(
                        'com/gpiay/cpm/hook/LivingRendererHook',
                        'onMatrixCallback', '(Lcom/mojang/blaze3d/matrix/MatrixStack;)Z',
                        ASMAPI.MethodType.STATIC),
                    new JumpInsnNode(Opcodes.IFEQ, endInsn),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    new VarInsnNode(Opcodes.ALOAD, 5),
                    new VarInsnNode(Opcodes.ILOAD, 6),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.FLOAD, 14),
                    new VarInsnNode(Opcodes.FLOAD, 13),
                    new VarInsnNode(Opcodes.FLOAD, 3),
                    new VarInsnNode(Opcodes.FLOAD, 12),
                    new VarInsnNode(Opcodes.FLOAD, 10),
                    new VarInsnNode(Opcodes.FLOAD, 11),
                    ASMAPI.buildMethodCall(
                        'com/gpiay/cpm/hook/LivingRendererHook',
                        'isRenderModel', '(Lnet/minecraft/client/renderer/entity/LivingRenderer;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/LivingEntity;FFFFFF)Z',
                        ASMAPI.MethodType.STATIC),
                    new JumpInsnNode(Opcodes.IFEQ, middleInsn));
                
                var instructions = method.instructions
                instructions.insertBefore(beginPos, beginInsn)
                instructions.insert(middlePos, middleInsn)
                instructions.insert(endPos, endInsn)

                ASMAPI.log('INFO', '[CPMCore]: Patched LivingRenderer#render');
                return method;
            }
        }
    }
}

// if (LivingRendererHook.onMatrixCallback(matrixStackIn)) {
//     if (LivingRendererHook.isRenderModel(this)) {
//         this.entityModel.setLivingAnimations(entityIn, f5, f8, partialTicks);
//         this.entityModel.setRotationAngles(entityIn, f5, f8, f7, f2, f6);
//         Minecraft minecraft = Minecraft.getInstance();
//         boolean flag = this.isVisible(entityIn);
//         boolean flag1 = !flag && !entityIn.isInvisibleToPlayer(minecraft.player);
//         boolean flag2 = minecraft.isEntityGlowing(entityIn);
//         RenderType rendertype = this.func_230496_a_(entityIn, flag, flag1, flag2);
//         if (rendertype != null) {
//             IVertexBuilder ivertexbuilder = bufferIn.getBuffer(rendertype);
//             int i = getPackedOverlay(entityIn, this.getOverlayProgress(entityIn, partialTicks));
//             this.entityModel.render(matrixStackIn, ivertexbuilder, packedLightIn, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
//         }
//     }
//
//     if (!entityIn.isSpectator()) {
//         for(LayerRenderer<T, M> layerrenderer : this.layerRenderers) {
//             layerrenderer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, f5, f8, partialTicks, f7, f2, f6);
//         }
//     }
//
//     matrixStackIn.pop();
//     super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
// }
//
// aload 4
// invokestatic LivingRendererHook.onMatrixCallback
// ifeq label_1
// aload_0
// invokestatic LivingRendererHook.isRenderModel
// ifeq label_2
//
// 439 aload_0
// 440 getfield #4 <net/minecraft/client/renderer/entity/LivingRenderer.entityModel>
// 443 aload_1
// 444 fload 14
// 446 fload 13
// 448 fload_3
// 449 invokevirtual #55 <net/minecraft/client/renderer/entity/model/EntityModel.setLivingAnimations>
// 452 aload_0
// 453 getfield #4 <net/minecraft/client/renderer/entity/LivingRenderer.entityModel>
// 456 aload_1
// 457 fload 14
// 459 fload 13
// 461 fload 12
// 463 fload 10
// 465 fload 11
// 467 invokevirtual #56 <net/minecraft/client/renderer/entity/model/EntityModel.setRotationAngles>
// 470 invokestatic #57 <net/minecraft/client/Minecraft.getInstance>
// 473 astore 15
// 475 aload_0
// 476 aload_1
// 477 invokevirtual #58 <net/minecraft/client/renderer/entity/LivingRenderer.isVisible>
// 480 istore 16
// 482 iload 16
// 484 ifne 503 (+19)
// 487 aload_1
// 488 aload 15
// 490 getfield #59 <net/minecraft/client/Minecraft.player>
// 493 invokevirtual #60 <net/minecraft/entity/LivingEntity.isInvisibleToPlayer>
// 496 ifne 503 (+7)
// 499 iconst_1
// 500 goto 504 (+4)
// 503 iconst_0
// 504 istore 17
// 506 aload 15
// 508 aload_1
// 509 invokevirtual #61 <net/minecraft/client/Minecraft.isEntityGlowing>
// 512 istore 18
// 514 aload_0
// 515 aload_1
// 516 iload 16
// 518 iload 17
// 520 iload 18
// 522 invokevirtual #62 <net/minecraft/client/renderer/entity/LivingRenderer.func_230496_a_>
// 525 astore 19
// 527 aload 19
// 529 ifnull 584 (+55)
// 532 aload 5
// 534 aload 19
// 536 invokeinterface #63 <net/minecraft/client/renderer/IRenderTypeBuffer.getBuffer> count 2
// 541 astore 20
// 543 aload_1
// 544 aload_0
// 545 aload_1
// 546 fload_3
// 547 invokevirtual #64 <net/minecraft/client/renderer/entity/LivingRenderer.getOverlayProgress>
// 550 invokestatic #65 <net/minecraft/client/renderer/entity/LivingRenderer.getPackedOverlay>
// 553 istore 21
// 555 aload_0
// 556 getfield #4 <net/minecraft/client/renderer/entity/LivingRenderer.entityModel>
// 559 aload 4
// 561 aload 20
// 563 iload 6
// 565 iload 21
// 567 fconst_1
// 568 fconst_1
// 569 fconst_1
// 570 iload 17
// 572 ifeq 580 (+8)
// 575 ldc #66 <0.15>
// 577 goto 581 (+4)
// 580 fconst_1
// 581 invokevirtual #67 <net/minecraft/client/renderer/entity/model/EntityModel.render>
//
// label_2
//
// 584 aload_1
// 585 invokevirtual #68 <net/minecraft/entity/LivingEntity.isSpectator>
// 588 ifne 650 (+62)
// 591 aload_0
// 592 getfield #3 <net/minecraft/client/renderer/entity/LivingRenderer.layerRenderers>
// 595 invokeinterface #69 <java/util/List.iterator> count 1
// 600 astore 20
// 602 aload 20
// 604 invokeinterface #70 <java/util/Iterator.hasNext> count 1
// 609 ifeq 650 (+41)
// 612 aload 20
// 614 invokeinterface #71 <java/util/Iterator.next> count 1
// 619 checkcast #72 <net/minecraft/client/renderer/entity/layers/LayerRenderer>
// 622 astore 21
// 624 aload 21
// 626 aload 4
// 628 aload 5
// 630 iload 6
// 632 aload_1
// 633 fload 14
// 635 fload 13
// 637 fload_3
// 638 fload 12
// 640 fload 10
// 642 fload 11
// 644 invokevirtual #73 <net/minecraft/client/renderer/entity/layers/LayerRenderer.render>
// 647 goto 602 (-45)
// 650 aload 4
// 652 invokevirtual #74 <com/mojang/blaze3d/matrix/MatrixStack.pop>
// 655 aload_0
// 656 aload_1
// 657 fload_2
// 658 fload_3
// 659 aload 4
// 661 aload 5
// 663 iload 6
// 665 invokespecial #75 <net/minecraft/client/renderer/entity/EntityRenderer.render>
//
// label_1