package me.jetby.divinevulcan;

import lombok.*;
import me.jetby.divinevulcan.configurations.BossBars;
import me.jetby.divinevulcan.configurations.Items;
import me.jetby.divinevulcan.configurations.Vulcans;
import me.jetby.divinevulcan.utils.TextUtil;
import me.jetby.divinevulcan.utils.UtilHologram;
import me.jetby.divinevulcan.worldGuardHook.WGHook;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static me.jetby.divinevulcan.Main.activeVulcans;
import static me.jetby.divinevulcan.worldGuardHook.WGHook.removeRegion;

@Getter @Setter
@AllArgsConstructor
public class Vulcan {

    private final Main plugin;

    private final boolean mask;
    private final Map<String, Vulcans.Mask> masks;

    private final String type;
    private int minPlayers;
    private int duration;
    private int activation;
    private final Set<Material> materialSet;
    private final List<String> onSpawnEvents;
    private final List<String> onStartEvents;
    private final List<String> onEndEvents;
    private final List<String> hologramLines;
    private final List<String> hologramActivatedLines;
    private World spawnWorld;
    private int spawnRadiusMin;
    private int spawnRadiusMax;

    private final double radius;
    private final int period;
    private final int pickupDelay;
    private final boolean particleEnabled;
    private final boolean fired;
    private final String sound;
    private final double soundPitch;
    private final double soundVolume;
    private final String particleType;
    private final int particleAmount;
    private final double minY;
    private final double maxY;
    private final double minHorizontalSpeed;
    private final double maxHorizontalSpeed;

    private final boolean region;
    private int regionSize;
    private final List<String> flags;

    private final boolean schem;
    private final @Nullable File schemFile;
    private final boolean schemIgnoreAirBlocks;
    private final int schemOffsetX;
    private final int schemOffsetY;
    private final int schemOffsetZ;

    private final double holoOffsetX;
    private final double holoOffsetY;
    private final double holoOffsetZ;

    private boolean staticLocation;
    private final String staticLocationWorld;
    private final int staticLocationX;
    private final int staticLocationY;
    private final int staticLocationZ;

    private boolean usePreGeneratedLocations;

    private final boolean phase;
    private final Map<Integer, List<String>> phases;

    private final Map<String, BossBars> bossBarsMap;

    private Location spawnLocation;
    private static BukkitRunnable activationTask;
    private static BukkitRunnable startTask;
    private static BukkitRunnable particleTask;

    private final Random random = new Random();

    public void activation() {

        if (startTask !=null) {
            startTask.cancel();
        }
        if (activationTask != null) {
            activationTask.cancel();
        }
        final int[] timeLeft = {duration};

        plugin.getActions().execute(type, spawnLocation, onStartEvents, this);

        Vulcan vulcan = this;

        activationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft[0] <= 0) {
                    stop();
                    cancel();
                }

                if (!isPhase()) if (timeLeft[0] % period == 0) dropItem();

