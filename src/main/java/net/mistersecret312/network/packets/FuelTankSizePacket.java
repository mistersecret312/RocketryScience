package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.function.Supplier;

public class FuelTankSizePacket
{
    public final BlockPos pos;
    public final int size;
    public final BlockPos controller;

    public FuelTankSizePacket(BlockPos pos, int size, BlockPos controller)
    {
        this.pos = pos;
        this.size = size;
        this.controller = controller;
    }

    public static void write(FuelTankSizePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.size);
        buffer.writeBlockPos(packet.controller);
    }

    public static FuelTankSizePacket read(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readInt();
        BlockPos controller = buffer.readBlockPos();

        return new FuelTankSizePacket(pos, size, controller);
    }

    public static void handle(FuelTankSizePacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handleSizePacket(packet));
        ctx.get().setPacketHandled(true);
    }
}
