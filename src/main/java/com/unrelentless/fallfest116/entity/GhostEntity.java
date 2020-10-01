package com.unrelentless.fallfest116.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.world.World;

public class GhostEntity extends SnowGolemEntity {

    public GhostEntity(EntityType<? extends SnowGolemEntity> entityType, World world) {
        super(entityType, world);
    }
}
