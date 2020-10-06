package com.unrelentless.fallfest116.util;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.Biome;

public enum LeafType implements StringIdentifiable {
    ACACIA, BIRCH, DARK_OAK, JUNGLE, OAK, SPRUCE;

    @Override
    public String asString() {
        switch (this) {
            case ACACIA:
                return "acacia";
            case BIRCH:
                return "birch";
            case DARK_OAK:
                return "dark_oak";
            case JUNGLE:
                return "jungle";
            case OAK:
                return "oak";
            case SPRUCE:
                return "spruce";
            default:
                return "oak";
        }
    }

    public static LeafType typeForBiome(Biome.Category biome) {
        switch (biome) {
            case DESERT:
            case MESA:
            case SAVANNA:
                return ACACIA;
            case RIVER:
            case BEACH:
            case PLAINS:
                return BIRCH;
            case SWAMP:
            case MUSHROOM:
                return DARK_OAK;
            case JUNGLE:
            case OCEAN:
                return JUNGLE;
            case EXTREME_HILLS:
            case ICY:
            case TAIGA:
                return SPRUCE;
            case THEEND:
            case NETHER:
            case NONE:
            case FOREST:
            default:
                return OAK;
        }
    }
}
