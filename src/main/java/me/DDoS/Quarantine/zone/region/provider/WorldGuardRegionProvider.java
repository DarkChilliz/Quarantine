package me.DDoS.Quarantine.zone.region.provider;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.DDoS.Quarantine.zone.location.BlockLocation;
import me.DDoS.Quarantine.zone.region.Region;
import me.DDoS.Quarantine.zone.region.SpawnRegion;
import org.bukkit.World;

public class WorldGuardRegionProvider implements RegionProvider {

    private final com.sk89q.worldguard.protection.regions.RegionContainer regionContainer;

    public WorldGuardRegionProvider() {
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public Region getRegion(World world, String regionName) {
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return null;

        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) return null;

        BlockVector3 maxVec = region.getMaximumPoint();
        BlockVector3 minVec = region.getMinimumPoint();

        BlockLocation max = new BlockLocation(world, maxVec.getX(), maxVec.getY(), maxVec.getZ());
        BlockLocation min = new BlockLocation(world, minVec.getX(), minVec.getY(), minVec.getZ());

        return new Region(world, max, min);
    }

    @Override
    public SpawnRegion getSpawnRegion(World world, String regionName) {
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return null;

        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) return null;

        BlockVector3 maxVec = region.getMaximumPoint();
        BlockVector3 minVec = region.getMinimumPoint();

        BlockLocation max = new BlockLocation(world, maxVec.getX(), maxVec.getY(), maxVec.getZ());
        BlockLocation min = new BlockLocation(world, minVec.getX(), minVec.getY(), minVec.getZ());

        return new SpawnRegion(world, max, min);
    }
}
