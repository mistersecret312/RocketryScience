package net.mistersecret312.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.datapack.CelestialBody;
import org.joml.Vector2d;

public class TransferData
{
	public enum TransferType {
		LOCAL_ORBIT,    // Same body, changing altitude (e.g., Earth 300km -> Earth 10000km)
		SATELLITE,      // Body to its moon (e.g., Earth -> Moon)
		INTERPLANETARY  // Different parent bodies (e.g., Earth -> Mars)
	}

	private final TransferType type;

	// --- Departure State ---
	private final ResourceLocation departureBody;
	public final double departureAltitude;
	public final double departureAngle; // Spacecraft's local angle around the body at departure
	public final long departureTime;

	// --- Arrival State ---
	private final ResourceLocation arrivalBody;
	public final double arrivalAltitude;
	public final double arrivalAngle;   // Spacecraft's local angle around the body at arrival
	public final long arrivalTime;

	// --- Transfer Context ---
	private final ResourceLocation referenceBody;
	public final double efficiency;
	public double progress;

	public TransferData(ResourceLocation departureBody, double departureAltitude, double departureAngle, long departureTime,
						ResourceLocation arrivalBody, double arrivalAltitude, double arrivalAngle, long arrivalTime,
						ResourceLocation referenceBody, double efficiency, double progress, TransferType type)
	{
		this.departureBody = departureBody;
		this.departureAltitude = departureAltitude;
		this.departureAngle = departureAngle;
		this.departureTime = departureTime;

		this.arrivalBody = arrivalBody;
		this.arrivalAltitude = arrivalAltitude;
		this.arrivalAngle = arrivalAngle;
		this.arrivalTime = arrivalTime;

		this.referenceBody = referenceBody;
		this.efficiency = efficiency;
		this.progress = progress;
		this.type = type;
	}

	// ... Standard Getters for all the final fields ...
	public TransferType getType() { return type; }
	public double getProgress() { return progress; }
	public void setProgress(double progress) { this.progress = progress; }

	public double getEfficiency()
	{
		return efficiency;
	}

	/**
	 * Calculates the true start position of the spacecraft relative to the reference body.
	 */
	public Vector2d getStartPosition(double zoom, double scale, Vector2d refScreenPos, RegistryAccess registryAccess)
	{
		// 1. Where is the spacecraft relative to its immediate parent?
		Vector2d localSpacecraftPos = getDepartureBody(registryAccess).getCoordinates(departureAltitude, departureAngle);

		// 2. Where is the parent body relative to the reference body?
		Vector2d parentPos = getBodyPositionRelativeToRef(departureBody, getDepartureBody(registryAccess), departureTime);

		// 3. The true position is the parent's position + the spacecraft's local offset
		Vector2d absoluteStart = parentPos.add(localSpacecraftPos);

		return absoluteStart.mul(zoom * scale).add(refScreenPos);
	}

	/**
	 * Calculates the true target position of the spacecraft relative to the reference body.
	 */
	public Vector2d getEndPosition(double zoom, double scale, Vector2d refScreenPos, RegistryAccess registryAccess)
	{
		// 1. Where will the spacecraft be relative to its immediate target parent?
		Vector2d localSpacecraftPos = getArrivalBody(registryAccess).getCoordinates(arrivalAltitude, arrivalAngle);

		// 2. Where will the target body be relative to the reference body at arrival time?
		Vector2d parentPos = getBodyPositionRelativeToRef(arrivalBody, getArrivalBody(registryAccess), arrivalTime);

		// 3. The true position is the target body's future position + the spacecraft's local offset
		Vector2d absoluteEnd = parentPos.add(localSpacecraftPos);

		return absoluteEnd.mul(zoom * scale).add(refScreenPos);
	}

	public Vector2d getRawStartPosition(RegistryAccess registryAccess)
	{
		CelestialBody departureBody = registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY).get(this.departureBody);
		Vector2d localSpacecraftPos = departureBody.getCoordinates(departureAltitude, departureAngle);
		Vector2d parentPos = getBodyPositionRelativeToRef(this.departureBody, departureBody, departureTime);

