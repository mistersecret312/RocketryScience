package net.mistersecret312.util.rocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.CombustionChamberBlock;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.client.model.PlumeModel;
import net.mistersecret312.client.renderer.PlumeRenderer;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.fluids.RocketFuelTank;
import net.mistersecret312.init.RocketBlockDataInit;
import net.mistersecret312.items.CombustionChamberItem;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketState;

import java.util.*;
import java.util.function.BiFunction;

public class RocketEngineData extends BlockData
{
    public BlockState nozzleState;
    public boolean enabled;
    public ArrayList<Map.Entry<BlockPos, BlockData>> tanks = new ArrayList<>();

    public double mass;
    public double thrust_kN;
    public double Isp;
    public double thrustPercentage = 0.0;

    public ItemStackHandler handler = new ItemStackHandler(3);
    public RocketFuel fuelType;
    public RocketFuelTank tank;

    public int frame = 0;
    public int animTick = 0;

    public RocketEngineData(Stage stage, int state, BlockState nozzleState, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
        this.nozzleState = nozzleState;
    }

    public RocketEngineData()
    {

    }

    @Override
    public void tick(Level level)
    {
        if(level.isClientSide())
        {
            clientTick(level);
            return;
        }

        if(this.tanks.isEmpty())
        {
            ArrayList<Map.Entry<BlockPos, BlockData>> datas = new ArrayList<>(this.getStage().blocks.entrySet());
            datas.removeIf(entry ->
                           {
                               if(entry.getValue() instanceof FuelTankData tank)
                                   return !tank.fuel.equals(this.fuelType);

                               return true;
                           });
            datas.sort(Comparator.comparing(entry -> entry.getKey().distSqr(pos)));
            this.tanks = datas;
        }

        Rocket rocket = this.getStage().getRocket();
        RocketEntity rocketEntity = rocket.getRocketEntity();

        if(!hasFuel() && enabled)
        {
            enabled = false;
            this.getStage().getRocket().setState(RocketState.COASTING);
            return;
        }
        else
        {
            FuelTankData fuelTank = getBestFuelTank();
            if(fuelTank != null)
                for(int i = 0; i < fuelTank.tank.getTanks(); i++)
                {
                    int maxFuelUsage = calculateMaxFuelUsage();
                    if(this.tank.getSpace(i) > maxFuelUsage)
                    {
                        FluidStack stack = fuelTank.tank.drain(new FluidStack(fuelTank.tank.getFluidInTank(i), maxFuelUsage),
                                                               IFluidHandler.FluidAction.EXECUTE);
                        this.tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
        }

        if(!enabled)
        {
            this.nozzleState = nozzleState.setValue(NozzleBlock.ACTIVE, false);
            if(level.getGameTime() % 400 == 0)
            {
                int targetHotness = Math.max(0, nozzleState.getValue(NozzleBlock.HOT) - 1);
                nozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
            }
            return;
        }
        else this.nozzleState = nozzleState.setValue(NozzleBlock.ACTIVE, true);

        for(int i = 0; i < this.tank.getTanks(); i++)
        {
            if(thrustPercentage == 0.0)
            {
                enabled = false;
                rocket.setState(RocketState.COASTING);
                return;
            }
            else
            {
                FluidStack drained = this.tank.drain(
                        new FluidStack(this.tank.getFluidInTank(i), (int) (calculateMaxFuelUsage() * (thrustPercentage))),
                        IFluidHandler.FluidAction.EXECUTE);

                if(drained.isEmpty() && thrustPercentage*8 == 0 && !(thrustPercentage == 0))
                {
                    enabled = false;
                    rocket.setState(RocketState.COASTING);
                    return;
                }
            }

        }
        if(level.getGameTime() % 200 == 0)
        {
            int targetHotness = Math.min(3, nozzleState.getValue(NozzleBlock.HOT) + 1);
            nozzleState = nozzleState.setValue(NozzleBlock.HOT, targetHotness);
        }

        double mass = rocket.getMassKilogram();
        double twr = (thrust_kN*1000)/(mass*9.80665);
        double accel = 0.025*twr*thrustPercentage;
        rocketEntity.addDeltaMovement(new Vec3(0, accel, 0));
    }

    public void clientTick(Level level)
    {
        if(!level.isClientSide())
            return;

        animTick++;
        if(animTick > 10)
        {
            frame++;
            animTick = 0;
        }
        if(frame > 1)
        {
            frame = 0;
        }
    }

    public boolean hasFuel()
    {
        List<Boolean> tanks = new ArrayList<>();
        for(int tank = 0; tank < this.tank.getTanks(); tank++)
        {
            int space = this.tank.getSpace(tank);
            int capacity = this.tank.getTankCapacity(tank);
            tanks.add(capacity>space);
        }

        return getBestFuelTank() != null || tanks.stream().allMatch(bool -> bool);
    }

    public int calculateMaxFuelUsage()
    {
        return (int) (((thrust_kN*1000)/(Isp*9.8))/20);
    }

    public FuelTankData getBestFuelTank()
    {
        FuelTankData tank = null;
        datas:
        for(Map.Entry<BlockPos, BlockData> data : this.tanks)
        {
            FuelTankData fuelTank = ((FuelTankData) data.getValue());
            for(int i = 0; i < fuelTank.tank.getTanks(); i++)
            {
                if(fuelTank.tank.getFluidInTank(i).isEmpty())
                    continue datas;
            }
            tank = fuelTank;
        }
        return tank;
    }

    @Override
    public double getMass()
    {
        int fluidMass = 0;
        for(int tank = 0; tank < this.tank.getTanks(); tank++)
        {
            fluidMass += this.tank.getFluidInTank(tank).getAmount();
        }

        return mass+fluidMass;
    }

    @Override
    public double getDryMass()
    {
        return this.mass;
    }

    public double getIsp()
    {
        return Isp;
    }

    @Override
    public BlockDataType<?> getType()
    {
        return RocketBlockDataInit.ROCKET_ENGINE.get();
    }

    @Override
    public void initializeData(Stage stage)
    {
        this.mass = this.extraData.getDouble("mass");
        this.thrust_kN = this.extraData.getDouble("thrust");
        this.Isp = this.extraData.getDouble("efficiency");

        this.handler = new ItemStackHandler(3);
        this.handler.deserializeNBT(this.extraData.getCompound("chamber"));
        ItemStack chamberStack = this.handler.getStackInSlot(0);
        if(chamberStack.getItem() instanceof CombustionChamberItem chamber)
            this.fuelType = chamber.getFuelType(chamberStack);

        this.tank = new RocketFuelTank(fuelType.getPropellants(), 1000)
        {
            @Override
            protected void onContentsChanged()
            {
                this.writeToNBT(extraData);
            }
        };

        this.tank.readFromNBT(this.extraData);
        this.tank.getTanks();
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getRocket().getRocketEntity().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag extraData;
            if(blockEntity instanceof RocketEngineBlockEntity rocketEngine)
            {
                BlockState nozzleState = level.getBlockState(pos.relative(state.getValue(CombustionChamberBlock.FACING).getOpposite()));
                if (rocketEngine.isBuilt && nozzleState.getBlock() instanceof NozzleBlock)
                {
                    extraData = blockEntity.saveWithId();
                    if(!stage.palette.contains(state))
                        stage.palette.add(state);

                    level.removeBlockEntity(pos);
                    level.removeBlock(pos, false);
                    level.removeBlock(pos.relative(state.getValue(CombustionChamberBlock.FACING).getOpposite()), false);
                    return new RocketEngineData(stage, stage.palette.indexOf(state), nozzleState, pos, extraData);
                }
            }
            if(state.getBlock() instanceof NozzleBlock)
            {
                RocketEngineBlockEntity rocketEngineBlockEntity = (RocketEngineBlockEntity) level.getBlockEntity(pos.relative(state.getValue(NozzleBlock.FACING)));
                if(rocketEngineBlockEntity != null && rocketEngineBlockEntity.isBuilt)
                    return BlockData.VOID;

            }
            return null;
        };
    }

    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        minX = (Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5));
        minY = (Math.min(aabb.minY, rocket.position().y+pos.getY()-1));
        minZ = (Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5));
        maxX = (Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5));
        maxY = (Math.max(aabb.maxY, rocket.position().y+pos.getY()+1));
        maxZ = (Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5));

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void placeInLevel(Level level, BlockPos pos)
    {
        super.placeInLevel(level, pos);

        level.setBlock(pos.offset(this.getBlockState().getValue(CombustionChamberBlock.FACING).getOpposite().getNormal()), nozzleState, 2);
    }

    @Override
    public void render(RocketEntity rocket, BlockRenderDispatcher dispatcher, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, BlockPos.MutableBlockPos mutablePos)
    {
        super.render(rocket, dispatcher, yaw, partial, pose, buffer, mutablePos);
        PlumeRenderer plume = new PlumeRenderer(RocketryScienceMod.ClientModEvents.plumeModel);

        pose.pushPose();

        int thrust = 0;
        if(enabled)
        {
            thrust = Math.max(1, Math.min((int) (thrustPercentage*15), 15));
            plume.renderPlume(frame, thrust, getBlockState(), pose, buffer, OverlayTexture.NO_OVERLAY);
        }
        else
        {
        }
        pose.translate(0, -1, 0);

        BakedModel model = dispatcher.getBlockModel(getBlockState());
        for (RenderType rt : model.getRenderTypes(getBlockState(), RandomSource.create(42), ModelData.EMPTY))
        {
            dispatcher.renderBatched(nozzleState, mutablePos.move(pos), rocket.level(), pose, buffer.getBuffer(rt), true, RandomSource.create(42), model.getModelData(rocket.level(), pos, nozzleState, ModelData.EMPTY), null);
        }
        mutablePos.move(-pos.getX(), -pos.getY(), -pos.getZ());

        pose.popPose();
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer)
    {
        super.toNetwork(buffer);
        buffer.writeBoolean(this.enabled);
        buffer.writeDouble(this.thrustPercentage);
    }

    @Override
    public void fromNetwork(FriendlyByteBuf buffer, BlockPos pos, Stage stage)
    {
        super.fromNetwork(buffer, pos, stage);
        this.enabled = buffer.readBoolean();
        this.thrustPercentage = buffer.readDouble();
    }

    @Override
    public CompoundTag save()
    {
        CompoundTag tag = super.save();
        tag.put("nozzle", NbtUtils.writeBlockState(nozzleState));
        return tag;
    }

    @Override
    public void load(CompoundTag tag, Stage stage)
    {
        super.load(tag, stage);
        this.nozzleState = NbtUtils.readBlockState(stage.getRocket().getRocketEntity().level().holderLookup(Registries.BLOCK),
                                                   tag.getCompound("nozzle"));
    }

    @Override
    public boolean doesTick(Level level)
    {
        return true;
    }

}
