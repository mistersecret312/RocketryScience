package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.network.ClientPacketHandler;

import java.util.function.Supplier;

public class RocketEngineUpdatePacket
{
    public BlockPos pos;
    public boolean isBuilt;
    public boolean isRunning;
    public int throttle;

    public RocketEngineUpdatePacket(BlockPos pos, boolean isBuilt, boolean isRunning, int throttle)
    {
        this.pos = pos;
        this.isBuilt = isBuilt;
        this.isRunning = isRunning;
        this.throttle = throttle;
    }

    public static void write(RocketEngineUpdatePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.isBuilt);
        buffer.writeBoolean(packet.isRunning);
        buffer.writeInt(packet.throttle);
    }

    public static RocketEngineUpdatePacket read(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        boolean isBuilt = buffer.readBoolean();
        boolean isRunning = buffer.readBoolean();
        int throttle = buffer.readInt();

        return new RocketEngineUpdatePacket(pos, isBuilt, isRunning, throttle);
    }

    public static void handle(RocketEngineUpdatePacket packet, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientPacketHandler.handleRocketEngineUpdatePacket(packet));
        context.get().setPacketHandled(true);
    }
}
