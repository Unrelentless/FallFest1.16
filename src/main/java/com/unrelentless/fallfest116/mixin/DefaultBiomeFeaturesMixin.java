package com.unrelentless.fallfest116.mixin;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;

@Mixin(DefaultBiomeFeatures.class)
public class DefaultBiomeFeaturesMixin {

    private static final EntityType<GhostEntity> GHOST = FallFest116.GHOST;

    @Inject(at = @At("HEAD"), method = "addFarmAnimals(Lnet/minecraft/world/biome/SpawnSettings$Builder;)V")
    private static void injectAddFarmAnimals(SpawnSettings.Builder builder, CallbackInfo info) {
        builder.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(GHOST, 25, 1, 1));
    }
}
