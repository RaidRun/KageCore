package de.syscy.kagecore.entityregistry;

import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.HashMap;
import java.util.Map;

public class EntityRegistry {
    private static Map<String, EntityTypes<?>> registeredEntityMap = new HashMap<>();

    public static void init() {

    }

    public static void registerEntity(String entityName, String replacingName, EntityConstructor entityConstructor, EnumCreatureType creatureType) {
		/*Map<String, Type<?>> types = (Map<String, Type<?>>) DataConverterRegistry.a().getSchema(DataFixUtils.makeKey(SharedConstants.a().getWorldVersion())).findChoiceType(DataConverterTypes.ENTITY).types();
		types.put("minecraft:" + entityName, types.get("minecraft:" + replacingName));
		EntityTypes.a<net.minecraft.server.v1_16_R3.Entity> a = EntityTypes.a.a(entityConstructor, creatureType);
		registeredEntityMap.put(entityName.toLowerCase(), IRegistry.a(IRegistry.ENTITY_TYPE, entityName, a.a(entityName)));*/
    }

    public static org.bukkit.entity.Entity spawnEntity(String entityName, Location location) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        EntityTypes<?> type = registeredEntityMap.get(entityName.toLowerCase());

        if (type == null) {
            throw new IllegalArgumentException("Invalid custom entity name \"" + entityName + "\"! Already registered the entity?");
        }
/*
		net.minecraft.server.v1_16_R3.Entity nmsEntity = type.b(world, null, null, null, new BlockPosition(location.getX(), location.getY(), location.getZ()), EnumMobSpawn.MOB_SUMMONED, false, false);

		if(nmsEntity != null) {
			nmsEntity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

			world.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);

			if(nmsEntity instanceof EntityLiving) {
				((CraftLivingEntity) nmsEntity.getBukkitEntity()).setRemoveWhenFarAway(false);
			}

			return nmsEntity.getBukkitEntity();
		} else {
			KageCore.debugMessage("Could not spawn custom entity \"" + entityName + "\".");

			return null;
		}*/

        return null;
    }

    public interface EntityConstructor<T extends net.minecraft.server.v1_16_R3.Entity> extends EntityTypes.b<T> {
        T create(EntityTypes<T> type, World world);
    }
}