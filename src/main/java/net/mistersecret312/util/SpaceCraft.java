package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class SpaceCraft implements SpaceObject
{

    @Override
    public Component getName()
    {
        return Component.translatable("spacecraftA");
    }

    @Override
    public Orbit getOrbit()
    {
        return null;
    }

    @Override
    public CompoundTag save(Level level)
    {
        return null;
    }

    @Override
    public void load(Level level, CompoundTag compoundTag)
    {

    }
}
