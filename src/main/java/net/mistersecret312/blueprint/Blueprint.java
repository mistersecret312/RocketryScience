package net.mistersecret312.blueprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface Blueprint extends INBTSerializable<CompoundTag>
{
    double getMass();

    void setMass(double mass);
}
