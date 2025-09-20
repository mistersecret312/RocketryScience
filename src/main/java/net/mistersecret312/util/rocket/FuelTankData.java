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

import java.util.function.BiFunction;

public class FuelTankData extends BlockData
{
    public int height;
    public int width;

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
    public void initializeData()
    {
        height = this.extraData.getInt("Height");
        width = this.extraData.getInt("Size");
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
        maxY = Math.max(aabb.maxY, rocket.position().y+pos.getY())+height;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getRocket().getRocketEntity().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof FuelTankBlockEntity fuelTank)
            {
                if(fuelTank.isController())
                {
                    CompoundTag extraData = blockEntity.saveWithId();
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
                    super.placeInLevel(level, pos.offset(0, j, 0));
                }
                return;
            case 2:
                for (int j = 0; j < height; j++)
                {
                    super.placeInLevel(level, pos.offset(0, j, 0));
                    super.placeInLevel(level, pos.offset(1, j, 0));
                    super.placeInLevel(level, pos.offset(0, j, 1));
                    super.placeInLevel(level, pos.offset(1, j, 1));
                }
                return;
            case 3:
                for (int j = 0; j < height; j++)
                {
                    super.placeInLevel(level, pos.offset(0, j, 0));
                    super.placeInLevel(level, pos.offset(1, j, 0));
                    super.placeInLevel(level, pos.offset(2, j, 0));
                    super.placeInLevel(level, pos.offset(0, j, 1));
                    super.placeInLevel(level, pos.offset(0, j, 2));
                    super.placeInLevel(level, pos.offset(1, j, 1));
                    super.placeInLevel(level, pos.offset(1, j, 2));
                    super.placeInLevel(level, pos.offset(2, j, 1));
                    super.placeInLevel(level, pos.offset(2, j, 2));
                }
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
