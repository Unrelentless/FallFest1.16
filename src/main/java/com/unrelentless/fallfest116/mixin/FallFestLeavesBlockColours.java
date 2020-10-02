package com.unrelentless.fallfest116.mixin;

import java.util.Random;

import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(LeavesBlock.class)
public abstract class FallFestLeavesBlockColours extends Block {

    public FallFestLeavesBlockColours(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties(Lnet/minecraft/state/StateManager/Builder;)V")
    private void injectFalledProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
        builder.add(GhostEntity.FALLED);
    }

    @Inject(at = @At("TAIL"), method = "randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
    private void injectFalledPropertyToNeighbours(BlockState state, ServerWorld world, BlockPos pos, Random random,
            CallbackInfo info) {
        System.out.println("Original: " + state + " at position " + pos);

        if (state.get(GhostEntity.FALLED) == true) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Direction[] var6 = AbstractBlock.FACINGS;
            int var7 = var6.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                Direction direction = var6[var8];
                mutable.set(pos, direction);
                BlockState blockState = world.getBlockState(mutable);
                if (blockState.getBlock() instanceof LeavesBlock) {
                    if (blockState.get(GhostEntity.FALLED)) {
                        System.out.println("Next before: " + state + " at position " + pos);

                        BlockState blockState2 = blockState
                                .getStateForNeighborUpdate(direction.getOpposite(), state, world, mutable, pos)
                                .with(GhostEntity.FALLED, true);

                        System.out.println("Next after: " + state + " at position " + pos);

                        world.setBlockState(mutable, blockState2);
                    }
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "hasRandomTicks(Lnet/minecraft/block/BlockState;)Z", cancellable = true)
    private void injectHasRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        boolean returnValue = cir.getReturnValue();
        boolean newValue = returnValue || state.get(GhostEntity.FALLED);
        cir.setReturnValue(newValue);
    }

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/block/AbstractBlock/Settings;)V")
    private void initProperty(AbstractBlock.Settings settings, CallbackInfo info) {
        this.setDefaultState(this.stateManager.getDefaultState().with(GhostEntity.FALLED, false));
    }
}
