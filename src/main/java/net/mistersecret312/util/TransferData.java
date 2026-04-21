package net.mistersecret312.util;

import net.minecraft.resources.ResourceKey;
import net.mistersecret312.datapack.CelestialBody;
import org.joml.Vector2d;

public class TransferData
{
	private final CelestialBody departureBody;
	private final CelestialBody arrivalBody;
	private final ResourceKey<CelestialBody> referenceBody;

	private final long departureTime;
	private final long arrivalTime;
	private final double efficiency;

	private double progress;

	public TransferData(CelestialBody departureBody, CelestialBody arrivalBody, ResourceKey<CelestialBody> referenceBody,
						long departureTime, long arrivalTime, double efficiency,
						double progress)
	{
		this.departureBody = departureBody;
		this.arrivalBody = arrivalBody;
		this.referenceBody = referenceBody;

		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.efficiency = efficiency;

		this.progress = progress;
	}

	public ResourceKey<CelestialBody> getReferenceBody()
	{
		return referenceBody;
	}

	public double getEfficiency()
	{
		return efficiency;
	}

	public double getProgress()
	{
		return progress;
	}

	public void setProgress(double progress)
	{
		this.progress = progress;
	}

	public Vector2d getStartPosition(double zoom, double scale, Vector2d refScreenPos)
	{
		Vector2d result = departureBody.getCoordinates(departureBody.getAltitude(),
				departureBody.getOrbit().getAngle(departureTime));
		return result.mul(zoom*scale).add(refScreenPos);
	}

	public Vector2d getEndPosition(double zoom, double scale, Vector2d refScreenPos)
	{
		Vector2d result = arrivalBody.getCoordinates(arrivalBody.getAltitude(),
				arrivalBody.getOrbit().getAngle(arrivalTime));
		return result.mul(zoom*scale).add(refScreenPos);
	}}
