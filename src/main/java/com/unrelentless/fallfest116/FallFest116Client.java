package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.entity.GhostEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.render.RenderLayer;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class FallFest116Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(FallFest116.GHOST, (dispatcher, context) -> {
            return new GhostEntityRenderer(dispatcher);
        });

        BlockRenderLayerMap.INSTANCE.putBlock(FallFest116.FALLEN_GRASS_BLOCK, RenderLayer.getCutoutMipped());

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            int colour = world != null && pos != null ? BiomeColors.getFoliageColor(world, pos)
                    : FoliageColors.getDefaultColor();
            int shiftedColour = 0xFF0000 | colour;
            return shiftedColour;
        }, FallFest116.FALLEN_GRASS_BLOCK);
    }
}