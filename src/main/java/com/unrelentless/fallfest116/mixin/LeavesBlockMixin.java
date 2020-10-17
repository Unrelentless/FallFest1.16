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
        if (state.get(GhostEntity.FALLED)) {
            BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
            Direction[] directions = AbstractBlock.FACINGS;

            for (Direction direction : directions) {
                mutableBlockPos.set(pos, direction);
                BlockState blockState = world.getBlockState(mutableBlockPos);

                boolean isNormalLeafBlock = blockState.getBlock() instanceof LeavesBlock
                        && !blockState.get(GhostEntity.FALLED);
                if (isNormalLeafBlock) {
                    BlockState updatedBlockState = blockState
                            .getStateForNeighborUpdate(direction.getOpposite(), state, world, mutableBlockPos, pos)
                            .with(GhostEntity.FALLED, true)
                            .with(FallenColour.COLOUR, FallenColour.COLOURS[new Random().nextInt(3)]);
                    world.setBlockState(mutableBlockPos, updatedBlockState);
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
