function initializeCoreMod() {
    return {
        'LivingEntity#getEyeHeight': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.LivingEntity',
                'methodName': 'func_213316_a',
                'methodDesc': '(Lnet/minecraft/entity/Pose;Lnet/minecraft/entity/EntitySize;)F'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching LivingEntity#getEyeHeight');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

                var aload0 = new VarInsnNode(Opcodes.ALOAD, 0)
                var insn = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    ASMAPI.buildMethodCall(
                        'com/gpiay/cpm/hook/LivingEntityHook', 'getEntityEyeHeight',
                        '(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/Pose;Lnet/minecraft/entity/EntitySize;)F',
                        ASMAPI.MethodType.STATIC));

                var areturn = ASMAPI.findFirstInstruction(method, Opcodes.FRETURN)

                var instructions = method.instructions
                instructions.insertBefore(areturn, insn)
                instructions.insert(aload0)

                ASMAPI.log('INFO', '[CPMCore]: Patched LivingEntity#getEyeHeight');
                return method;
            }
        }
    }
}