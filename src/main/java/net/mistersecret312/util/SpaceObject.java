package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public interface SpaceObject
{
    Component getName();
    Orbit getOrbit();
    void setOrbit(Orbit orbit);

    CompoundTag save(Level level);
    void load(Level level, CompoundTag compoundTag);

}
