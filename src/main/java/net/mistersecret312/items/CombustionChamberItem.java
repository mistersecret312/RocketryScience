package net.mistersecret312.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.mistersecret312.util.RocketFuel;
import net.mistersecret312.util.RocketMaterial;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombustionChamberItem extends Item
{

    public CombustionChamberItem(Properties properties)
    {
        super(properties);
    }

    public static ItemStack create(CombustionChamberItem item, RocketFuel fuel, RocketMaterial material)
    {
        ItemStack stack = new ItemStack(item);
        item.setFuelType(stack, fuel);
        item.setMaterial(stack, material);

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> components,
                                TooltipFlag pIsAdvanced)
    {
        RocketFuel fuelType = getFuelType(stack);
        RocketMaterial material = getMaterial(stack);
        if(fuelType == null || material == null)
            return;

        components.add(Component.translatable("desc.rocketry_science.rocket_fuel."+fuelType.getSerializedName()).withStyle(ChatFormatting.AQUA));
        components.add(Component.translatable("desc.rocketry_science.rocket_material."+material.getSerializedName()).withStyle(ChatFormatting.DARK_PURPLE));

        super.appendHoverText(stack, pLevel, components, pIsAdvanced);
    }

    @Nullable
    public RocketFuel getFuelType(ItemStack stack)
    {
        if(stack.getTag() != null && stack.getTag().contains("fuel_type"))
        {
            try
            {
                return RocketFuel.valueOf(stack.getTag().getString("fuel_type"));
            } catch (IllegalArgumentException e)
            {
                this.setFuelType(stack, RocketFuel.HYDROLOX);
                return RocketFuel.HYDROLOX;
            }
        }
        return null;
    }

    public void setFuelType(ItemStack stack, RocketFuel fuel)
    {
        stack.getOrCreateTag().putString("fuel_type", fuel.toString());
    }

    @Nullable
    public RocketMaterial getMaterial(ItemStack stack)
    {
        if(stack.getTag() != null && stack.getTag().contains("material"))
        {
            try
            {
                return RocketMaterial.valueOf(stack.getTag().getString("material"));
            } catch (IllegalArgumentException e)
            {
                this.setMaterial(stack, RocketMaterial.STAINLESS_STEEL);
                return RocketMaterial.STAINLESS_STEEL;
            }
        }
        return null;
    }

    public void setMaterial(ItemStack stack, RocketMaterial fuel)
    {
        stack.getOrCreateTag().putString("material", fuel.toString());
    }

}
