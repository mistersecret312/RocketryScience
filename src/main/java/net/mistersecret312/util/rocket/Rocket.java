package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.entities.RocketEntity;
import net.mistersecret312.network.ClientPacketHandler;
import net.mistersecret312.util.RocketState;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Rocket
{
    public RocketState state;
    public LinkedHashSet<Stage> stages;
    public RocketEntity rocket;

    public Rocket(RocketEntity rocket, LinkedHashSet<Stage> stages)
    {
        this.stages = stages;
        this.rocket = rocket;
        this.state = RocketState.IDLE;
    }

    public void tick(Level level)
    {
        if(stages.isEmpty())
            rocket.discard();

        for(Stage stage : stages)
            stage.tick(level);

        switch(state)
        {
            case IDLE ->
            {

            }
            case LANDING ->
            {
                if(level.isClientSide())
                    return;
                land(level);
            }
            case TAKEOFF ->
            {
                if(level.isClientSide())
                    return;
                takeoff(level);
            }
            case STAGING ->
            {
                stage(level);
            }
            case COASTING ->
            {
                if(this.stages.size() > 1)
                {
                    for(Stage stage : this.stages)
                        System.out.println("Orbital - " + stage.calculateDeltaV());
                    stage(level);
                    return;
                }

                //TODO: Early out of fuel handling && staging related && do payload stuff

                if(this.canLand())
                    setState(RocketState.LANDING);
            }
            case ORBIT ->
            {
                getRocketEntity().discard();
            }
        }
    }

    public void takeoff(Level level)
    {
        double altitude = getAltitude(level);
        double spaceY = getSpaceHeight(level);

        if(rocket.getY() <= spaceY)
        {
            toggleEngines(true);
            double engineThrust = 0.0D;
            double hover = getHoverThrust();
            double height = this.getRocketEntity().makeBoundingBox().getYsize();
            if(altitude < height)
            {
                engineThrust = hover*1.1;
                setEngineThrust(engineThrust);
            }
            else
            {
                engineThrust = hover*(altitude/height);
                setEngineThrust(engineThrust);
            };
        }
        else
        {
            System.out.println("ORBITAL - " + altitude + " DeltaV Left - " + getCurrentStage().calculateDeltaV());
            this.toggleEngines(false);
            rocket.setDeltaMovement(0, 0, 0);
            setState(RocketState.COASTING);
        }
    }

    public void land(Level level)
    {
        double altitude = getAltitude(level);
        double thrustLevel = 0.0; // fraction [0,1]
        double twr = getMaxTWR();
        double safeLandingSpeed = -0.1;
        double velocity = rocket.getDeltaMovement().y;
        double g = 0.025;

        if(altitude > 0.25) {
            double netAccelMax = (twr - 1.0) * g;

            double stoppingDistance = 0;
            if (netAccelMax > 0 && velocity < 0) {
                stoppingDistance = Math.max(this.rocket.makeBoundingBox().getYsize(), (velocity * velocity) / (2.0 * netAccelMax))*Math.max(1, 0.5*getMaxTWR());;
            }

            if (altitude <= stoppingDistance + 1)
            {
                toggleEngines(true);

                double desiredVelocity = safeLandingSpeed;
                double error = desiredVelocity - velocity;

                double Kp = 0.5;
                double Kd = 0.2;

                double accelCmd = Kp * error - Kd * velocity;
                double thrustFraction = (g + accelCmd) / (twr * g);
                thrustLevel = Mth.clamp(thrustFraction, 0.0, 1.0);
            }
            setEngineThrust(thrustLevel);
        }
        else
        {
            toggleEngines(false);
            setState(RocketState.IDLE);
            setEngineThrust(thrustLevel);
            System.out.println("LANDED" + " DeltaV Left - " + getCurrentStage().calculateDeltaV());
        }
    }

    public void stage(Level level)
    {
        Stage oldDetach = getCurrentStage();
        if(oldDetach != null)
        {
            RocketEntity rocketEntityNew = new RocketEntity(level);
            Rocket rocketNew = new Rocket(rocketEntityNew, new LinkedHashSet<>());
            Stage stageNew = new Stage(rocketNew);

            stageNew.palette = oldDetach.palette;
            stageNew.blocks = oldDetach.blocks;

            Iterator<Map.Entry<BlockPos, BlockData>> iterator = stageNew.blocks.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<BlockPos, BlockData> entry = iterator.next();
                entry.getValue().stage = stageNew;
            }

            rocketNew.stages.add(stageNew);

            this.stages.remove(oldDetach);
            rocketEntityNew.setRocket(rocketNew);

            double height = rocketEntityNew.makeBoundingBox().getYsize();

            BlockPos origin = null;
            Iterator<Stage> stages = this.stages.iterator();
            while(stages.hasNext())
            {
                Stage stage = stages.next();
                HashMap<BlockPos, BlockData> blocks = new HashMap<>();
                for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
                {
                    if(origin == null)
                        origin = entry.getKey();

                    BlockPos pos = entry.getKey().offset(0, (int) -height, 0);
                    BlockData data = entry.getValue();
                    data.pos = pos;
                    blocks.put(pos, data);
                }
                stage.blocks = blocks;
            }

            rocketEntityNew.setPos(this.getRocketEntity().position());
            this.getRocketEntity().setPos(this.getRocketEntity().position().add(0, height+2, 0));
            this.getRocketEntity().getRocket().setState(RocketState.COASTING);
            rocketEntityNew.getRocket().setState(RocketState.COASTING);
            rocketEntityNew.setDeltaMovement(this.getRocketEntity().getDeltaMovement().add(0, -0.8, 0));

            level.addFreshEntity(rocketEntityNew);
        }
    }

    public double getAltitude(Level level)
    {
        return rocket.position().y-level.getHeight(Heightmap.Types.MOTION_BLOCKING, rocket.blockPosition().getX(), rocket.blockPosition().getZ());
    }

    public CelestialBody getCelestialBody(Level level)
    {
        Registry<CelestialBody> registry = level.getServer().registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
        for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
        {
            if(entry.getValue().getDimension().equals(level.dimension()))
                return entry.getValue();
        }

        return null;
    }

    public double getSpaceHeight(Level level)
    {
        return (level.getMaxBuildHeight()-level.getMinBuildHeight())*2;
    }

    public boolean hasFuel()
    {
        Stage local = getCurrentStage();
        if(local == null)
            return false;

        for(Map.Entry<BlockPos, BlockData> entry : local.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData engineData)
            {
                if(engineData.hasFuel())
                    return true;
            }
        }

        return false;
    }

    public void setEngineThrust(double thrust)
    {
        for(Stage stage : this.stages)
            for(Map.Entry<BlockPos, BlockData > entry :stage.blocks.entrySet())
            {
                if(entry.getValue() instanceof RocketEngineData engine)
                {
                    engine.thrustPercentage = Math.min(1.0, thrust);
                }
            }
    }

    public Stage getCurrentStage()
    {
        Stage stage = null;
        for (Stage stageO : this.stages)
        {
            stage = stageO;
            break;
        }
        return stage;
    }

    public double getHoverThrust()
    {
        return (getMassKilogram()*9.80665)/(getMaxThrustKiloNewtons()*1000);
    }

    public void toggleEngines(boolean state)
    {
        Stage stage = getCurrentStage();
        if(stage == null) return;

        for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData engine)
            {
                engine.enabled = state;
            }
        }
    }

    public double getMaxTWR()
    {
        double thrust = getMaxThrustKiloNewtons()*1000;
        double mass = getMassKilogram()*9.80665;
        return thrust / mass;
    }

    public double getMaxThrustKiloNewtons()
    {
        double thrustkN = 0;
        Stage stage = getCurrentStage();
        if(stage == null) return 0;

        for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData engine)
            {
                thrustkN += engine.thrust_kN;
            }
        }
        return (thrustkN);
    }

    public void landingSimulation()
    {
        AtomicInteger takeOffFuel = new AtomicInteger(0);
        double takeoffDeltaV = takeoffSimulation(takeOffFuel);

        int fuelUsed = 0;
        int ticks = 0;
        Stage stage = getCurrentStage();

        double thrust = getMaxThrustKiloNewtons();
        double fuelFlow = getAverageFuelUsage();
        double mass = stage.getTotalMass()-takeOffFuel.get();
        double massAccounted = stage.getTotalMass()-takeOffFuel.get();

        double altitude = getSpaceHeight(rocket.level())-rocket.level().getHeight(Heightmap.Types.WORLD_SURFACE, rocket.blockPosition().getX(), rocket.blockPosition().getZ());
        double acceleration = 0;
        double velocity = 0;

        double safeLandingSpeed = -0.1;
        double g = 0.025;

        int ticksRan = 0;

        while(altitude > 0.25)
        {
            if(altitude < rocket.makeBoundingBox().getYsize() && velocity > safeLandingSpeed)
                break;

            velocity -= g;
            velocity = Mth.clamp(velocity, -4, 0);

            double twr = (thrust*1000)/(massAccounted*9.80665);
            double netAccelMax = (twr - 1.0) * g;

            double stoppingDistance = 0;
            if (netAccelMax > 0 && velocity < 0)
            {
                stoppingDistance = Math.max(rocket.makeBoundingBox().getYsize(), (velocity * velocity) / (2.0 * netAccelMax))*Math.max(1, 0.5*getMaxTWR());;
            }

            double thrustLevel = 0.0;
            if (altitude <= stoppingDistance + this.rocket.makeBoundingBox().getYsize())
            {
                double desiredVelocity = safeLandingSpeed;
                double error = desiredVelocity - velocity;

                double Kp = 0.5;
                double Kd = 0.2;

                double accelCmd = Kp * error - Kd * velocity;
                double thrustFraction = (g + accelCmd) / (twr * g);
                thrustLevel = Mth.clamp(thrustFraction, 0.0, 1.0);

                fuelUsed += (int) (fuelFlow * thrustLevel) * stage.getFuelTypeAmount() * getEngineAmount();
                massAccounted -= (int) (fuelFlow * thrustLevel) * stage.getFuelTypeAmount() * getEngineAmount();

                ticksRan++;
            }
            else thrustLevel = 0.0;

            acceleration = 0.025*twr*thrustLevel;
            velocity += acceleration;

            altitude += velocity;
            altitude = Math.max(0, altitude);

            ticks++;
        }

        double Isp = getAverageIsp();
        double massRatio = mass/(massAccounted);
        double deltaV = 9.8*Isp*Math.log(massRatio);

        System.out.println("Cursed ticks - " + ticksRan);
        System.out.println("Simulated landing fuel - " + fuelUsed);
        System.out.println("Simulated deltaV - " + takeoffDeltaV + " Landing - " + deltaV);
    }

    public double takeoffSimulation(AtomicInteger fuel)
    {
        int fuelUsed = 0;
        int ticks = 0;
        Stage stage = getCurrentStage();

        double thrust = getMaxThrustKiloNewtons();
        double height = this.getRocketEntity().makeBoundingBox().getYsize();
        double fuelFlow = getAverageFuelUsage();
        double mass = stage.getTotalMass();
        double massAccounted = stage.getTotalMass();
        double rocketMass = getMassKilogram();

        double altitude = 0;
        double acceleration = 0;
        double velocity = 0;
        double spaceY = getSpaceHeight(rocket.level())-rocket.getY();

        if(stage == null)
            return 0;

        while(altitude < spaceY)
        {
            velocity -= 0.025;
            double engineThrust = 0.0D;
            double hover = (rocketMass*9.80665)/(thrust*1000);

            if(altitude < height)
                engineThrust = hover*1.1;
            else
                engineThrust = hover*(altitude/height);

            engineThrust = Math.max(0, Math.min(engineThrust, 1));

            fuelUsed += (int) (fuelFlow * engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();
            massAccounted -= (int) (fuelFlow*engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();
            rocketMass -= (int) (fuelFlow*engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();

            double twr = (thrust*1000)/(rocketMass*9.80665);
            acceleration = 0.025*twr*engineThrust;

            velocity = Math.min(RocketEntity.MAX_SPEED_UP_BT, acceleration+velocity);
            velocity = Math.max(velocity, 0);
            altitude += velocity;
            altitude = Math.max(0, altitude);

            ticks++;
        }

        double Isp = getAverageIsp();
        double massRatio = mass/(massAccounted);
        double deltaV = 9.8*Isp*Math.log(massRatio);

        fuel.set(fuelUsed);

        System.out.println("Simulated time - " + ticks);
        System.out.println("Simulated takeoff fuel - " + fuelUsed);
        return deltaV;
    }

    public int getAverageFuelUsage()
    {
        int fuelUse = 0;
        int amount = 0;

        Stage current = getCurrentStage();

        for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData data)
            {
                fuelUse += data.calculateMaxFuelUsage();
                amount++;
            }
        }

        if(amount == 0) return 0;
        return fuelUse / amount;
    }

    public double getAverageIsp()
    {
        double Isp = 0;
        int amount = 0;

        Stage current = this.getCurrentStage();
        if(current == null) return 0;

        for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
        {
            if(entry.getValue() instanceof RocketEngineData data)
            {
                Isp += data.getIsp();
                amount++;
            }
        }
        if(amount == 0) return 0;
        return Isp / amount;
    }

    public double getMassKilogram()
    {
        double mass = 0;
        for(Stage stage : this.stages)
            mass += stage.getTotalMass();
        return mass;
    }

    public double getMassDryKilogram()
    {
        double mass = 0;
        for(Stage stage : this.stages)
            mass += stage.getTotalDryMass();
        return mass;
    }

    public int getEngineAmount()
    {
        int amount = 0;
        Stage stage = getCurrentStage();
        if(stage == null)
            return 0;

        for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
            if(entry.getValue() instanceof RocketEngineData)
                amount++;


        return amount;
    }

    public boolean canLand()
    {
        return getMaxTWR() > 1.0;
    }

    public RocketEntity getRocketEntity()
    {
        return rocket;
    }

    public RocketState getState()
    {
        return state;
    }

    public void setState(RocketState state)
    {
        this.state = state;
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.rocket.getId());
        buffer.writeEnum(this.state);
        buffer.writeCollection(stages, (writer, stage) -> stage.toNetwork(writer));
    }

    public static Rocket fromNetwork(FriendlyByteBuf buffer)
    {
        RocketEntity rocketEntity = ClientPacketHandler.getEntity(buffer.readInt());
        RocketState state = buffer.readEnum(RocketState.class);
        Rocket rocket = new Rocket(rocketEntity, new LinkedHashSet<>());
        LinkedHashSet<Stage> stages = buffer.readCollection(LinkedHashSet::new, reader -> Stage.fromNetwork(buffer, rocket));

        rocket.stages = stages;
        rocket.state = state;
        return rocket;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        tag.putString("state", state.toString().toLowerCase());

        ListTag stageTag = new ListTag();
        for(Stage stage : stages)
            stageTag.add(stage.save());
        tag.put("stages", stageTag);

        return tag;
    }

    public void load(CompoundTag tag)
    {
        this.state = RocketState.valueOf(tag.getString("state").toUpperCase());

        ListTag stageTag = tag.getList("stages", Tag.TAG_COMPOUND);
        LinkedHashSet<Stage> stages = new LinkedHashSet<>();
        for(Tag listTag : stageTag)
        {
            Stage stage = new Stage(this);
            stage.load((CompoundTag) listTag);
            stages.add(stage);
        }
        this.stages = stages;
    }
}
