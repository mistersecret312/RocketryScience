package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.function.Supplier;

public class FuelTankFrostPacket
{
    public final BlockPos pos;
    public final float ratio;

    public FuelTankFrostPacket(BlockPos pos, float ratio)
    {
        this.pos = pos;
        this.ratio = ratio;
    }

    public static void write(FuelTankFrostPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeFloat(packet.ratio);
    }

    public static FuelTankFrostPacket read(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        float ratio = buffer.readFloat();

        return new FuelTankFrostPacket(pos, ratio);
    }

    public static void handle(FuelTankFrostPacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handleFrostPacket(packet));
        ctx.get().setPacketHandled(true);
    }
}
