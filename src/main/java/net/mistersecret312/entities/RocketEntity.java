package net.mistersecret312.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.init.BlockInit;
import net.mistersecret312.init.EntityDataSerializersInit;
import net.mistersecret312.init.EntityInit;
import net.mistersecret312.util.rocket.BlockData;
import net.mistersecret312.util.rocket.FuelTankData;
import net.mistersecret312.util.rocket.Rocket;
import net.mistersecret312.util.rocket.Stage;

import java.util.*;

public class RocketEntity extends Entity
{
    private static final String ROCKET_DATA = "rocket_data";
    private static final EntityDataAccessor<Rocket> ROCKET =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializersInit.ROCKET.get());

    public RocketEntity(EntityType<?> type, Level level)
    {
        super(type, level);
    }

    public RocketEntity(Level level)
    {
        super(EntityInit.ROCKET.get(), level);
    }

    @Override
    public void tick()
    {
        super.tick();
        if(level().getGameTime() % 5 == 0)
        {
            Rocket rocket = new Rocket(this, new LinkedHashSet<>());
            Stage stage = new Stage(rocket);

            LinkedHashSet<Stage> stages = new LinkedHashSet<>();
            List<BlockState> palette = List.of(Blocks.STONE.defaultBlockState(), BlockInit.FUEL_TANK.get().defaultBlockState());
            HashMap<BlockPos, BlockData> blocks = new HashMap<>();
            blocks.put(BlockPos.ZERO, new BlockData(stage, 0, BlockPos.ZERO, new CompoundTag()));
            CompoundTag tank1 = new CompoundTag();
            tank1.putInt("Height", 3);
            tank1.putInt("Size", 1);
            blocks.put(new BlockPos(0,1,0), new FuelTankData(stage, 1, new BlockPos(0, 1, 0), tank1));

            stage.palette = palette;
            stage.blocks = blocks;
            stage.maxSolidFuel = 0;
            stage.solidFuel = 0;
            stage.maxFluids = new ArrayList<>();
            stage.fluidStacks = new ArrayList<>();
            stages.add(stage);
            rocket.stages = stages;

            this.setRocket(rocket);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        Rocket rocket = new Rocket(this, new LinkedHashSet<>());
        rocket.load(tag.getCompound(ROCKET_DATA));
        this.entityData.set(ROCKET, rocket);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        tag.put(ROCKET_DATA, this.entityData.get(ROCKET).save());
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(ROCKET, new Rocket(this, new LinkedHashSet<>()));
    }

    @Override
    protected AABB makeBoundingBox()
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (Stage stage : this.getRocket().stages)
        {
            for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
            {
                if(entry.getValue() instanceof FuelTankData tank)
                {
                    switch(tank.width)
                    {
                        case 1:
                            minX = Math.min(minX, entry.getKey().getX()-1);
                            minZ = Math.min(minZ, entry.getKey().getZ()-1);
                            break;
                        case 2:
                            minX = Math.min(minX, entry.getKey().getX()-1);
                            minZ = Math.min(minZ, entry.getKey().getZ()-1);
                            maxX = Math.max(maxX, entry.getKey().getX()+1);
                            maxZ = Math.max(maxX, entry.getKey().getZ()+1);
                            break;
                        case 3:
                            minX = Math.min(minX, entry.getKey().getX()-1);
                            minZ = Math.min(minZ, entry.getKey().getZ()-1);
                            maxX = Math.max(maxX, entry.getKey().getX()+2);
                            maxZ = Math.max(maxX, entry.getKey().getZ()+2);
                            break;
                    }

                    minY = Math.min(minY, entry.getKey().getY())*tank.height;
                    maxY = Math.max(maxY, entry.getKey().getY())*tank.height;

                    continue;
                }

                minX = Math.min(minX, entry.getKey().getX());
                minY = Math.min(minY, entry.getKey().getY());
                minZ = Math.min(minZ, entry.getKey().getZ());
                maxX = Math.max(maxX, entry.getKey().getX());
                maxY = Math.max(maxY, entry.getKey().getY());
                maxZ = Math.max(maxZ, entry.getKey().getZ());
            }
        }


        return new AABB(this.position().x+minX, this.position().y+minY, this.position().z+minZ,
                this.position().x+maxX, this.position().y+maxY+1, this.position().z+maxZ).move(0.5d, 0d, 0.5d);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if(player.level().isClientSide())
            return InteractionResult.SUCCESS;

        for (Stage stage : this.getRocket().stages)
        {
            for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
            {
                entry.getValue().placeInLevel(player.level(), entry.getKey().offset(this.getOnPos().above()));
            }
        }
        this.discard();

        return InteractionResult.PASS;
    }

    public Rocket getRocket()
    {
        return this.entityData.get(ROCKET);
    }

    public void setRocket(Rocket rocket)
    {
        this.entityData.set(ROCKET, rocket);
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
}
