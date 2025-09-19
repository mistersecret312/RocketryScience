package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class Stage
{
    public List<BlockState> palette;
    public HashMap<BlockPos, BlockData> blocks;
    public List<FluidStack> fluidStacks;
    public List<FluidStack> maxFluids;
    public int solidFuel;
    public int maxSolidFuel;

    private Rocket rocket;

    public Stage(Rocket rocket)
    {
        this.rocket = rocket;
    }

    public Stage(Rocket rocket, List<BlockState> palette, HashMap<BlockPos, BlockData> blocks,
                 List<FluidStack> fluidStacks, List<FluidStack> maxFluids,
                 int solidFuel, int maxSolidFuel)
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
        for(Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
        {
            BlockData data = entry.getValue();
            if(data.doesTick(level))
                data.tick(level);
        }
    }

    public Rocket getRocket()
    {
        return rocket;
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeCollection(this.palette, (writer, state) -> {
            writer.writeId(Block.BLOCK_STATE_REGISTRY, state);
        });

        buffer.writeCollection(this.blocks.entrySet(), (writer, data) -> {
            writer.writeBlockPos(data.getKey());
            writer.writeInt(data.getValue().state);
            writer.writeNbt(data.getValue().extraData);
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
            blocks.put(pos, new BlockData(stage, buffer.readInt(), pos, buffer.readNbt()));
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
            CompoundTag data = new CompoundTag();
            data.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            data.putInt("state", entry.getValue().state);
            data.put("extra_data", entry.getValue().extraData);
            blocksTag.add(data);
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
            BlockPos pos = NbtUtils.readBlockPos(listCompound);
            blocks.put(pos, new BlockData(this, listCompound.getInt("state"), pos, listCompound.getCompound("extra_data")));
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
