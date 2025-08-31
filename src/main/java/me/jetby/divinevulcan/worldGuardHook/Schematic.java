package me.jetby.divinevulcan.worldGuardHook;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.utils.Logger;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Schematic {

    private final Main pl;

    public Schematic(Main pl) {
        this.pl = pl;
    }

    private final Map<String, StoredPaste> editSessionMap = new HashMap<>();
    private static class StoredPaste {
        final EditSession session;
        final org.bukkit.World bukkitWorld;
        final int minX, minY, minZ, maxX, maxY, maxZ;

        StoredPaste(EditSession session, org.bukkit.World bukkitWorld,
                    int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.session = session;
            this.bukkitWorld = bukkitWorld;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }
    public void undoSchematic(Vulcan vulcan) {
        StoredPaste stored = editSessionMap.remove(vulcan.getType());
        if (stored == null) {
            Logger.warn("No edit session to undo for " + vulcan.getType());
            return;
        }

        World weWorld = BukkitAdapter.adapt(stored.bukkitWorld);
        EditSession undoSession = pl.getWorldEdit().newEditSession(weWorld);

        stored.session.undo(undoSession);
        undoSession.close();

        stored.session.close();

        int minChunkX = stored.minX >> 4;
        int maxChunkX = stored.maxX >> 4;
        int minChunkZ = stored.minZ >> 4;
        int maxChunkZ = stored.maxZ >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                stored.bukkitWorld.refreshChunk(cx, cz);
            }
        }

    }

    public void pasteSchematicAdvanced(Location loc, @Nullable File schematicFile, Vulcan vulcan) {

        if (schematicFile==null) {
            Logger.error("Schematic file is null");
            return;
        }
        if (!schematicFile.exists()) {
            Logger.warn("Schematic file not found: " + schematicFile.getName());
            return;
        }

        try {
            Vector offsets = new Vector(
                    vulcan.getSchemOffsetX(),
                    vulcan.getSchemOffsetY(),
                    vulcan.getSchemOffsetZ()
            );
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));


            Clipboard clipboard = reader.read();


            World adaptedWorld = BukkitAdapter.adapt(vulcan.getSpawnWorld());
            EditSession editSession = pl.getWorldEdit().newEditSession(adaptedWorld);

            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(loc.getX() + offsets.getBlockX(), loc.getY() + offsets.getBlockY(), loc.getZ() + offsets.getBlockZ())).ignoreAirBlocks(vulcan.isSchemIgnoreAirBlocks()).build();

            Operations.complete(operation);
            editSession.close();
            int minX = loc.getBlockX() + offsets.getBlockX();
            int minY = loc.getBlockY() + offsets.getBlockY();
            int minZ = loc.getBlockZ() + offsets.getBlockZ();

            int maxX = minX + clipboard.getDimensions().getBlockX() - 1;
            int maxY = minY + clipboard.getDimensions().getBlockY() - 1;
            int maxZ = minZ + clipboard.getDimensions().getBlockZ() - 1;

            StoredPaste stored = new StoredPaste(editSession, vulcan.getSpawnWorld(),
                    minX, minY, minZ, maxX, maxY, maxZ);
            editSessionMap.put(vulcan.getType(), stored);

        } catch (IOException | WorldEditException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    public int getMaxY( @Nullable File schematicFile) {
        if (schematicFile==null) {
            Logger.error("Schematic file is null");
            return -1;
        }
        if (!schematicFile.exists()) {
            Logger.warn("Schematic file not found: " + schematicFile.getName());
            return -1;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            return clipboard.getDimensions().getBlockY();
        } catch (IOException e) {
            return -1;
        }
    }

}
