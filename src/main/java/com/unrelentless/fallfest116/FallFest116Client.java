package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.entity.GhostEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class FallFest116Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(FallFest116.GHOST, (dispatcher, context) -> {
            return new GhostEntityRenderer(dispatcher);
        });
    }
}