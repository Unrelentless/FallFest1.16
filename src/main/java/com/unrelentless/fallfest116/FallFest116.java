package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.entity.GhostEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FallFest116 implements ModInitializer {

	public static final EntityType<GhostEntity> GHOST = Registry.register(Registry.ENTITY_TYPE,
			new Identifier("unrelentless-fallfest116", "ghost"),
			FabricEntityTypeBuilder.<GhostEntity>create(SpawnGroup.MISC, GhostEntity::new)
					.dimensions(EntityDimensions.fixed(0.7F, 1.9F)).build());

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(GHOST, GhostEntity.createMobAttributes());
	}
}
