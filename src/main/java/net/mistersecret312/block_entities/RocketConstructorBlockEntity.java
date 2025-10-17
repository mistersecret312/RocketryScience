package net.mistersecret312.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.blocks.SeparatorBlock;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.BlockEntityInit;
import net.mistersecret312.init.RocketBlockDataInit;
import net.mistersecret312.util.OrbitalMath;
import net.mistersecret312.util.rocket.BlockData;
import net.mistersecret312.util.rocket.Rocket;
import net.mistersecret312.util.rocket.Stage;
import net.mistersecret312.util.trajectories.EllipticalPath;
import net.mistersecret312.util.trajectories.OrbitalPath;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class RocketConstructorBlockEntity extends BlockEntity implements IRocketPadConnective
{
    public UUID uuid = UUID.randomUUID();

    public RocketConstructorBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(BlockEntityInit.CONSTRUCTOR.get(), pPos, pBlockState);
    }

    public void assembleRocket(RocketPadBlockEntity pad, Player player)
    {
        if(pad.getLevel() == null || pad.getLevel().isClientSide())
            return;

        if(!pad.isComplete())
        {
            player.displayClientMessage(Component.literal("ERROR: Rocket Pad is not fully constructed"), true);
            return;
        }

        AABB box = pad.getOnPadBox();
        RocketEntity rocketEntity = new RocketEntity(pad.getLevel());
        Rocket rocket = new Rocket(rocketEntity, new LinkedHashSet<>());
        Stage currentStage = new Stage(rocket);
        BlockPos firstFound = null;
        for(double y = box.minY; y <= box.maxY; y++)
        {
            boolean foundSeparator = false;
            for(double x = box.minX; x <= box.maxX; x++)
                for(double z = box.minZ; z <= box.maxZ; z++)
                {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    BlockState state = pad.getLevel().getBlockState(pos);
                    if(state.isAir() || state.is(Blocks.BEDROCK)) continue;

                    if(firstFound == null) firstFound = pos;

                    BlockData data = BlockData.VOID;

                    for(BiFunction<Stage, BlockPos, BlockData> func : RocketBlockDataInit.DATA_FACTORY)
                    {
                        BlockData attemptedData = func.apply(currentStage, pos);
                        if(attemptedData == BlockData.VOID) break;

                        data = attemptedData;
                        if(attemptedData != null) break;
                    }

                    if(data != BlockData.VOID && data != null)
                    {
                        if(!currentStage.palette.contains(state)) currentStage.palette.add(state);

                        data.pos = pos.subtract(firstFound);
                        currentStage.blocks.put(data.pos, data);

                    }

                    if(state.getBlock() instanceof SeparatorBlock) foundSeparator = true;

                }
            if(foundSeparator)
            {
                rocket.stages.add(currentStage);
                currentStage = new Stage(rocket);
            }
        }

        if(firstFound == null)
        {
            player.displayClientMessage(Component.literal("ERROR: Rocket Pad is empty"), true);
            return;
        }

        rocket.stages.add(currentStage);
        rocketEntity.setRocket(rocket);
        rocketEntity.setPos(firstFound.getCenter().add(0, -0.5, 0));
        if(!rocket.stages.isEmpty())
        {
            pad.getLevel().addFreshEntity(rocketEntity);
            player.displayClientMessage(Component.literal("SUCCESS! Rocket assembled!"), true);
            int stageI = 0;
            for(Stage stage : rocket.stages)
            {
                System.out.println("Stage[" + stageI + "] = " + stage.calculateDeltaV());
            }

            System.out.println("Rocket TWR - " + rocket.getMaxTWR());
            rocket.landingSimulation();
            System.out.println("Target Orbit DeltaV Requirement - " + OrbitalMath.getLaunchDeltaV(rocket.getCelestialBody(pad.getLevel()), 300*1000));

            System.out.println("LEO -> Luna transfer example... calculating...");
            Vector2d A = new Vector2d(0,0);
            Vector2d B = new Vector2d(4,0);
            Vector2d C = new Vector2d(0, 4);
            OrbitalPath path = OrbitalMath.calculatePath(A, B, C);
            List<Vector2d> points = path.getPathPoints(10);
            if(path instanceof EllipticalPath)
                System.out.println("Elliptical");
            else System.out.println("Hyperbolic");
            System.out.println("Retrograde : " + path.isRetrograde());
            for(int i = 0; i < points.size(); i++)
            {
                Vector2d point = points.get(i);
                System.out.println("Point [" + i + "] is [" + point.x + ", " + point.y + "]");
            }


        } else player.displayClientMessage(Component.literal("ERROR: Rocket Pad is empty! Report to developer!"), true);

    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putUUID("uuid", this.uuid);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.uuid = tag.getUUID("uuid");
    }

    @Override
    public void setPadUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public UUID getPadUUID()
    {
        return uuid;
    }
}
