package net.mistersecret312.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.network.ClientPacketHandler;
import net.mistersecret312.sound.SoundAccess;

import java.util.function.Supplier;

public class RocketEngineSoundPacket
{
    public final BlockPos pos;
    public final boolean stop;

    public RocketEngineSoundPacket(BlockPos pos, boolean stop)
    {
        this.pos = pos;
        this.stop = stop;
    }

    public static void write(RocketEngineSoundPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.stop);
    }

    public static RocketEngineSoundPacket read(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        boolean stop = buffer.readBoolean();

        return new RocketEngineSoundPacket(pos, stop);
    }

    public static void handle(RocketEngineSoundPacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handleSoundPacket(packet));
        ctx.get().setPacketHandled(true);
    }
}
