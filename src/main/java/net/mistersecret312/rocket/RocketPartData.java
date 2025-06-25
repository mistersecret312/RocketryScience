package net.mistersecret312.rocket;

public class RocketPartData
{
    private int durability;
    private int maxDurability;
    private double mass;
    private double reliability;

    public RocketPartData(int durability, int maxDurability, double mass, double reliability)
    {
        this.durability = durability;
        this.maxDurability = maxDurability;
        this.mass = mass;
        this.reliability = reliability;
    }

    public double getMass()
    {
        return mass;
    }

    public double getReliability()
    {
        return reliability;
    }

    public int getDurability()
    {
        return durability;
    }

    public int getMaxDurability()
    {
        return maxDurability;
    }

    public void setMass(double mass)
    {
        this.mass = mass;
    }

    public void setReliability(double reliability)
    {
        this.reliability = reliability;
    }

    public void setDurability(int durability)
    {
        this.durability = durability;
    }

    public void setMaxDurability(int maxDurability)
    {
        this.maxDurability = maxDurability;
    }
}

