package com.unrelentless.fallfest116.mixin;

import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.state.StateManager;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeavesBlock.class)
public abstract class FallFestLeavesBlockColours extends Block {

    public FallFestLeavesBlockColours(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties(Lnet/minecraft/state/StateManager/Builder;)V")
    private void injectFalledProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
        builder.add(GhostEntity.FALLED);
    }

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/block/AbstractBlock/Settings;)V")
    private void initProperty(AbstractBlock.Settings settings, CallbackInfo info) {
        this.setDefaultState(this.stateManager.getDefaultState().with(GhostEntity.FALLED, false));
    }
}
