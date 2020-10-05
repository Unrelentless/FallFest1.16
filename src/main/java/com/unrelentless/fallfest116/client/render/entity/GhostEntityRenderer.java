package com.unrelentless.fallfest116.client.render.entity;

import com.unrelentless.fallfest116.FallFest116;
import com.unrelentless.fallfest116.client.render.entity.model.GhostEntityModel;
import com.unrelentless.fallfest116.entity.GhostEntity;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class GhostEntityRenderer extends MobEntityRenderer<GhostEntity, GhostEntityModel> {

    private static final Identifier TEXTURE = new Identifier(FallFest116.MODID, "textures/entity/ghost/pump1.png");
    private static final Identifier ANGRY_TEXTURE = new Identifier(FallFest116.MODID,
            "textures/entity/ghost/pump2.png");

    public GhostEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new GhostEntityModel(), 0.5F);
    }

    @Override
    public Identifier getTexture(GhostEntity entity) {
        return entity.isShooting() ? ANGRY_TEXTURE : TEXTURE;
    }
}
