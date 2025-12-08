package net.mistersecret312.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.datapack.CelestialBody;

import java.util.LinkedHashSet;

public class OrphanObject
{
	private static final String SPACE_OBJECT = "space_object";
	private static final String IS_ARTIFICIAL = "artificial";

	public SpaceObject object;

	public OrphanObject(SpaceObject object)
	{
		this.object = object;
	}

	public void tick()
	{
		this.object.tick();
	}

	public CompoundTag save(Level level)
	{
		CompoundTag tag = new CompoundTag();

		tag.putBoolean(IS_ARTIFICIAL, isArtificial());
		tag.put(SPACE_OBJECT, object.save(level));

		return tag;
	}

	public static OrphanObject load(Level level, CompoundTag tag)
	{
		boolean artificial = tag.getBoolean(IS_ARTIFICIAL);
		SpaceObject object;
		if(artificial)
		{
			object = new SpaceCraft(new LinkedHashSet<>(), level);
			object.load(level, tag.getCompound(SPACE_OBJECT));
		}
		else
		{
			CompoundTag compoundTag = tag.getCompound(SPACE_OBJECT);
			ResourceLocation key = ResourceLocation.parse(compoundTag.getString("key"));
			object = level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY).get(key);
		}

		return new OrphanObject(object);
	}

	public SpaceObject getObject()
	{
		return object;
	}

	public boolean isArtificial()
	{
		return object instanceof SpaceCraft;
	}
}
