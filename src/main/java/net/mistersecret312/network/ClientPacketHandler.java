package net.mistersecret312.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.network.packets.RocketEngineUpdatePacket;

public class ClientPacketHandler
{
    public static void handleRocketEngineUpdatePacket(RocketEngineUpdatePacket packet)
    {
        BlockEntity blockEntity = getBlockEntity(packet.pos);
        if(blockEntity instanceof RocketEngineBlockEntity rocketEngine)
        {
            rocketEngine.isBuilt = packet.isBuilt;
            rocketEngine.isRunning = packet.isRunning;
            rocketEngine.throttle = packet.throttle;
        }
    }

    public static <T extends BlockEntity> T getBlockEntity(BlockPos pos)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null)
            return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return (T) blockEntity;
    }
}
