package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.block.FallenLeavesBlock;
import com.unrelentless.fallfest116.component.EntityComponents;
import com.unrelentless.fallfest116.component.GhostCooldownIntComponent;
import com.unrelentless.fallfest116.entity.GhostEntity;

import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FallFest116 implements ModInitializer {

	public static final String MODID = "unrelentless-fallfest116";

	public static final EntityType<GhostEntity> GHOST = Registry.register(Registry.ENTITY_TYPE,
			new Identifier(MODID, "ghost"),
			FabricEntityTypeBuilder.<GhostEntity>create(SpawnGroup.CREATURE, GhostEntity::new).trackRangeBlocks(8)
					.dimensions(EntityDimensions.fixed(0.7F, 1.9F)).build());

	public static final FallenLeavesBlock FALLEN_LEAVES_BLOCK = new FallenLeavesBlock(
			FabricBlockSettings.of(Material.LEAVES).strength(0.2F).sounds(BlockSoundGroup.GRASS).nonOpaque()
					.blockVision((state, world, pos) -> false));

	@Override
	public void onInitialize() {

		// Blocks
		Registry.register(Registry.BLOCK, new Identifier(MODID, "fallen_leaves"), FALLEN_LEAVES_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "fallen_leaves"),
				new BlockItem(FALLEN_LEAVES_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

		// Entities
		FabricDefaultAttributeRegistry.register(GHOST, GhostEntity.createGhostAttributes());

		// Cardinal Components
		EntityComponentCallback.event(PlayerEntity.class).register((provider, components) -> {
			components.put(EntityComponents.GHOST_COOLDOWN, new GhostCooldownIntComponent());
		});
	}
}
