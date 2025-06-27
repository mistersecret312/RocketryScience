package net.mistersecret312.blueprint;

import net.minecraft.nbt.CompoundTag;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;

public class RocketEngineBlueprint implements Blueprint
{
    public double mass;
    public double reliability;
    public double integrity;
    public double maxIntegrity;

    public RocketMaterial combustionChamberMaterial;
    public RocketMaterial nozzleMaterial;
    public RocketFuel rocketFuel;

    public double thrust_kN;
    public double Isp_vacuum;
    public double Isp_atmosphere;

    public RocketEngineBlueprint(double mass, double reliability, double integrity, double maxIntegrity,
                                 RocketMaterial combustionChamberMaterial, RocketMaterial nozzleMaterial,
                                 RocketFuel rocketFuel, double thrust_kN, double Isp_vacuum, double Isp_atmosphere)
    {
        this.mass = mass;
        this.reliability = reliability;
        this.integrity = integrity;
        this.maxIntegrity = maxIntegrity;

        this.combustionChamberMaterial = combustionChamberMaterial;
        this.nozzleMaterial = nozzleMaterial;
        this.rocketFuel = rocketFuel;

        this.thrust_kN = thrust_kN;
        this.Isp_vacuum = Isp_vacuum;
        this.Isp_atmosphere = Isp_atmosphere;
    }

    public RocketEngineBlueprint(RocketMaterial combustionChamberMaterial, RocketMaterial nozzleMaterial, RocketFuel rocketFuel)
    {
        this.combustionChamberMaterial = combustionChamberMaterial;
        this.nozzleMaterial = nozzleMaterial;
        this.rocketFuel = rocketFuel;

        calculateRocketPartStats();
        calculateRocketEngineStats();
    }

    public RocketEngineBlueprint()
    {}

    public void calculateRocketPartStats()
    {
        this.maxIntegrity = this.combustionChamberMaterial.getMaxIntegrity()+this.nozzleMaterial.getMaxIntegrity();
        this.integrity = this.maxIntegrity;

        this.mass = 2500*((this.combustionChamberMaterial.getMassCoefficient()+this.nozzleMaterial.getMassCoefficient())/2);

        this.reliability = ((this.combustionChamberMaterial.getBaseReliability()+this.nozzleMaterial.getBaseReliability())/2);
    }

    private void calculateRocketEngineStats()
    {
        this.thrust_kN = this.rocketFuel.getThrustKiloNewtons()*((this.combustionChamberMaterial.getThrustCoefficient()+this.nozzleMaterial.getThrustCoefficient())/2)*this.reliability;

        this.Isp_vacuum = this.rocketFuel.getEfficiency()*((this.combustionChamberMaterial.getEfficiencyCoefficientVacuum()+this.nozzleMaterial.getEfficiencyCoefficientVacuum())/2)*this.reliability;
        this.Isp_atmosphere = this.rocketFuel.getEfficiency()*((this.combustionChamberMaterial.getEfficiencyCoefficientAtmosphere()+this.nozzleMaterial.getEfficiencyCoefficientAtmosphere())/2)*this.reliability;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        tag.putString("combustion_chamber_material", this.combustionChamberMaterial.toString());
        tag.putString("nozzle_material", this.nozzleMaterial.toString());
        tag.putString("rocket_fuel", this.rocketFuel.toString());

        tag.putDouble("mass", this.mass);
        tag.putDouble("integrity", this.integrity);
        tag.putDouble("max_integrity", this.maxIntegrity);
        tag.putDouble("reliability", this.reliability);

        tag.putDouble("thrust", this.thrust_kN);
        tag.putDouble("isp_vacuum", this.Isp_vacuum);
        tag.putDouble("isp_atmosphere", this.Isp_atmosphere);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        this.combustionChamberMaterial = RocketMaterial.valueOf(tag.getString("combustion_chamber_material"));
        this.nozzleMaterial = RocketMaterial.valueOf(tag.getString("nozzle_material"));
        this.rocketFuel = RocketFuel.valueOf(tag.getString("rocket_fuel"));

        this.mass = tag.getDouble("mass");
        this.integrity = tag.getDouble("integrity");
        this.maxIntegrity = tag.getDouble("max_integrity");
        this.reliability = tag.getDouble("reliability");

        this.thrust_kN = tag.getDouble("thrust");
        this.Isp_vacuum = tag.getDouble("isp_vacuum");
        this.Isp_atmosphere = tag.getDouble("isp_atmosphere");

    }

    @Override
    public double getMass()
    {
        return this.mass;
    }

    @Override
    public double getReliability()
    {
        return this.reliability;
    }

    @Override
    public double getIntegrity()
    {
        return this.integrity;
    }

    @Override
    public double getMaxIntegrity()
    {
        return this.maxIntegrity;
    }

    @Override
    public void setMass(double mass)
    {
        this.mass = mass;
    }

    @Override
    public void setReliability(double reliability)
    {
        this.reliability = reliability;
    }

    @Override
    public void setIntegrity(double integrity)
    {
        this.integrity = integrity;
    }

    @Override
    public void setMaxIntegrity(double maxIntegrity)
    {
        this.maxIntegrity = maxIntegrity;
    }
}
