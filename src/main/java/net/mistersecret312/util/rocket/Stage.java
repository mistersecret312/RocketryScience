package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;

import javax.annotation.Nullable;
import java.util.*;

public class Stage
{
    public HashMap<BlockPos, BlockData> blocks;
    public List<FluidStack> fluidStacks;
    public List<FluidStack> maxFluids;
    public int solidFuel;
    public int maxSolidFuel;

    private Rocket rocket;

    public Stage()
    {}

    public Stage(Rocket rocket, HashMap<BlockPos, BlockData> blocks,
                 List<FluidStack> fluidStacks, List<FluidStack> maxFluids,
                 int solidFuel, int maxSolidFuel)
    {
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
        buffer.writeCollection(this.blocks.entrySet(), (writer, data) -> {
            writer.writeBlockPos(data.getKey());
            writer.writeId(Block.BLOCK_STATE_REGISTRY, data.getValue().state);
            writer.writeNbt(data.getValue().extraData);
        });

        buffer.writeCollection(fluidStacks, IForgeFriendlyByteBuf::writeFluidStack);
        buffer.writeCollection(maxFluids, IForgeFriendlyByteBuf::writeFluidStack);

        buffer.writeInt(solidFuel);
        buffer.writeInt(maxSolidFuel);
    }

    public static Stage fromNetwork(FriendlyByteBuf buffer, Rocket rocket)
    {
        Stage stage = new Stage();

        HashMap<BlockPos, BlockData> blocks = new HashMap<>();
        int sizeBlocks = buffer.readVarInt();
        for (int i = 0; i < sizeBlocks; i++)
        {
            BlockPos pos = buffer.readBlockPos();
            blocks.put(pos, new BlockData(stage, buffer.readById(Block.BLOCK_STATE_REGISTRY), pos, buffer.readNbt()));
        }
        List<FluidStack> fluidStacks = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readFluidStack);
        List<FluidStack> maxFluids = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readFluidStack);

        int solidFuel = buffer.readInt();
        int maxSolidFuel = buffer.readInt();

        stage.rocket = rocket;
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

        ListTag blocksTag = new ListTag();
        for(Map.Entry<BlockPos, BlockData> entry : blocks.entrySet())
        {
            CompoundTag data = new CompoundTag();
            data.putInt("X", entry.getKey().getX());
            data.putInt("Y", entry.getKey().getY());
            data.putInt("Z", entry.getKey().getZ());

            //TODO - figure out saving blockstates to NBT.
            blocksTag.add(data);
        }

        return tag;
    }
}
