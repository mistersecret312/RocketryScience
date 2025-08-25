package net.mistersecret312.blueprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;

import java.util.ArrayList;
import java.util.List;

public class RocketEngineBlueprint implements Blueprint
{
    public double mass;

    public RocketMaterial combustionChamberMaterial;
    public RocketMaterial nozzleMaterial;
    public RocketFuel rocketFuel;

    public double thrust_kN;
    public double Isp_vacuum;
    public double Isp_atmosphere;


    public RocketEngineBlueprint(double mass,
                                 RocketMaterial combustionChamberMaterial, RocketMaterial nozzleMaterial,
                                 RocketFuel rocketFuel, double thrust_kN, double Isp_vacuum, double Isp_atmosphere)
    {
        this.mass = mass;

        this.combustionChamberMaterial = combustionChamberMaterial;
        this.nozzleMaterial = nozzleMaterial;
        this.rocketFuel = rocketFuel;

        this.thrust_kN = thrust_kN;
        this.Isp_vacuum = Isp_vacuum;
        this.Isp_atmosphere = Isp_atmosphere;
    }

    public RocketEngineBlueprint(RocketMaterial combustionChamberMaterial, RocketMaterial nozzleMaterial,
                                 RocketFuel rocketFuel)
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

        this.mass = 2500 * ((this.combustionChamberMaterial.getMassCoefficient() + this.nozzleMaterial.getMassCoefficient()) / 2);
    }

    private void calculateRocketEngineStats()
    {
        this.thrust_kN = this.rocketFuel.getThrustKiloNewtons() * ((this.combustionChamberMaterial.getThrustCoefficient() + this.nozzleMaterial.getThrustCoefficient()) / 2);

        this.Isp_vacuum = this.rocketFuel.getEfficiency() * ((this.combustionChamberMaterial.getEfficiencyCoefficientVacuum() + this.nozzleMaterial.getEfficiencyCoefficientVacuum()) / 2);
        this.Isp_atmosphere = this.rocketFuel.getEfficiency() * ((this.combustionChamberMaterial.getEfficiencyCoefficientAtmosphere() + this.nozzleMaterial.getEfficiencyCoefficientAtmosphere()) / 2);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        tag.putString("combustion_chamber_material", this.combustionChamberMaterial.toString());
        tag.putString("nozzle_material", this.nozzleMaterial.toString());
        tag.putString("rocket_fuel", this.rocketFuel.toString());

        tag.putDouble("mass", this.mass);

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
    public void setMass(double mass)
    {
        this.mass = mass;
    }
}
