function initializeCoreMod() {
    return {
        'PlayerRenderer#renderLeftArm': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.PlayerRenderer',
                'methodName': 'func_177139_c',
                'methodDesc': '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching PlayerRenderer#renderLeftArm');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var Label = Java.type('org.objectweb.asm.Label');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

                var aload1 = new VarInsnNode(Opcodes.ALOAD, 1)
                var invoke = ASMAPI.buildMethodCall(
                    'com/gpiay/cpm/hook/PlayerRendererHook', 'renderLeftArm',
                    '(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)Z',
                    ASMAPI.MethodType.STATIC);
                var labelInsn = new LabelNode(new Label());
                var jumpInsn = new JumpInsnNode(Opcodes.IFNE, labelInsn)
                var returnInsn = new InsnNode(Opcodes.RETURN)

                var instructions = method.instructions
                instructions.insert(labelInsn)
                instructions.insert(returnInsn)
                instructions.insert(jumpInsn)
                instructions.insert(invoke)
                instructions.insert(aload1)

                ASMAPI.log('INFO', '[CPMCore]: Patched PlayerRenderer#renderLeftArm');
                return method;
            }
        }
    }
}