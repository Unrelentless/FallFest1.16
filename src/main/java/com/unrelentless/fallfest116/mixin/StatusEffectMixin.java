package com.unrelentless.fallfest116.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

// This is ridiculous hack to get status effect on the server side
@Mixin(StatusEffect.class)
public interface StatusEffectMixin {

    @Accessor("type")
    public StatusEffectType getStatusEffectType();
}
