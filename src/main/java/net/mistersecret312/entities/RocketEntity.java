package net.mistersecret312.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.init.EntityInit;
import net.mistersecret312.util.rocket.Rocket;
import net.mistersecret312.util.rocket.RocketDataSerializers;

import java.util.LinkedHashSet;

public class RocketEntity extends Entity
{
    private static final String ROCKET_DATA = "rocket_data";
    private static final EntityDataAccessor<Rocket> ROCKET =
            SynchedEntityData.defineId(RocketEntity.class, RocketDataSerializers.ROCKET);

    public RocketEntity(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    public RocketEntity(Level level)
    {
        super(EntityInit.ROCKET.get(), level);
    }

    @Override
    public boolean save(CompoundTag tag)
    {
        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(ROCKET, new Rocket(this, new LinkedHashSet<>()));
    }

    @Override
    protected AABB makeBoundingBox()
    {
        return new AABB(this.position().add(0f, 0.5f, 0f), this.position().add(0, 1.5f, 0)).inflate(0.5f);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean isPushable()
    {
        return true;
    }

    @Override
    public boolean isNoGravity()
    {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound)
    {

    }
}
