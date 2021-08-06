package com.gpiay.cpm.model.animation;

import net.minecraft.potion.EffectInstance;

public class ScriptEffect {
    public static final ScriptEffect empty = new ScriptEffect(null);

    private final EffectInstance effect;

    public ScriptEffect(EffectInstance effect) {
        this.effect = effect;
    }

    public boolean isEmpty() { return effect == null; }
    public int getAmplifier() { return effect == null ? -1 : effect.getAmplifier(); }
    public int getDuration() { return effect == null ? 0 : effect.getDuration(); }
}