                if (isPhase()) {
                    for (Integer moment : phases.keySet()) {
                        if (timeLeft[0]!=moment) continue;
                        plugin.getActions().execute(type, spawnLocation, phases.get(moment), vulcan);
                    }
                }
                timeLeft[0]--;
                updateHologramActivation(timeLeft[0]);
            }
        };
        activationTask.runTaskTimer(plugin, 0, 20);
    }

    public void start() {
        if (startTask != null) return;

        Location generateLoc = getRandomSpawnLocation();

        if (generateLoc == null) return;

        final int[] timeLeft = {activation};

        spawnLocation = new Location(generateLoc.getWorld(),
                generateLoc.getX(),
                generateLoc.getY()+plugin.getSchematic().getMaxY(schemFile)-1,
                generateLoc.getZ());

        if (schem) {
            plugin.getSchematic().pasteSchematicAdvanced(spawnLocation,
                    schemFile,
                    this);
        }

        if (region) {
            WGHook.createRegion(spawnLocation, regionSize, flags);
        }

            plugin.getActions().execute(type, spawnLocation, onSpawnEvents, this);


        createHologramActivation(timeLeft[0]);
        startParticleEffects(activation);
        startTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft[0] <= 0) {
                    activation();
                } else {
                    timeLeft[0]--;
                    updateHologramStart(timeLeft[0]);

                }
            }
        };
        startTask.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        if (activationTask != null) {
            activationTask.cancel();
            activationTask = null;
        }
        if (startTask != null) {
            startTask.cancel();
            startTask = null;
        }
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        plugin.getSchematic().undoSchematic(this);
        clearHologram();

        for (String id : bossBarsMap.keySet()) {
            if (!plugin.getBossBar().getDatas().containsKey(id)) continue;
            plugin.getBossBar().deleteBossBar(id);
        }

        plugin.getActions().execute(type, spawnLocation, onEndEvents, this);

        removeRegion(spawnLocation);

        activeVulcans.remove(type, this);
    }

    private void startParticleEffects(int totalTime) {
        if (particleTask != null) {
            particleTask.cancel();
        }
        Location loc = new Location(spawnLocation.getWorld(), spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
        particleTask = new BukkitRunnable() {
            int timeLeft = totalTime*2;
            @Override
            public void run() {
                if (spawnLocation == null || timeLeft <= 0) {
                    cancel();
                    return;
                }
                spawnLocation.getWorld().spawnParticle(Particle.LAVA, loc, 50);
                timeLeft--;
            }
        };
        particleTask.runTaskTimerAsynchronously(plugin, 0, 10);
    }


    private Location getRandomSpawnLocation() {
        Location location = null;

        if (staticLocation) {
            location = new Location(
                    Bukkit.getWorld(staticLocationWorld),
                    staticLocationX,
                    staticLocationY,
                    staticLocationZ
            );

        } else {
            location = plugin.getLocationGenerator().findRandomLocation(this);
        }
        return location;
    }


    private void updateHologramStart(int timeLeft) {
        if (spawnLocation != null) {
            List<String> lines = new ArrayList<>(hologramLines);
            lines.replaceAll(line -> line.replace("{time}", String.valueOf(timeLeft)));
            String hologramName = "vulcan_" + spawnLocation.getBlockX() + "_" + spawnLocation.getBlockY() + "_" + spawnLocation.getBlockZ();
            UtilHologram.update(lines, spawnLocation.clone().add(holoOffsetX, holoOffsetY, holoOffsetZ), hologramName);
        }
    }

    private void createHologramActivation(int timeLeft) {
        if (spawnLocation != null) {
            List<String> lines = new ArrayList<>(hologramActivatedLines);
            lines.replaceAll(line -> line.replace("{time}", String.valueOf(timeLeft)));
            String hologramName = "vulcan_" + spawnLocation.getBlockX() + "_" + spawnLocation.getBlockY() + "_" + spawnLocation.getBlockZ();
            UtilHologram.create(lines, spawnLocation.clone().add(holoOffsetX, holoOffsetY, holoOffsetZ), hologramName);
        }
    }

    private void updateHologramActivation(int timeLeft) {
        if (spawnLocation != null) {
            List<String> lines = new ArrayList<>(hologramActivatedLines);
            lines.replaceAll(line -> line.replace("{time}", String.valueOf(timeLeft)));
            String hologramName = "vulcan_" + spawnLocation.getBlockX() + "_" + spawnLocation.getBlockY() + "_" + spawnLocation.getBlockZ();
            UtilHologram.update(lines, spawnLocation.clone().add(holoOffsetX, holoOffsetY, holoOffsetZ), hologramName);
        }
    }

    private void clearHologram() {
        if (spawnLocation != null) {
            String hologramName = "vulcan_" + spawnLocation.getBlockX() + "_" + spawnLocation.getBlockY() + "_" + spawnLocation.getBlockZ();
            UtilHologram.remove(hologramName);
        }
    }

    @Nullable
    public Location getLocation() {
        return spawnLocation;
    }

    public boolean isStarted() {
        return (startTask != null);
    }
    public boolean isActivation() {
        return activationTask !=null;
    }

    public void dropItem(ItemStack originalItem, Location location) {

        ItemStack itemToDrop;
        if (originalItem == null || location == null) {
            itemToDrop = new ItemStack(Material.DIAMOND);
        } else {
            itemToDrop = originalItem;
        }
        if (mask && !masks.isEmpty()) {
            List<String> maskKeys = new ArrayList<>(masks.keySet());
            String randomMaskKey = maskKeys.get(random.nextInt(maskKeys.size()));
            Vulcans.Mask randomMask = masks.get(randomMaskKey);

            ItemStack maskedItem = new ItemStack(randomMask.material());
            ItemMeta meta = maskedItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(TextUtil.colorize(randomMask.name()));
                if (randomMask.enchanted()) {
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                }
                NamespacedKey key = new NamespacedKey(plugin, "no_stack");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, UUID.randomUUID().toString());
                maskedItem.setItemMeta(meta);
            }
            itemToDrop = maskedItem;
        }
        Item droppedItem = location.getWorld().dropItemNaturally(location, itemToDrop);
        droppedItem.setPickupDelay(pickupDelay);
        droppedItem.setVisualFire(fired);
        if (mask) {
            if (itemToDrop.getItemMeta() != null && itemToDrop.getItemMeta().hasDisplayName()) {
                droppedItem.setCustomName(itemToDrop.getItemMeta().getDisplayName());
                droppedItem.setCustomNameVisible(true);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                droppedItem.setMetadata("originalItem", new FixedMetadataValue(plugin, originalItem));
            });
        }

        double targetY = location.getY() + random.nextInt((int) (random.nextInt((int) (maxY - minY + 1)) + minY));
        double vertSpeed = Math.sqrt(2 * 0.08 * (targetY - location.getY()));
        double angle = random.nextDouble() * Math.PI * 2;
        double horSpeed = minHorizontalSpeed + (maxHorizontalSpeed - minHorizontalSpeed) * random.nextDouble();
        Vector velocity = new Vector(Math.cos(angle) * horSpeed, vertSpeed, Math.sin(angle) * horSpeed);
        droppedItem.setVelocity(velocity);


        location.getWorld().playSound(location, Sound.valueOf(sound),
                (float) soundVolume,
                (float) soundPitch);
        new BukkitRunnable() {
            final Vector lastPosition = droppedItem.getLocation().toVector();
            @Override
            public void run() {
                if (!droppedItem.isValid()) {
                    cancel();
                    return;
                }
                Vector currentPosition = droppedItem.getLocation().toVector();
                Vector step = currentPosition.clone().subtract(lastPosition).multiply(1.0 / particleAmount);
                for (int i = 0; i < particleAmount; i++) {
                    Vector particlePos = lastPosition.clone().add(step.clone().multiply(i));
                    if (!particleEnabled) continue;
                    location.getWorld().spawnParticle(Particle.valueOf(particleType),
                            particlePos.toLocation(location.getWorld()), 0, 0, 0, 0, 0);
                }
                lastPosition.copy(currentPosition);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    public void dropItem() {
        dropItem(getRandomItem(), spawnLocation );
    }

    public void dropItem(Integer amount) {
        long time = 1L;
        for (int i = 0; i < amount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                dropItem( getRandomItem(), spawnLocation );
            }, time);
            time = time+1L;
        }
    }

    private ItemStack getRandomItem() {
        Random random = new Random();
        List<ItemStack> weightedItems = new ArrayList<>();
        List<Items.ItemsData> items = plugin.getItems().getData().get(type);
        if (items==null) {
            return new ItemStack(Material.DIAMOND);
        }
        for (Items.ItemsData itemData : items) {
            ItemStack item = itemData.itemStack();
            if (item == null) continue;

            int chance = itemData.chance();
            for (int i = 0; i < chance; i++) {
                weightedItems.add(item);
            }
        }
        return weightedItems.isEmpty() ? new ItemStack(Material.DIAMOND) :
                weightedItems.get(random.nextInt(weightedItems.size()));
    }
}