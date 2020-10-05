package com.unrelentless.fallfest116.mixin;

import java.util.function.BooleanSupplier;

import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class FallFestTickMixin {

    @Inject(at = @At("TAIL"), method = "tick(Ljava/util/function/BooleanSupplier;)V")
    private void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        GhostEntity.updatePlayerData();
    }
}
