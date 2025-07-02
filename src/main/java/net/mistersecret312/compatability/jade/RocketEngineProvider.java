package net.mistersecret312.compatability.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.block_entities.RocketEngineBlockEntity;
import net.mistersecret312.blocks.NozzleBlock;
import net.mistersecret312.blueprint.RocketEngineBlueprint;
import net.mistersecret312.init.CapabilityInit;
import net.mistersecret312.mishaps.MishapType;
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
        if(accessor.getServerData().getBoolean("is_built"))
        {
            boolean isLiquid = accessor.getServerData().getBoolean("is_liquid_rocket_engine");
            if(isLiquid)
            {
                IElement nameElement = tooltip.getElementHelper().text(Component.translatable("rocket.rocketry_science.steel_rocket_engine").withStyle(ChatFormatting.WHITE));
                tooltip.get(0, IElement.Align.LEFT).set(0, nameElement);
            }

            NumberFormat fraction = NumberFormat.getNumberInstance();
            fraction.setParseIntegerOnly(false);
            fraction.setMaximumFractionDigits(0);
            fraction.setMinimumFractionDigits(0);
            fraction.setGroupingUsed(false);

            boolean minorMishap = accessor.getServerData().getBoolean("has_minor_mishap");
            double thrust = accessor.getServerData().getDouble("thrust");
            double mass = accessor.getServerData().getDouble("mass");
            int throttle = accessor.getServerData().getInt("throttle");
            double reliability = accessor.getServerData().getDouble("reliability");
            double integrity = accessor.getServerData().getDouble("integrity");
            double maxIntegrity = accessor.getServerData().getDouble("max_integrity");

            tooltip.add(Component.translatable("data.rocketry_science.mass", fraction.format(mass)));
            tooltip.add(Component.translatable("data.rocketry_science.thrust", fraction.format(thrust * ((double) throttle / 15)), thrust));
            tooltip.add(Component.translatable("data.rocketry_science.reliability", String.format("%.0f%%", reliability * 100)));
            tooltip.add(Component.translatable("data.rocketry_science.integrity", fraction.format(integrity), maxIntegrity));

            if(minorMishap)
                tooltip.add(Component.translatable("data.rocketry_science.minor_mishap").withStyle(ChatFormatting.RED));
        }
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
            if(rocketEngine.getNozzle().getBlock() instanceof NozzleBlock nozzle)
                tag.putBoolean("is_liquid_rocket_engine", nozzle.isLiquidPropellant());

            tag.putBoolean("is_built", rocketEngine.isBuilt);
            tag.putBoolean("has_minor_mishap", rocketEngine.mishaps.stream().anyMatch(mishap -> mishap.getType().category.equals(MishapType.MishapCategory.MINOR)));
            tag.putBoolean("has_major_mishap", rocketEngine.mishaps.stream().anyMatch(mishap -> mishap.getType().category.equals(MishapType.MishapCategory.MAJOR)));
            tag.putDouble("thrust", blueprint.thrust_kN);
            tag.putDouble("mass", blueprint.mass);
            tag.putDouble("integrity", rocketEngine.integrity);
            tag.putDouble("max_integrity", blueprint.maxIntegrity);
            tag.putDouble("reliability", rocketEngine.reliability);
            tag.putInt("throttle", rocketEngine.throttle);
        });
    }
}
