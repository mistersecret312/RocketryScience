package net.mistersecret312.compatability.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.init.CapabilityInit;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import java.text.NumberFormat;

public class RocketEngineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    public static final RocketEngineProvider INSTANCE = new RocketEngineProvider();
    public static final ResourceLocation ID = new ResourceLocation(RocketryScienceMod.MODID, "rocket_engine_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        NumberFormat fraction = NumberFormat.getNumberInstance();
        fraction.setParseIntegerOnly(false);
        fraction.setMaximumFractionDigits(0);
        fraction.setMinimumFractionDigits(0);
        fraction.setGroupingUsed(false);

        double thrust = accessor.getServerData().getDouble("thrust");
        int throttle = accessor.getServerData().getInt("throttle");
        double reliability = accessor.getServerData().getDouble("reliability");
        double integrity = accessor.getServerData().getDouble("integrity");
        double maxIntegrity = accessor.getServerData().getDouble("max_integrity");

        double throttledThrust = thrust*((double) throttle /15);

        tooltip.add(Component.translatable("data.rocketry_science.thrust", fraction.format(thrust*((double) throttle /15)), thrust));
        tooltip.add(Component.translatable("data.rocketry_science.reliability", String.format("%.0f%%", reliability*100)));
        tooltip.add(Component.translatable("data.rocketry_science.integrity", fraction.format(integrity), maxIntegrity));

    }

    @Override
    public ResourceLocation getUid()
    {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor)
    {
        RocketEngineBlockEntity rocketEngine = (RocketEngineBlockEntity) blockAccessor.getBlockEntity();
        rocketEngine.getLevel().getCapability(CapabilityInit.BLUEPRINTS_DATA).ifPresent(cap -> {
            RocketEngineBlueprint blueprint = cap.rocketEngineBlueprints.get(rocketEngine.getBlueprintID());
            tag.putDouble("thrust", blueprint.thrust_kN);
            tag.putDouble("integrity", rocketEngine.integrity);
            tag.putDouble("max_integrity", blueprint.maxIntegrity);
            tag.putDouble("reliability", rocketEngine.reliability);
            tag.putInt("throttle", rocketEngine.throttle);
        });
    }
}
