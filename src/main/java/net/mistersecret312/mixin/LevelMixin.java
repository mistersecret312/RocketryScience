package net.mistersecret312.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.OrbitalMath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin
{
	@Shadow @Final private ResourceKey<DimensionType> dimensionTypeId;

	@Shadow public abstract float getRainLevel(float pDelta);

	@Shadow public abstract float getThunderLevel(float pDelta);

	@Shadow private int skyDarken;

	@Inject(method = "updateSkyBrightness()V", at = @At("HEAD"), cancellable = true)
	public void skyBrightness(CallbackInfo ci)
	{
		double weatherFactor = 1.0D;

		CelestialBody body = OrbitalMath.getCelestialBody(this.dimensionTypeId.location());
		if (body != null && body.dimension.location().equals(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna")))
		{
			double rain = 1.0D - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0D;
			double thunder = 1.0D - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
			weatherFactor = rain * thunder;
		}

		// 2. Calculate Sun Angle (Day/Night Cycle)
		// This remains the same: standard cosine cycle
		double sunHeight = Mth.cos(((LevelTimeAccess) this).getTimeOfDay(1.0F) * ((float)Math.PI * 2F));
		double dayLightFactor = 0.5D + 2.0D * Mth.clamp(sunHeight, -0.25D, 0.25D);

		// 3. Calculate Final Darkness
		// Original Code: (1.0D - d2 * d0 * d1) * 11.0D;
		// The '11.0D' implies the sky never gets darker than light level 4 (15 - 11).
		// We want space to get fully dark (multiply by 15, not 11).

		double maxDarkness;
		if (body == null || !body.dimension.location().equals(ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna")))
		{
			// Earth-like: Standard scaling (Max darkness around 11)
			// You can adjust 11.0D based on atmosphere density if you want
			maxDarkness = 11.0D;
		}
		else
		{
			// Vacuum: Pitch black shadows/night (Max darkness 15)
			maxDarkness = 15.0D;
		}

		this.skyDarken = (int)((1.0D - dayLightFactor * weatherFactor) * maxDarkness);

		ci.cancel();
	}
}
