package net.mistersecret312.rocket.engine;

import net.mistersecret312.rocket.RocketPartData;

public class RocketEngineData extends RocketPartData
{
    private double thrust;
    private double efficiencyAtmosphere;
    private double efficiencyVacuum;

    public RocketEngineData(int durability, int maxDurability, double mass, double reliability,
                            double thrust, double efficiencyAtmosphere, double efficiencyVacuum)
    {
        super(durability, maxDurability, mass, reliability);
        this.thrust = thrust;
        this.efficiencyAtmosphere = efficiencyAtmosphere;
        this.efficiencyVacuum = efficiencyVacuum;
    }
}