		return parentPos.add(localSpacecraftPos);
	}

	public Vector2d getRawEndPosition(RegistryAccess registryAccess)
	{
		CelestialBody arrivalBody = registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY).get(this.arrivalBody);
		Vector2d localSpacecraftPos = arrivalBody.getCoordinates(arrivalAltitude, arrivalAngle);
		Vector2d parentPos = getBodyPositionRelativeToRef(this.arrivalBody, arrivalBody, arrivalTime);

		return parentPos.add(localSpacecraftPos);
	}

	/**
	 * Helper method to prevent the planet's orbit from being calculated if it IS the reference body.
	 */
	private Vector2d getBodyPositionRelativeToRef(ResourceLocation bodyKey, CelestialBody body, long time)
	{
		// If the body IS the reference body (e.g., Earth-to-Earth transfer, or Earth-to-Moon),
		// it is at the center of the coordinate system (0,0).
		if (bodyKey.equals(referenceBody))
		{
			return new Vector2d(0, 0);
		}

		// Otherwise, calculate where this body is on its orbit at the given time.
		double orbitAngle = body.getOrbit().getAngle(time);
		return body.getCoordinates(body.getAltitude(), orbitAngle);
	}

	// ==========================================
	// NETWORK SERIALIZATION (FriendlyByteBuf)
	// ==========================================

	public void toNetwork(FriendlyByteBuf buf)
	{
		buf.writeEnum(this.type);

		buf.writeResourceLocation(this.departureBody);
		buf.writeDouble(this.departureAltitude);
		buf.writeDouble(this.departureAngle);
		buf.writeLong(this.departureTime);

		buf.writeResourceLocation(this.arrivalBody);
		buf.writeDouble(this.arrivalAltitude);
		buf.writeDouble(this.arrivalAngle);
		buf.writeLong(this.arrivalTime);

		buf.writeResourceLocation(this.referenceBody);
		buf.writeDouble(this.efficiency);
		buf.writeDouble(this.progress);
	}

	public static TransferData fromNetwork(FriendlyByteBuf buf)
	{
		TransferType type = buf.readEnum(TransferType.class);

		ResourceLocation depId = buf.readResourceLocation();
		double depAlt = buf.readDouble();
		double depAngle = buf.readDouble();
		long depTime = buf.readLong();

		ResourceLocation arrId = buf.readResourceLocation();
		double arrAlt = buf.readDouble();
		double arrAngle = buf.readDouble();
		long arrTime = buf.readLong();

		ResourceLocation refId = buf.readResourceLocation();
		double eff = buf.readDouble();
		double prog = buf.readDouble();

		return new TransferData(depId, depAlt, depAngle, depTime, arrId, arrAlt, arrAngle, arrTime, refId, eff, prog, type);
	}

	// ==========================================
	// NBT SERIALIZATION (CompoundTag)
	// ==========================================

	public CompoundTag save(CompoundTag tag)
	{
		tag.putString("Type", this.type.name());

		tag.putString("DepartureBody", this.departureBody.toString());
		tag.putDouble("DepartureAltitude", this.departureAltitude);
		tag.putDouble("DepartureAngle", this.departureAngle);
		tag.putLong("DepartureTime", this.departureTime);

		tag.putString("ArrivalBody", this.arrivalBody.toString());
		tag.putDouble("ArrivalAltitude", this.arrivalAltitude);
		tag.putDouble("ArrivalAngle", this.arrivalAngle);
		tag.putLong("ArrivalTime", this.arrivalTime);

		tag.putString("ReferenceBody", this.referenceBody.toString());
		tag.putDouble("Efficiency", this.efficiency);
		tag.putDouble("Progress", this.progress);

		return tag;
	}

	public static TransferData load(CompoundTag tag)
	{
		TransferType type = TransferType.valueOf(tag.getString("Type"));

		ResourceLocation depId = new ResourceLocation(tag.getString("DepartureBody"));
		double depAlt = tag.getDouble("DepartureAltitude");
		double depAngle = tag.getDouble("DepartureAngle");
		long depTime = tag.getLong("DepartureTime");

		ResourceLocation arrId = new ResourceLocation(tag.getString("ArrivalBody"));
		double arrAlt = tag.getDouble("ArrivalAltitude");
		double arrAngle = tag.getDouble("ArrivalAngle");
		long arrTime = tag.getLong("ArrivalTime");

		ResourceLocation refId = new ResourceLocation(tag.getString("ReferenceBody"));
		double eff = tag.getDouble("Efficiency");
		double prog = tag.getDouble("Progress");

		return new TransferData(depId, depAlt, depAngle, depTime, arrId, arrAlt, arrAngle, arrTime, refId, eff, prog, type);
	}

	public CelestialBody getDepartureBody(RegistryAccess registryAccess)
	{
		return registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY).get(departureBody);
	}

	public CelestialBody getArrivalBody(RegistryAccess registryAccess)
	{
		return registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY).get(arrivalBody);
	}

	public CelestialBody getReferenceBody(RegistryAccess registryAccess)
	{
		return registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY).get(referenceBody);
	}
}