package com.unrelentless.fallfest116;

import com.unrelentless.fallfest116.block.FallenGrassBlock;
import com.unrelentless.fallfest116.entity.GhostEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
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
			FabricEntityTypeBuilder.<GhostEntity>create(SpawnGroup.MISC, GhostEntity::new).trackRangeBlocks(8)
					.dimensions(EntityDimensions.fixed(0.7F, 1.9F)).build());

	public static final FallenGrassBlock FALLEN_GRASS_BLOCK = new FallenGrassBlock(
			FabricBlockSettings.of(Material.LEAVES).strength(0.2F).sounds(BlockSoundGroup.GRASS).nonOpaque());

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "fallen_leaves"), FALLEN_GRASS_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MODID, "fallen_leaves"),
				new BlockItem(FALLEN_GRASS_BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

		FabricDefaultAttributeRegistry.register(GHOST, GhostEntity.createGhostAttributes());

		// .ticksRandomly()
		// .allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	}
}
