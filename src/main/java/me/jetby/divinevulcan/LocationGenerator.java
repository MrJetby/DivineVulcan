package me.jetby.divinevulcan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.jetby.divinevulcan.utils.Logger;
import me.jetby.divinevulcan.worldGuardHook.WGHook;
import org.bukkit.*;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.*;

@Getter @RequiredArgsConstructor
public class LocationGenerator {

    private final Main plugin;
    private final PreLocationGenerator preLocationGenerator;

    private final Random RANDOM = new Random();

    private int getHighestBlock(Chunk chunk, int x, int z, Set<Material> materialSet) {
        int y = chunk.getChunkSnapshot().getHighestBlockYAt(x, z);
        if (y <= 0) return -1;

        Material blockType = chunk.getBlock(x, y, z).getType();

        if (materialSet != null && materialSet.contains(blockType)) {
            if (plugin.getCfg().isDebug()) {
                Logger.error("[getHighestBlock] запрещённый блок: " + blockType.name() + " на " + chunk.getX() + "," + chunk.getZ() + " локально " + x + "," + z);
            }
            return -1;
        }

        return y;
    }


    @Nullable
    public Location findRandomLocation(Vulcan vulcan) {

        Collection<Location> locs = preLocationGenerator.getLocations().get(vulcan.getType());
        List<Location> locationList = (locs != null) ? new ArrayList<>(locs) : new ArrayList<>();

        if (vulcan.isUsePreGeneratedLocations() && !locationList.isEmpty()) {
            for (Location location : locationList) {
                if (location==null) continue;
                if (!WGHook.isRegionEmpty(vulcan.getRegionSize(), location)) continue;
                return location;
            }
        } else {
            return getRandomLocation(vulcan);
        }

        return null;
    }


    public boolean checkFlatSurface(Location center, int radius, int maxDiff, int requiredClearance) {
        World world = center.getWorld();
        if (world == null) return false;

        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int groundY = center.getBlockY() - 1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = cx + dx;
                int wz = cz + dz;

                int surfaceY = world.getHighestBlockYAt(wx, wz);
                if (surfaceY <= 0) {
                    if (plugin.getCfg().isDebug()) {
                        Logger.error("[checkFlatSurface] surfaceY <= 0 at " + wx + "," + wz);
                    }
                    return false;
                }

                if (Math.abs(surfaceY - groundY) > maxDiff) {
                    if (plugin.getCfg().isDebug()) {
                        Logger.error("[checkFlatSurface] слишком большой перепад высот: centerY=" + groundY + " surfaceY=" + surfaceY + " at " + wx + "," + wz);
                    }
                    return false;
                }

                Block groundBlock = world.getBlockAt(wx, surfaceY, wz);
                if (groundBlock.getType().equals(Material.GRASS) || groundBlock.getType().equals(Material.TALL_GRASS)) continue;
                if (groundBlock.isPassable() || groundBlock.getType() == Material.AIR
                        || groundBlock.getType() == Material.WATER || groundBlock.getType() == Material.LAVA) {
                    if (plugin.getCfg().isDebug()) {
                        Logger.error("[checkFlatSurface] неподходящий groundBlock: " + groundBlock.getType() + " at " + wx + "," + surfaceY + "," + wz);
                    }
                    return false;
                }

                for (int h = 1; h <= requiredClearance; h++) {
                    Block above = world.getBlockAt(wx, surfaceY + h, wz);
                    if (!above.isEmpty()) {
                        if (plugin.getCfg().isDebug()) {
                            Logger.error("[checkFlatSurface] нет свободного пространства над surface: " + above.getType() + " at " + wx + "," + (surfaceY + h) + "," + wz);
                        }
                        return false;
                    }
                }
            }
        }

        return true;
    }
    public boolean isSuitableFlat(Location center, int radius) {
        int maxDiff = 0;
        int requiredClearance = 2;
        return checkFlatSurface(center, radius, maxDiff, requiredClearance);
    }


    @Nullable
    public Location getRandomLocation(Vulcan vulcan) {

        World world = Bukkit.getWorld(vulcan.getSpawnWorld());
        Set<Material> materials = vulcan.getMaterialSet();
        int minRadius = vulcan.getSpawnRadiusMin();
        int maxRadius = vulcan.getSpawnRadiusMax();
        int regionSize = vulcan.getRegionSize();

        if (world == null) {
            if (plugin.getCfg().isDebug()) {
                Logger.error("[getRandomLocation] world возвращает null");
            }
            return null;
        }

        if (minRadius < 0) {
            minRadius = 0;
        }
        if (maxRadius <= minRadius) {
            maxRadius = minRadius + 1;
        }

        for (int attempts = 0; attempts < 30; attempts++) {
            int radius = minRadius + RANDOM.nextInt(maxRadius - minRadius + 1);

            if (radius <= 0) {
                radius = 1;
            }

            int x = RANDOM.nextInt(radius * 2 + 1) - radius;
            int z = RANDOM.nextInt(radius * 2 + 1) - radius;

            int wx = x;
            int wz = z;

            Chunk chunk = world.getChunkAt(wx >> 4, wz >> 4);
            int localX = wx & 15;
            int localZ = wz & 15;

            int y = getHighestBlock(chunk, localX, localZ, materials);

            if (y == -1) {
                if (plugin.getCfg().isDebug()) {
                    Logger.error("[getRandomLocation] getHighestBlock возвращает -1");
                }
                continue;
            }

            Location location = new Location(world, wx + 0.5, y + 1, wz + 0.5);

            if (!isSuitableFlat(location, regionSize)) {
                if (plugin.getCfg().isDebug()) {
                    Logger.error("[getRandomLocation] isSuitableFlat вернул false для " + location);
                }
                continue;
            }

            if (!WGHook.isRegionEmpty(regionSize, location)) {
                if (plugin.getCfg().isDebug()) {
                    Logger.error("[getRandomLocation] " + location + " - в этом месте есть регион");
                }
                continue;
            }
            if (plugin.getCfg().isDebug()) {
                Logger.success("[getRandomLocation] найдено подходящее место: " + location);
            }
            return location;
        }
        if (plugin.getCfg().isDebug()) {
            Logger.error("[getRandomLocation] null (не удалось найти)");
        }
        return null;
    }
}
