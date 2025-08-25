package net.mistersecret312.entities;

import com.simibubi.create.foundation.data.BlockStateGen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.init.EntityInit;

import java.util.List;

public class RocketEntity extends Entity
{

    public RocketEntity(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    public RocketEntity(Level level)
    {
        super(EntityInit.ROCKET.get(), level);
    }


    @Override
    protected void defineSynchedData()
    {

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
