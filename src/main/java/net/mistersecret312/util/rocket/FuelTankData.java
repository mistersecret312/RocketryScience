package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.client.renderer.FuelTankRenderer;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.BlockInit;
import net.mistersecret312.init.RocketBlockDataInit;
import net.mistersecret312.util.RocketFuel;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class FuelTankData extends BlockData
{
    public int height;
    public int width;

    public RocketFuel fuel;
    public int capacity;
    public RocketFuelTank tank;

    public FuelTankData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
    }

    public FuelTankData()
    {

    }

    @Override
    public void tick(Level level)
    {

    }

    @Override
    public double getMass()
    {
        double fluidMass = 0;
        for(int tank = 0; tank < this.tank.getTanks(); tank++)
        {
            fluidMass += this.tank.getFluidInTank(tank).getAmount();
        }

        double hullMass = getDryMass();

        return fluidMass+hullMass;
    }

    @Override
    public double getDryMass()
    {
        return this.width*width*height*500;
    }

    @Override
    public BlockDataType<FuelTankData> getType()
    {
        return RocketBlockDataInit.FUEL_TANK.get();
    }

    @Override
    public void initializeData(Stage stage)
    {
        height = this.extraData.getInt("Height");
        width = this.extraData.getInt("Size");
        fuel = RocketFuel.valueOf(this.extraData.getString("fuel_type").toUpperCase());
        capacity = width*width*height;
        tank = new RocketFuelTank(fuel.getPropellants(), capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                extraData.put("TankContent", this.writeToNBT(new CompoundTag()));
            }
        };

        tank.readFromNBT(this.extraData.getCompound("TankContent"));
    }

    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        switch(width)
        {
            case 1:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5);
                break;
            case 2:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+1.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+1.5);
                break;
            case 3:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+2.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+2.5);
                break;
        }

        minY = Math.min(aabb.minY, rocket.position().y+pos.getY());
        maxY = Math.max(aabb.maxY, rocket.position().y+pos.getY()+height);

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getVessel().getLevel();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof FuelTankBlockEntity fuelTank)
            {
                if(fuelTank.isController())
                {
                    CompoundTag extraData = blockEntity.saveWithId();
                    if(!stage.palette.contains(state))
                        stage.palette.add(state);
                    for (int x = pos.getX(); x < pos.getX()+fuelTank.getWidth(); x++)
                        for (int z = pos.getZ(); z < pos.getZ() + fuelTank.getWidth() ; z++)
                            for (int y = pos.getY(); y < pos.getY() + fuelTank.getHeight(); y++)
                            {
                                level.removeBlock(new BlockPos(x, y, z), false);
                            }
                    return new FuelTankData(stage, stage.palette.indexOf(state), pos, extraData);
                }
                else return BlockData.VOID;
            }
            return null;
        };
    }

    @Override
    public void placeInLevel(Level level, BlockPos pos)
    {
        switch(width)
        {
            case 1:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);
                return;
            case 2:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);
                return;
            case 3:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);

        }
    }

    @Override
    public void render(RocketEntity rocket, BlockRenderDispatcher dispatcher, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, BlockPos.MutableBlockPos mutablePos)
    {
        mutablePos.move(pos);
        switch (width)
        {
            case 1:
            {
                FuelTankRenderer.renderSingularWidth(height, rocket.level(), mutablePos, 0f, pose, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(rocket.level(), mutablePos) );
                return;
            }
            case 2:
            {
                FuelTankRenderer.renderDoubleWidth(height, rocket.level(), mutablePos, 0f, pose, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(rocket.level(), mutablePos));
                return;
            }
            case 3:
            {
                FuelTankRenderer.renderTripleWidth(height, rocket.level(), mutablePos, 0f, pose, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(rocket.level(), mutablePos));
                return;
            }
        }
        mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());
    }
}
