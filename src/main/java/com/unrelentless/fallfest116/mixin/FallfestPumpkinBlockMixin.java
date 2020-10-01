package com.unrelentless.fallfest116.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.entity.GhostEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CarvedPumpkinBlock.class)
public class FallfestPumpkinBlockMixin {

    private BlockPattern ghostPattern;

    @Inject(at = @At("TAIL"), method = "trySpawnEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
    private void injectGhostPattern(World world, BlockPos pos, CallbackInfo info) {
        BlockPattern.Result result = this.getGhostPattern().searchAround(world, pos);

        if (result != null) {
            for (int k = 0; k < this.getGhostPattern().getHeight(); ++k) {
                CachedBlockPosition cachedBlockPosition = result.translate(0, k, 0);
                world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(),
                        Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
            }

            GhostEntity ghostEnity = (GhostEntity) FallFest116.GHOST.create(world);
            BlockPos blockPos = result.translate(0, 2, 0).getBlockPos();
            ghostEnity.refreshPositionAndAngles((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.05D,
                    (double) blockPos.getZ() + 0.5D, 0.0F, 0.0F);
            world.spawnEntity(ghostEnity);
            Iterator var6 = world
                    .getNonSpectatingEntities(ServerPlayerEntity.class, ghostEnity.getBoundingBox().expand(5.0D))
                    .iterator();

            while (var6.hasNext()) {
                ServerPlayerEntity serverPlayerEntity2 = (ServerPlayerEntity) var6.next();
                Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity2, ghostEnity);
            }

            for (int m = 0; m < this.getGhostPattern().getHeight(); ++m) {
                CachedBlockPosition cachedBlockPosition2 = result.translate(0, m, 0);
                world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.AIR);
            }
        }
    }

    private BlockPattern getGhostPattern() {
        if (this.ghostPattern == null) {
            this.ghostPattern = BlockPatternBuilder.start().aisle("^", "#", "#")
                    .where('^',
                            CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.CARVED_PUMPKIN)))
                    .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COBWEB)))
                    .build();
        }

        return this.ghostPattern;
    }
}
