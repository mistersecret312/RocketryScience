package net.mistersecret312.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.init.SoundInit;

public class SoundAccess
{
    protected static Minecraft minecraft = Minecraft.getInstance();

    public static void playLiquidRocketPlume(BlockPos pos)
    {
        if(minecraft.level.getBlockEntity(pos) instanceof RocketEngineBlockEntity rocketEngine)
        {
            if(rocketEngine.runningSound == null)
                rocketEngine.runningSound = new RocketEngineSoundWrapper(rocketEngine, new SimpleSoundInstance(SoundInit.ROCKET_ENGINE_RUN_LIQUID.get(),
                        SoundSource.BLOCKS, 1F, 1F, SoundInstance.createUnseededRandom(), pos));

        }
    }
}
