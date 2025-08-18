package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.function.Supplier;

public class FuelTankSizePacket
{
    public final BlockPos pos;
    public final int size;
    public final CompoundTag tag;

    public FuelTankSizePacket(BlockPos pos, int size, CompoundTag tag)
    {
        this.pos = pos;
        this.size = size;
        this.tag = tag;
    }

    public static void write(FuelTankSizePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.size);
        buffer.writeNbt(packet.tag);
    }

    public static FuelTankSizePacket read(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readInt();
        CompoundTag tag = buffer.readNbt();

        return new FuelTankSizePacket(pos, size, tag);
    }

    public static void handle(FuelTankSizePacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handleSizePacket(packet));
        ctx.get().setPacketHandled(true);
    }
}
