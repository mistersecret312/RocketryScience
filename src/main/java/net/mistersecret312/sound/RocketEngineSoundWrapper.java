package net.mistersecret312.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;

public class RocketEngineSoundWrapper
{
    protected static Minecraft minecraft = Minecraft.getInstance();

    public RocketEngineBlockEntity rocketEngine;
    public SimpleSoundInstance sound;
    public boolean playing = false;

    protected RocketEngineSoundWrapper(RocketEngineBlockEntity rocketEngine, SimpleSoundInstance sound)
    {
        this.rocketEngine = rocketEngine;
        this.sound = sound;
    }

    public boolean isPlaying()
    {
        return this.playing;
    }

    public boolean hasSound()
    {
        return this.sound != null;
    }

    public void playSound()
    {
        if(!this.playing)
        {
            minecraft.getSoundManager().play(sound);
            this.playing = true;
        }
    }

    public void stopSound()
    {
        if(this.playing)
        {
            minecraft.getSoundManager().stop(sound);
            this.playing = false;
        }
    }
}
