package com.unrelentless.fallfest116.util;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;

public enum FallenColour implements StringIdentifiable {
    ORANGE, YELLOW, RED;

    public static final FallenColour[] COLOURS = { FallenColour.ORANGE, FallenColour.YELLOW, FallenColour.RED };
    public static final EnumProperty<FallenColour> COLOUR = EnumProperty.of("colour", FallenColour.class);

    public int colourForFoliageColour(int foliageColour) {
        switch (this) {
            case ORANGE:
                return 0xe07408;
            case YELLOW:
                return 0xf1b109;
            case RED:
                return 0x9c1e14;
            default:
                return foliageColour;
        }
    }

    @Override
    public String asString() {
        switch (this) {
            case ORANGE:
                return "orange";
            case RED:
                return "red";
            case YELLOW:
                return "yellow";
            default:
                return "green";
        }
    }
}