package com.unrelentless.fallfest116.component;

import net.minecraft.nbt.CompoundTag;

public class GhostCooldownIntComponent implements IntComponent {
    public static final int GHOST_COOLDOWN_VALUE = 3600;

    public int value = 0;

    @Override
    public void fromTag(CompoundTag tag) {
        this.value = tag.getInt("value");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("value", this.value);
        return tag;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public void setValue(int newValue) {
        this.value = newValue;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        fromTag(tag);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        toTag(tag);
    }

    @Override
    public void resetValue() {
        this.value = GHOST_COOLDOWN_VALUE;
    }
}
