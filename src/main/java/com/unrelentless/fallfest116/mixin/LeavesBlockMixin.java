package com.unrelentless.fallfest116.mixin;

import java.util.Random;

import com.unrelentless.fallfest116.entity.GhostEntity;
import com.unrelentless.fallfest116.util.FallenColour;

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
public abstract class LeavesBlockMixin extends Block {

    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V")
    private void injectAppendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
        builder.add(GhostEntity.FALLED, FallenColour.COLOUR);
    }

    @Inject(at = @At("TAIL"), method = "randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
    private void injectRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
        if (state.get(GhostEntity.FALLED) == true) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Direction[] var6 = AbstractBlock.FACINGS;
            int var7 = var6.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                Direction direction = var6[var8];
                mutable.set(pos, direction);
                BlockState blockState = world.getBlockState(mutable);
                if (blockState.getBlock() instanceof LeavesBlock) {
                    if (!blockState.get(GhostEntity.FALLED)) {
                        BlockState blockState2 = blockState
                                .getStateForNeighborUpdate(direction.getOpposite(), state, world, mutable, pos)
                                .with(GhostEntity.FALLED, true)
                                .with(FallenColour.COLOUR, FallenColour.COLOURS[new Random().nextInt(3)]);
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

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V")
    private void injectInit(AbstractBlock.Settings settings, CallbackInfo info) {
        this.setDefaultState(this.getDefaultState().with(GhostEntity.FALLED, false));
    }
}
