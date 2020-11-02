function initializeCoreMod() {
    return {
        'PlayerEntity#getSize': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.PlayerEntity',
                'methodName': 'func_213305_a',
                'methodDesc': '(Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching PlayerEntity#getSize');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

                var aload0 = new VarInsnNode(Opcodes.ALOAD, 0)
                var aload1 = new VarInsnNode(Opcodes.ALOAD, 1)
                var invoke = ASMAPI.buildMethodCall(
                    'com/gpiay/cpm/hook/LivingEntityHook', 'getEntitySize',
                    '(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EntitySize;Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;',
                    ASMAPI.MethodType.STATIC);

                var areturn = ASMAPI.findFirstInstruction(method, Opcodes.ARETURN)

                var instructions = method.instructions
                instructions.insertBefore(areturn, aload1)
                instructions.insertBefore(areturn, invoke)
                instructions.insert(aload0)

                ASMAPI.log('INFO', '[CPMCore]: Patched PlayerEntity#getSize');
                return method;
            }
        }
    }
}