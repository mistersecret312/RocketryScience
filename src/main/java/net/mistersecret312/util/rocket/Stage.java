package net.mistersecret312.util.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage
{
    public HashMap<BlockPos,BlockState> blockStates;
    public List<RocketEngineHandler> rocketEngines;
    @Nullable
    public List<FluidStack> fluidStacks;
    public List<Map.Entry<Fluid, Integer>> maxFluids;
    public int solidFuel;
    public int maxSolidFuel;

    public Stage(HashMap<BlockPos, BlockState> blockStates, List<RocketEngineHandler> rocketEngines,
                 List<FluidStack> fluidStacks, List<Map.Entry<Fluid, Integer>> maxFluids,
                 int solidFuel, int maxSolidFuel)
    {
        this.blockStates = blockStates;
        this.rocketEngines = rocketEngines;
        this.fluidStacks = fluidStacks;
        this.maxFluids = maxFluids;
        this.solidFuel = solidFuel;
        this.maxSolidFuel = maxSolidFuel;
    }
}
