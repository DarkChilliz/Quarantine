package me.DDoS.Quarantine.zone.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author DDoS
 */
public class SpawnLocation extends BlockLocation {

    private EntityType creatureType;

    public SpawnLocation(World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    public void setCreatureType(EntityType creatureType) {
        this.creatureType = creatureType;
    }

    public LivingEntity spawnCreature() {
        if (!creatureType.isAlive()) {
            throw new IllegalArgumentException("EntityType " + creatureType + " is not a LivingEntity!");
        }

        Entity entity = world.spawn(new Location(world, x, y, z), creatureType.getEntityClass());

        if (!(entity instanceof LivingEntity living)) {
            throw new IllegalStateException("Spawned entity is not a LivingEntity!");
        }

        return living;
    }

}

