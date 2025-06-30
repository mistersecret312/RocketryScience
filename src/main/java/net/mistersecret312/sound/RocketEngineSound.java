package net.mistersecret312.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;

public class RocketEngineSound extends AbstractTickableSoundInstance
{
    protected RocketEngineBlockEntity rocketEngine;
    private Minecraft minecraft = Minecraft.getInstance();

    public RocketEngineSound(RocketEngineBlockEntity rocketEngine, SoundEvent event)
    {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.rocketEngine = rocketEngine;
        this.x = rocketEngine.getBlockPos().getX();
        this.y = rocketEngine.getBlockPos().getY();
        this.z = rocketEngine.getBlockPos().getZ();
        this.relative = true;
    }

    @Override
    public void tick()
    {
        if(rocketEngine == null)
            this.stop();
    }

    @Override
    public boolean canPlaySound()
    {
        return rocketEngine != null && canStartSilent();
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    public void stopSound()
    {
        this.stop();
    }

    public Vec3 getPosition()
    {
        return new Vec3(x, y, z);
    }

    public double getDistanceFromSource()
    {
        LocalPlayer player = minecraft.player;
        Vec3 playerPos = player.position();
        return getPosition().distanceTo(playerPos);
    }

    public float getVolume()
    {
        float localVolume = 0.0F;
        double distanceFromSource = getDistanceFromSource();

        //TODO - make this a config;
        float fullDistance = 32F;
        float maxDistance = 64F;

        if(fullDistance >= maxDistance)
            maxDistance = fullDistance + 1;

        if(distanceFromSource <= fullDistance)
            localVolume = getMaxVolume();
        else if(distanceFromSource <= maxDistance)
            localVolume = (float) (getMaxVolume() - (distanceFromSource - fullDistance) / (maxDistance - fullDistance));
        else
            localVolume = getMinVolume();

        return 1.0F;
    }

    public float getMaxVolume()
    {
        return 1.0F;
    }

    public float getMinVolume()
    {
        return 0.0F;
    }
}
