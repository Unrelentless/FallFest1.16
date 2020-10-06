package com.unrelentless.fallfest116.mixin;

import java.util.function.BooleanSupplier;

import com.unrelentless.fallfest116.component.EntityComponents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    private PlayerManager playerManager;

    // TERRIBLE IDEA!
    @Inject(at = @At("TAIL"), method = "tick(Ljava/util/function/BooleanSupplier;)V")
    private void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        playerManager.getPlayerList().forEach(player -> {
            int previousValue = EntityComponents.GHOST_COOLDOWN.get(player).getValue();
            EntityComponents.GHOST_COOLDOWN.get(player).setValue(previousValue <= 0 ? 0 : previousValue - 1);
        });
    }
}
