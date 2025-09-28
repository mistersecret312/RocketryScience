package net.mistersecret312.util;

import net.minecraft.util.StringRepresentable;

public enum RocketState implements StringRepresentable
{
    IDLE,
    TAKEOFF,
    COASTING,
    STAGING,
    LANDING,
    ORBIT;

    @Override
    public String getSerializedName()
    {
        return this.name();
    }
}
