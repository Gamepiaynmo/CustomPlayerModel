function initializeCoreMod() {
    return {
        'PlayerRenderer#renderItem': {
            'target': {
                'type': 'METHOD',
                'class': 'net/minecraft/client/renderer/entity/PlayerRenderer',
                'methodName': 'func_229145_a_',
                'methodDesc': '(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/client/renderer/model/ModelRenderer;Lnet/minecraft/client/renderer/model/ModelRenderer;)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching PlayerRenderer#renderItem');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var Label = Java.type('org.objectweb.asm.Label');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                var labelInsn = new LabelNode(new Label());
                var insn = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new VarInsnNode(Opcodes.ILOAD, 3),
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    new VarInsnNode(Opcodes.ALOAD, 5),
                    ASMAPI.buildMethodCall(
                        'com/gpiay/cpm/hook/PlayerRendererHook', 'renderFirstPerson',
                        '(Lnet/minecraft/client/renderer/entity/PlayerRenderer;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/client/renderer/model/ModelRenderer;)Z',
                        ASMAPI.MethodType.STATIC),
                    new JumpInsnNode(Opcodes.IFNE, labelInsn),
                    new InsnNode(Opcodes.RETURN),
                    labelInsn);

                var instructions = method.instructions
                instructions.insert(insn)

                ASMAPI.log('INFO', '[CPMCore]: Patched PlayerRenderer#renderItem');
                return method;
            }
        }
    }
}