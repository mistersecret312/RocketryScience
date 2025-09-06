package net.mistersecret312.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.block_entities.FuelTankBlockEntity;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.block_entities.SeparatorBlockEntity;
import net.mistersecret312.init.SoundInit;
import net.mistersecret312.network.packets.FuelTankFrostPacket;
import net.mistersecret312.network.packets.FuelTankSizePacket;
import net.mistersecret312.network.packets.RocketEngineSoundPacket;
import net.mistersecret312.network.packets.RocketEngineUpdatePacket;
import net.mistersecret312.sound.RocketEngineSound;
import net.mistersecret312.sound.SoundAccess;

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

    public static void handleFrostPacket(FuelTankFrostPacket packet)
    {
        BlockEntity blockEntity = getBlockEntity(packet.pos);
        if(blockEntity instanceof FuelTankBlockEntity fuelTank)
            fuelTank.ratio = packet.ratio;
    }

    public static void handleSizePacket(FuelTankSizePacket packet)
    {
        BlockEntity blockEntity = getBlockEntity(packet.pos);
        if(blockEntity instanceof FuelTankBlockEntity fuelTank)
        {
            fuelTank.load(packet.tag);
        }
        if(blockEntity instanceof SeparatorBlockEntity separator)
        {
            separator.load(packet.tag);
        }
    }

    public static void handleSoundPacket(RocketEngineSoundPacket packet)
    {
        BlockEntity blockEntity = getBlockEntity(packet.pos);
        if(blockEntity instanceof RocketEngineBlockEntity rocketEngine)
        {
            SoundAccess.playLiquidRocketPlume(packet.pos);
            rocketEngine.runningSound.playing = packet.stop;
            if(packet.stop)
                rocketEngine.runningSound.stopSound();
            else rocketEngine.runningSound.playSound();
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

    public static <T extends Entity> T getEntity(int id)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null)
            return null;
        Entity entity = level.getEntity(id);
        return (T) entity;
    }
}
