package com.unrelentless.fallfest116.client.render.entity.model;

import com.unrelentless.fallfest116.entity.GhostEntity;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.GhastEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class GhostEntityModel extends GhastEntityModel<GhostEntity> {

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green,
            float blue, float alpha) {

        // translate model down
        matrices.translate(0, -1, 0);
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);

    }
}
