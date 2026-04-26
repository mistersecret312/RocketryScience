package net.mistersecret312.data;

import net.minecraft.resources.ResourceKey;
import net.mistersecret312.datapack.CelestialBody;
import net.mistersecret312.util.TransferData;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class ClientOrbits
{
	public static List<ClientOrbit> ORBITS = new ArrayList<>();

	public static class ClientOrbit
	{
		public double epoch;
		public double orbitalAltitude;
		public double orbitalPeriod;

		public boolean isArtifical;

		public ResourceKey<CelestialBody> parent;
		public TransferData transferData;

		public ClientOrbit(double epoch, double orbitalAltitude, double orbitalPeriod,
						   ResourceKey<CelestialBody> parent, boolean isArtifical, TransferData transferData)
		{
			this.epoch = epoch;
			this.orbitalAltitude = orbitalAltitude;
			this.orbitalPeriod = orbitalPeriod;

			this.isArtifical = isArtifical;

			this.parent = parent;
			this.transferData = transferData;
		}

		public Vector2d getCoordinates(double altitude, double angle)
		{
			double radians = Math.toRadians(angle);

			double x = altitude * Math.cos(radians);
			double y = altitude * Math.sin(radians);

			return new Vector2d(x, y);
		}

		public double getAngle(long time)
		{
			double period = orbitalPeriod*20*20*60;

			double velocity = (360D/period);
			double angle = velocity*time + epoch;
			return angle % 360D;
		}

		public TransferData getTransferData()
		{
			return transferData;
		}
	}
}
