package net.mistersecret312.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelTimeAccess;
import net.mistersecret312.RocketryScienceMod;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.OrbitalMath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin
{
	@Inject(method = "getSkyDarken(F)F", at = @At("HEAD"), cancellable = true)
	public void skyLight(float pPartialTick, CallbackInfoReturnable<Float> cir)
	{
		// 1. Basic Day/Night Cycle
		float f = ((LevelTimeAccess) this).getTimeOfDay(pPartialTick);
		float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
		f1 = Mth.clamp(f1, 0.0F, 1.0F);
		f1 = 1.0F - f1;

		CelestialBody body = OrbitalMath.getCelestialBody(((ClientLevel) (Object) this).dimensionTypeId().location());

		// 2. Apply Weather (Only if atmosphere exists)
		if (body == null || !body.dimension.location().equals(
				ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna")))
		{
			f1 *= 1.0F - ((ClientLevel) (Object) this).getRainLevel(pPartialTick) * 5.0F / 16.0F;
			f1 *= 1.0F - ((ClientLevel) (Object) this).getThunderLevel(pPartialTick) * 5.0F / 16.0F;
		}

		// 3. Calculate Ambient Light Floor
		// Original code hardcoded this as 0.2F (meaning 20% minimum brightness)
		// Formula: return f1 * (1.0 - minAmbient) + minAmbient;

		float minAmbient;

		if (body != null && body.dimension.location().equals(
				ResourceLocation.fromNamespaceAndPath(RocketryScienceMod.MODID, "luna")))
		{
			// VACUUM: No light scattering.
			// Shadows are fully black. Night is fully black.
			minAmbient = 0.0F;
		} else {
			// ATMOSPHERE:
			// Earth default is 0.2F.
			// You can scale this by density. Example: Mars (0.1F), Venus (0.35F)
			minAmbient = 0.2F * 1;
		}

		// Ensure scaling is correct so we don't exceed 1.0F total brightness
		cir.setReturnValue(f1 * (1.0F - minAmbient) + minAmbient);
	}
}
