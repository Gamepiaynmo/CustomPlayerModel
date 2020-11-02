function initializeCoreMod() {
    return {
        'LivingRenderer#doRender': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.LivingRenderer',
                'methodName': 'func_76986_a',
                'methodDesc': '(Lnet/minecraft/entity/LivingEntity;DDDFF)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log('INFO', '[CPMCore]: Patching LivingRenderer#doRender');

                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var Label = Java.type('org.objectweb.asm.Label');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');

                var beginPos = ASMAPI.findFirstMethodCall(method,
                    ASMAPI.MethodType.STATIC,
                    'com/mojang/blaze3d/platform/GlStateManager',
                    'enableAlphaTest', '()V');
                var endPos = ASMAPI.findFirstMethodCall(method,
                    ASMAPI.MethodType.STATIC,
                    'com/mojang/blaze3d/platform/GlStateManager',
                    'disableRescaleNormal', '()V');

                var beginInsn = ASMAPI.buildMethodCall(
                    'com/gpiay/cpm/hook/LivingRendererHook',
                    'onBeginRenderModel', '()Z',
                    ASMAPI.MethodType.STATIC);
                var labelInsn = new LabelNode(new Label());
                var jumpInsn = new JumpInsnNode(Opcodes.IFNE, labelInsn)
                
                var instructions = method.instructions
                instructions.insertBefore(beginPos, beginInsn)
                instructions.insertBefore(beginPos, jumpInsn)
                instructions.insertBefore(endPos, labelInsn)

                ASMAPI.log('INFO', '[CPMCore]: Patched LivingRenderer#doRender');
                return method;
            }
        }
    }
}