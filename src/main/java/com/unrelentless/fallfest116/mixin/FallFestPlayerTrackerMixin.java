package com.unrelentless.fallfest116.mixin;

import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

@Mixin(PlayerEntity.class)
public class FallFestPlayerTrackerMixin {

    public int ghostCooldown = 600;

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        // tag.putFloat(GhostEntity.GHOST_COOLDOWN_KEY, this.ghostCooldown);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        // this.ghostCooldown = tag.getInt(GhostEntity.GHOST_COOLDOWN_KEY);
    }
}
