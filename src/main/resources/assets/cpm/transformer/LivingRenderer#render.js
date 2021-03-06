function initializeCoreMod() {
    return {
        'LivingRenderer#render': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingRenderer'
            },
            'transformer': function(classNode) {
                var methods = classNode.methods;
                for (var i in methods) {
                    var method = methods[i];
                    if (!method.name.equals('func_225623_a_') && !method.name.equals('render'))
                        continue;
                    if (!method.desc.equals('(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V'))
                        continue;

                    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                    ASMAPI.log('INFO', '[CPMCore]: Patching LivingRenderer#render');

                    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                    var Label = Java.type('org.objectweb.asm.Label');
                    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

                    for (var j = 0; j < method.instructions.size(); j++) {
                        var node = method.instructions.get(j);
                        if (node.getOpcode() === Opcodes.GETSTATIC && node.owner === 'net/optifine/reflect/Reflector') {
                            var hasOptifine = true;
                            ASMAPI.log('INFO', '[CPMCore]: LivingRenderer#render found Optifine');
                            break;
                        }
                    }

                    var beginPos = ASMAPI.findFirstMethodCall(method,
                        ASMAPI.MethodType.VIRTUAL,
                        'net/minecraft/client/renderer/entity/model/EntityModel',
                        'setLivingAnimations', '(Lnet/minecraft/entity/Entity;FFF)V');
                    if (beginPos === null) {
                        beginPos = ASMAPI.findFirstMethodCall(method,
                            ASMAPI.MethodType.VIRTUAL,
                            'net/minecraft/client/renderer/entity/model/EntityModel',
                            'func_212843_a_', '(Lnet/minecraft/entity/Entity;FFF)V');
                    }
                    while (beginPos.getOpcode() !== Opcodes.ALOAD || beginPos.var !== 0)
                        beginPos = beginPos.getPrevious();

                    var middlePos = ASMAPI.findFirstMethodCall(method,
                        ASMAPI.MethodType.VIRTUAL,
                        'net/minecraft/client/renderer/entity/model/EntityModel',
                        'render', '(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V');
                    if (middlePos === null) {
                        middlePos = ASMAPI.findFirstMethodCall(method,
                            ASMAPI.MethodType.VIRTUAL,
                            'net/minecraft/client/renderer/entity/model/EntityModel',
                            'func_225598_a_', '(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V');
                    }

                    var endPos = ASMAPI.findFirstMethodCall(method,
                        ASMAPI.MethodType.SPECIAL,
                        'net/minecraft/client/renderer/entity/EntityRenderer',
                        'render', '(Lnet/minecraft/entity/Entity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V');
                    if (endPos === null) {
                        endPos = ASMAPI.findFirstMethodCall(method,
                            ASMAPI.MethodType.SPECIAL,
                            'net/minecraft/client/renderer/entity/EntityRenderer',
                            'func_225623_a_', '(Lnet/minecraft/entity/Entity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V');
                    }

                    var middleInsn = new LabelNode(new Label());
                    var endInsn = new LabelNode(new Label());

                    var offset = hasOptifine ? 1 : 0;
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
                        new VarInsnNode(Opcodes.FLOAD, 14 - offset),
                        new VarInsnNode(Opcodes.FLOAD, 13 - offset),
                        new VarInsnNode(Opcodes.FLOAD, 3),
                        new VarInsnNode(Opcodes.FLOAD, 12 - offset),
                        new VarInsnNode(Opcodes.FLOAD, 10 - offset),
                        new VarInsnNode(Opcodes.FLOAD, 11 - offset),
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

                    methods[i] = method;
                    break;
                }

                return classNode;
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

/**
 * Util function to print a list of instructions for debugging
 *
 * @param {InsnList} instructions The list of instructions to print
 */
function printInstructions(instructions) {
    var arrayLength = instructions.size();
    var labelNames = {
        length: 0
    };
    for (var i = 0; i < arrayLength; ++i) {
        var text = getInstructionText(instructions.get(i), labelNames);
        if (text.length > 0) // Some instructions are ignored
            print(text);
    }
}

/**
 * Util function to get the text for an instruction
 *
 * @param {AbstractInsnNode} instruction The instruction to generate text for
 * @param {Map<int, string>} labelNames The names of the labels in the format Map<LabelHashCode, LabelName>
 */
function getInstructionText(instruction, labelNames) {
    var out = "";
    if (instruction.getType() != 8) // LABEL
        out += " "; // Nice formatting
    if (instruction.getOpcode() > 0) // Labels, Frames and LineNumbers don't have opcodes
        out += OPCODES[instruction.getOpcode()] + " ";
    switch (instruction.getType()) {
        default:
        case 0: // INSN
            break;
        case 1: // INT_INSN
            out += instruction.operand;
            break;
        case 2: // VAR_INSN
            out += instruction.var;
            break;
        case 3: // TYPE_INSN
            out += instruction.desc;
            break;
        case 4: // FIELD_INSN
            out += instruction.owner + "." + instruction.name + " " + instruction.desc;
            break;
        case 5: // METHOD_INSN
            out += instruction.owner + "." + instruction.name + " " + instruction.desc + " (" + instruction.itf + ")";
            break;
        case 6: // INVOKE_DYNAMIC_INSN
            out += instruction.name + " " + instruction.desc;
            break;
        case 7: // JUMP_INSN
            out += getLabelName(instruction.label, labelNames);
            break;
        case 8: // LABEL
            out += getLabelName(instruction.getLabel(), labelNames);
            break;
        case 9: // LDC_INSN
            out += instruction.cst;
            break;
        case 10: // IINC_INSN
            out += instruction.var + " " + instruction.incr;
            break;
        case 11: // TABLESWITCH_INSN
            out += instruction.min + " " + instruction.max;
            out += "\n";
            for (var i = 0; i < instruction.labels.length; ++i) {
                out += "   " + (instruction.min + i) + ": ";
                out += getLabelName(instruction.labels[i], labelNames);
                out += "\n";
            }
            out += "   " + "default: " + getLabelName(instruction.dflt, labelNames);
            break;
        case 12: // LOOKUPSWITCH_INSN
            for (var i = 0; i < instruction.labels.length; ++i) {
                out += "   " + instruction.keys[i] + ": ";
                out += getLabelName(instruction.labels[i], labelNames);
                out += "\n";
            }
            out += "   " + "default: " + getLabelName(instruction.dflt, labelNames);
            break;
        case 13: // MULTIANEWARRAY_INSN
            out += instruction.desc + " " + instruction.dims;
            break;
        case 14: // FRAME
            out += "FRAME";
            // Frames don't work because Nashhorn calls AbstractInsnNode#getType()
            // instead of accessing FrameNode#type for the code "instruction.type"
            // so there is no way to get the frame type of the FrameNode
            break;
        case 15: // LINENUMBER
            out += "LINENUMBER ";
            out += instruction.line + " " + getLabelName(instruction.start.getLabel(), labelNames);
            break;
    }
    return out;
}

/**
 * Util function to get the name for a LabelNode "instruction"
 *
 * @param {LabelNode} label The label to generate a name for
 * @param {Map<int, string>} labelNames The names of other labels in the format Map<LabelHashCode, LabelName>
 */
function getLabelName(label, labelNames) {
    var labelHashCode = label.hashCode();
    var labelName = labelNames[labelHashCode];
    if (labelName === undefined) {
        labelName = "L" + labelNames.length;
        labelNames[labelHashCode] = labelName;
        ++labelNames.length;
    }
    return labelName;
}

/** The names of the Java Virtual Machine opcodes. */
OPCODES = [
    "NOP", // 0 (0x0)
    "ACONST_NULL", // 1 (0x1)
    "ICONST_M1", // 2 (0x2)
    "ICONST_0", // 3 (0x3)
    "ICONST_1", // 4 (0x4)
    "ICONST_2", // 5 (0x5)
    "ICONST_3", // 6 (0x6)
    "ICONST_4", // 7 (0x7)
    "ICONST_5", // 8 (0x8)
    "LCONST_0", // 9 (0x9)
    "LCONST_1", // 10 (0xa)
    "FCONST_0", // 11 (0xb)
    "FCONST_1", // 12 (0xc)
    "FCONST_2", // 13 (0xd)
    "DCONST_0", // 14 (0xe)
    "DCONST_1", // 15 (0xf)
    "BIPUSH", // 16 (0x10)
    "SIPUSH", // 17 (0x11)
    "LDC", // 18 (0x12)
    "LDC_W", // 19 (0x13)
    "LDC2_W", // 20 (0x14)
    "ILOAD", // 21 (0x15)
    "LLOAD", // 22 (0x16)
    "FLOAD", // 23 (0x17)
    "DLOAD", // 24 (0x18)
    "ALOAD", // 25 (0x19)
    "ILOAD_0", // 26 (0x1a)
    "ILOAD_1", // 27 (0x1b)
    "ILOAD_2", // 28 (0x1c)
    "ILOAD_3", // 29 (0x1d)
    "LLOAD_0", // 30 (0x1e)
    "LLOAD_1", // 31 (0x1f)
    "LLOAD_2", // 32 (0x20)
    "LLOAD_3", // 33 (0x21)
    "FLOAD_0", // 34 (0x22)
    "FLOAD_1", // 35 (0x23)
    "FLOAD_2", // 36 (0x24)
    "FLOAD_3", // 37 (0x25)
    "DLOAD_0", // 38 (0x26)
    "DLOAD_1", // 39 (0x27)
    "DLOAD_2", // 40 (0x28)
    "DLOAD_3", // 41 (0x29)
    "ALOAD_0", // 42 (0x2a)
    "ALOAD_1", // 43 (0x2b)
    "ALOAD_2", // 44 (0x2c)
    "ALOAD_3", // 45 (0x2d)
    "IALOAD", // 46 (0x2e)
    "LALOAD", // 47 (0x2f)
    "FALOAD", // 48 (0x30)
    "DALOAD", // 49 (0x31)
    "AALOAD", // 50 (0x32)
    "BALOAD", // 51 (0x33)
    "CALOAD", // 52 (0x34)
    "SALOAD", // 53 (0x35)
    "ISTORE", // 54 (0x36)
    "LSTORE", // 55 (0x37)
    "FSTORE", // 56 (0x38)
    "DSTORE", // 57 (0x39)
    "ASTORE", // 58 (0x3a)
    "ISTORE_0", // 59 (0x3b)
    "ISTORE_1", // 60 (0x3c)
    "ISTORE_2", // 61 (0x3d)
    "ISTORE_3", // 62 (0x3e)
    "LSTORE_0", // 63 (0x3f)
    "LSTORE_1", // 64 (0x40)
    "LSTORE_2", // 65 (0x41)
    "LSTORE_3", // 66 (0x42)
    "FSTORE_0", // 67 (0x43)
    "FSTORE_1", // 68 (0x44)
    "FSTORE_2", // 69 (0x45)
    "FSTORE_3", // 70 (0x46)
    "DSTORE_0", // 71 (0x47)
    "DSTORE_1", // 72 (0x48)
    "DSTORE_2", // 73 (0x49)
    "DSTORE_3", // 74 (0x4a)
    "ASTORE_0", // 75 (0x4b)
    "ASTORE_1", // 76 (0x4c)
    "ASTORE_2", // 77 (0x4d)
    "ASTORE_3", // 78 (0x4e)
    "IASTORE", // 79 (0x4f)
    "LASTORE", // 80 (0x50)
    "FASTORE", // 81 (0x51)
    "DASTORE", // 82 (0x52)
    "AASTORE", // 83 (0x53)
    "BASTORE", // 84 (0x54)
    "CASTORE", // 85 (0x55)
    "SASTORE", // 86 (0x56)
    "POP", // 87 (0x57)
    "POP2", // 88 (0x58)
    "DUP", // 89 (0x59)
    "DUP_X1", // 90 (0x5a)
    "DUP_X2", // 91 (0x5b)
    "DUP2", // 92 (0x5c)
    "DUP2_X1", // 93 (0x5d)
    "DUP2_X2", // 94 (0x5e)
    "SWAP", // 95 (0x5f)
    "IADD", // 96 (0x60)
    "LADD", // 97 (0x61)
    "FADD", // 98 (0x62)
    "DADD", // 99 (0x63)
    "ISUB", // 100 (0x64)
    "LSUB", // 101 (0x65)
    "FSUB", // 102 (0x66)
    "DSUB", // 103 (0x67)
    "IMUL", // 104 (0x68)
    "LMUL", // 105 (0x69)
    "FMUL", // 106 (0x6a)
    "DMUL", // 107 (0x6b)
    "IDIV", // 108 (0x6c)
    "LDIV", // 109 (0x6d)
    "FDIV", // 110 (0x6e)
    "DDIV", // 111 (0x6f)
    "IREM", // 112 (0x70)
    "LREM", // 113 (0x71)
    "FREM", // 114 (0x72)
    "DREM", // 115 (0x73)
    "INEG", // 116 (0x74)
    "LNEG", // 117 (0x75)
    "FNEG", // 118 (0x76)
    "DNEG", // 119 (0x77)
    "ISHL", // 120 (0x78)
    "LSHL", // 121 (0x79)
    "ISHR", // 122 (0x7a)
    "LSHR", // 123 (0x7b)
    "IUSHR", // 124 (0x7c)
    "LUSHR", // 125 (0x7d)
    "IAND", // 126 (0x7e)
    "LAND", // 127 (0x7f)
    "IOR", // 128 (0x80)
    "LOR", // 129 (0x81)
    "IXOR", // 130 (0x82)
    "LXOR", // 131 (0x83)
    "IINC", // 132 (0x84)
    "I2L", // 133 (0x85)
    "I2F", // 134 (0x86)
    "I2D", // 135 (0x87)
    "L2I", // 136 (0x88)
    "L2F", // 137 (0x89)
    "L2D", // 138 (0x8a)
    "F2I", // 139 (0x8b)
    "F2L", // 140 (0x8c)
    "F2D", // 141 (0x8d)
    "D2I", // 142 (0x8e)
    "D2L", // 143 (0x8f)
    "D2F", // 144 (0x90)
    "I2B", // 145 (0x91)
    "I2C", // 146 (0x92)
    "I2S", // 147 (0x93)
    "LCMP", // 148 (0x94)
    "FCMPL", // 149 (0x95)
    "FCMPG", // 150 (0x96)
    "DCMPL", // 151 (0x97)
    "DCMPG", // 152 (0x98)
    "IFEQ", // 153 (0x99)
    "IFNE", // 154 (0x9a)
    "IFLT", // 155 (0x9b)
    "IFGE", // 156 (0x9c)
    "IFGT", // 157 (0x9d)
    "IFLE", // 158 (0x9e)
    "IF_ICMPEQ", // 159 (0x9f)
    "IF_ICMPNE", // 160 (0xa0)
    "IF_ICMPLT", // 161 (0xa1)
    "IF_ICMPGE", // 162 (0xa2)
    "IF_ICMPGT", // 163 (0xa3)
    "IF_ICMPLE", // 164 (0xa4)
    "IF_ACMPEQ", // 165 (0xa5)
    "IF_ACMPNE", // 166 (0xa6)
    "GOTO", // 167 (0xa7)
    "JSR", // 168 (0xa8)
    "RET", // 169 (0xa9)
    "TABLESWITCH", // 170 (0xaa)
    "LOOKUPSWITCH", // 171 (0xab)
    "IRETURN", // 172 (0xac)
    "LRETURN", // 173 (0xad)
    "FRETURN", // 174 (0xae)
    "DRETURN", // 175 (0xaf)
    "ARETURN", // 176 (0xb0)
    "RETURN", // 177 (0xb1)
    "GETSTATIC", // 178 (0xb2)
    "PUTSTATIC", // 179 (0xb3)
    "GETFIELD", // 180 (0xb4)
    "PUTFIELD", // 181 (0xb5)
    "INVOKEVIRTUAL", // 182 (0xb6)
    "INVOKESPECIAL", // 183 (0xb7)
    "INVOKESTATIC", // 184 (0xb8)
    "INVOKEINTERFACE", // 185 (0xb9)
    "INVOKEDYNAMIC", // 186 (0xba)
    "NEW", // 187 (0xbb)
    "NEWARRAY", // 188 (0xbc)
    "ANEWARRAY", // 189 (0xbd)
    "ARRAYLENGTH", // 190 (0xbe)
    "ATHROW", // 191 (0xbf)
    "CHECKCAST", // 192 (0xc0)
    "INSTANCEOF", // 193 (0xc1)
    "MONITORENTER", // 194 (0xc2)
    "MONITOREXIT", // 195 (0xc3)
    "WIDE", // 196 (0xc4)
    "MULTIANEWARRAY", // 197 (0xc5)
    "IFNULL", // 198 (0xc6)
    "IFNONNULL" // 199 (0xc7)
];