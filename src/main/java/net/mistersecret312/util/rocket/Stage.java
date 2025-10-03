package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.init.RocketBlockDataInit;

import java.util.*;

public class Stage
{
    public List<BlockState> palette;
    public HashMap<BlockPos, BlockData> blocks;
    public List<FluidStack> fluidStacks;
    public List<FluidStack> maxFluids;
    public int solidFuel;
    public int maxSolidFuel;

    public double mass = 10000;

    private Rocket rocket;

    public Stage(Rocket rocket)
    {
        this.rocket = rocket;

        this.palette = new ArrayList<>();
        this.blocks = new HashMap<>();
        this.fluidStacks = new ArrayList<>();
        this.maxFluids = new ArrayList<>();
        this.solidFuel = 0;
        this.maxSolidFuel = 0;
    }

    public Stage(Rocket rocket, List<BlockState> palette, HashMap<BlockPos, BlockData> blocks,
                 List<FluidStack> fluidStacks, List<FluidStack> maxFluids, int solidFuel, int maxSolidFuel)
    {
        this.palette = palette;
        this.blocks = blocks;
        this.fluidStacks = fluidStacks;
        this.maxFluids = maxFluids;
        this.solidFuel = solidFuel;
        this.maxSolidFuel = maxSolidFuel;

        this.rocket = rocket;
    }

    public void tick(Level level)
    {
        if(this.blocks.isEmpty())
        {
            this.rocket.stages.remove(this);
            return;
        }

        if(this.blocks.isEmpty() && this.rocket.stages.size() == 1)
        {
            this.rocket.getRocketEntity().discard();
            return;
        }
        for (Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
        {
            BlockData data = entry.getValue();
            if (data.doesTick(level))
                data.tick(level);
        }
    }

    public Rocket getRocket()
    {
        return rocket;
    }

    public double calculateDeltaV()
    {
        double averageIsp = getAverageIsp();
        if(getTotalDryMass() == 0)
            return 0;
        double massRatio = getTotalMass()/getTotalDryMass();
        double log = Math.log(massRatio);
        return 9.8*averageIsp*log;
    }

    public double getTotalMass()
    {
        double mass = 0;
        for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
        {
            mass += entry.getValue().getMass();
        }

        return mass;
    }

    public double getFuelMass()
    {
        return getTotalMass()-getTotalDryMass();
    }

    public int getFuelTypeAmount()
    {
        int amount = 0;
        for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData data)
            {
                amount = data.fuelType.getPropellants().size();
            }
        }

        return amount;
    }

    public double getTotalDryMass()
    {
        double mass = 0;
        for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
        {
            mass += entry.getValue().getDryMass();
        }

        return mass;
    }

    public double getAverageIsp()
    {
        double Isp = 0;
        int amount = 0;
        for(Map.Entry<BlockPos, BlockData> entry : this.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData data)
            {
                Isp += data.getIsp();
                amount++;
            }
        }
        if(amount == 0)
            return 0;

        return Isp/amount;
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.palette, (writer, state) -> {
            writer.writeId(Block.BLOCK_STATE_REGISTRY, state);
        });

        buffer.writeCollection(this.blocks.entrySet(), (writer, data) -> {
            writer.writeBlockPos(data.getKey());
            writer.writeRegistryId(RocketBlockDataInit.ROCKET_DATA.get(), data.getValue().getType());
            data.getValue().toNetwork(writer);
        });

        buffer.writeCollection(fluidStacks, IForgeFriendlyByteBuf::writeFluidStack);
        buffer.writeCollection(maxFluids, IForgeFriendlyByteBuf::writeFluidStack);

        buffer.writeInt(solidFuel);
        buffer.writeInt(maxSolidFuel);
    }

    public static Stage fromNetwork(FriendlyByteBuf buffer, Rocket rocket)
    {
        Stage stage = new Stage(rocket);

        List<BlockState> pallete = buffer.readCollection(ArrayList::new, reader -> reader.readById(Block.BLOCK_STATE_REGISTRY));

        HashMap<BlockPos, BlockData> blocks = new HashMap<>();
        int sizeBlocks = buffer.readVarInt();
        for (int i = 0; i < sizeBlocks; i++)
        {
            BlockPos pos = buffer.readBlockPos();
            BlockData data = buffer.readRegistryId();
            data.fromNetwork(buffer, pos, stage);
            blocks.put(pos, data);
        }
        List<FluidStack> fluidStacks = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readFluidStack);
        List<FluidStack> maxFluids = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readFluidStack);

        int solidFuel = buffer.readInt();
        int maxSolidFuel = buffer.readInt();

        stage.rocket = rocket;
        stage.palette = pallete;
        stage.blocks = blocks;
        stage.fluidStacks = fluidStacks;
        stage.maxFluids = maxFluids;
        stage.solidFuel = solidFuel;
        stage.maxSolidFuel = maxSolidFuel;

        return stage;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        ListTag paletteTag = new ListTag();
        for(BlockState state : palette)
        {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        tag.put("palette", paletteTag);

        ListTag blocksTag = new ListTag();
        for(Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
        {
            blocksTag.add(entry.getValue().save());
        }
        tag.put("blocks", blocksTag);

        ListTag storedFluidTag = new ListTag();
        ListTag maxFluidsTag = new ListTag();
        for(FluidStack stack : fluidStacks)
            storedFluidTag.add(stack.writeToNBT(new CompoundTag()));
        for(FluidStack stack : maxFluids)
            maxFluidsTag.add(stack.writeToNBT(new CompoundTag()));
        tag.put("stored_fluid", storedFluidTag);
        tag.put("max_fluid", maxFluidsTag);

        tag.putInt("solid_fuel", solidFuel);
        tag.putInt("max_solid_fuel", maxSolidFuel);

        return tag;
    }

    public void load(CompoundTag tag)
    {
        ListTag paletteTag = tag.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for(Tag listTag : paletteTag)
            palette.add(NbtUtils.readBlockState(getRocket().getRocketEntity().level().holderLookup(Registries.BLOCK), (CompoundTag) listTag));
        this.palette = palette;

        ListTag blocksTag = tag.getList("blocks", Tag.TAG_COMPOUND);
        HashMap<BlockPos, BlockData> blocks = new HashMap<>();
        for(Tag listTag : blocksTag)
        {
            CompoundTag listCompound = ((CompoundTag) listTag);
            ResourceLocation type = ResourceLocation.tryParse(listCompound.getString("type"));
            BlockDataType<?> dataType = RocketBlockDataInit.ROCKET_DATA.get().getValue(type);
            BlockData data = dataType.supplier.get();

            data.load(listCompound, this);
            data.initializeData(this);
            blocks.put(data.pos, data);
        }
        this.blocks = blocks;

        ListTag storedFluidTag = tag.getList("stored_fluid", Tag.TAG_COMPOUND);
        List<FluidStack> storedFluid = new ArrayList<>();
        for(Tag listTag : storedFluidTag)
        {
            storedFluid.add(FluidStack.loadFluidStackFromNBT((CompoundTag) listTag));
        }

        ListTag maxStoredFluidTag = tag.getList("max_stored_fluid", Tag.TAG_COMPOUND);
        List<FluidStack> maxStoredFluid = new ArrayList<>();
        for(Tag listTag : maxStoredFluidTag)
        {
            storedFluid.add(FluidStack.loadFluidStackFromNBT((CompoundTag) listTag));
        }
        this.fluidStacks = storedFluid;
        this.maxFluids = maxStoredFluid;

        this.solidFuel = tag.getInt("solid_fuel");
        this.maxSolidFuel = tag.getInt("max_solid_fuel");
    }
}
