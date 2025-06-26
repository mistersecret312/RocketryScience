package net.mistersecret312.blueprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface Blueprint extends INBTSerializable<CompoundTag>
{
    double getMass();
    double getReliability();
    double getIntegrity();
    double getMaxIntegrity();

    void setMass(double mass);
    void setReliability(double reliability);
    void setIntegrity(double integrity);
    void setMaxIntegrity(double maxIntegrity);
}
