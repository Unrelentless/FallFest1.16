package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.client.render.entity.GhostEntityRenderer;
import com.unrelentless.fallfest116.entity.GhostEntity;
import com.unrelentless.fallfest116.util.FallenColour;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class FallFest116Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Entity Renderer
        EntityRendererRegistry.INSTANCE.register(FallFest116.GHOST, (dispatcher, context) -> {
            return new GhostEntityRenderer(dispatcher);
        });

        // Block Renderer
        BlockRenderLayerMap.INSTANCE.putBlock(FallFest116.FALLEN_LEAVES_BLOCK, RenderLayer.getCutoutMipped());

        // Color
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            int colour = world != null && pos != null ? BiomeColors.getFoliageColor(world, pos)
                    : FoliageColors.getDefaultColor();
            int shiftedColour = state.get(FallenColour.COLOUR).colourForFoliageColour(colour);
            return shiftedColour;
        }, FallFest116.FALLEN_LEAVES_BLOCK);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return ColorProviderRegistry.BLOCK.get(FallFest116.FALLEN_LEAVES_BLOCK).getColor(
                    FallFest116.FALLEN_LEAVES_BLOCK.getDefaultState(), (BlockRenderView) null, (BlockPos) null,
                    tintIndex);
        }, FallFest116.FALLEN_LEAVES_BLOCK);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (state.get(GhostEntity.FALLED) == false) {
                return world != null && pos != null ? BiomeColors.getFoliageColor(world, pos)
                        : FoliageColors.getDefaultColor();
            }

            int colour = world != null && pos != null ? BiomeColors.getFoliageColor(world, pos)
                    : FoliageColors.getDefaultColor();
            int shiftedColour = state.get(FallenColour.COLOUR).colourForFoliageColour(colour);
            return shiftedColour;
        }, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (state.get(GhostEntity.FALLED) == false) {
                return FoliageColors.getBirchColor();
            }

            int colour = FoliageColors.getBirchColor();
            int shiftedColour = state.get(FallenColour.COLOUR).colourForFoliageColour(colour);
            return shiftedColour;
        }, Blocks.BIRCH_LEAVES);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (state.get(GhostEntity.FALLED) == false) {
                return FoliageColors.getSpruceColor();
            }

            int colour = FoliageColors.getSpruceColor();
            int shiftedColour = state.get(FallenColour.COLOUR).colourForFoliageColour(colour);
            return shiftedColour;
        }, Blocks.SPRUCE_LEAVES);
    }
}