package me.jetby.divinevulcan.worldGuardHook;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.utils.Logger;
import org.bukkit.Location;

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

    private final Map<String, EditSession> editSessionMap = new HashMap<>();

    public void undoSchematic(Location location) {
        EditSession session = editSessionMap.get(makeKey(location));
        if (session != null) {
            session.undo(session);
            editSessionMap.remove(makeKey(location));
            session.close();
        } else {
            Logger.warn("No edit session to undo for LOCATION: " + makeKey(location));
        }
    }

    private String makeKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public void pasteSchematicAdvanced(Location location, String schematicFileName,
                                       boolean ignoreAirBlocks, int offsetX, int offsetY, int offsetZ) {
        File schematicFile = new File(pl.getDataFolder() + "/schematics", schematicFileName);
        if (!schematicFile.exists()) {
            Logger.warn("Schematic file not found: " + schematicFileName);
            return;
        }

        try {
            ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile));

            Clipboard clipboard = reader.read();
            World adaptedWorld = BukkitAdapter.adapt(location.getWorld());

            Location pasteLoc = location.clone().add(offsetX, offsetY, offsetZ);
            BlockVector3 to = BlockVector3.at(pasteLoc.getBlockX(), pasteLoc.getBlockY(), pasteLoc.getBlockZ());

            EditSession editSession = pl.getWorldEdit().newEditSession(adaptedWorld);
            editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(to)
                    .ignoreAirBlocks(ignoreAirBlocks)
                    .build();

            Operations.complete(operation);
            editSession.flushSession();

            editSessionMap.put(makeKey(location), editSession);
            reader.close();

        } catch (IOException | com.sk89q.worldedit.WorldEditException e) {
            e.printStackTrace();
        }
    }


    public int getMaxY(String schematicFileName) {
        File schematicFile = new File(pl.getDataFolder()+"/schematics", schematicFileName);
        if (!schematicFile.exists()) {
            Logger.warn("Schematic file not found: " + schematicFileName);
            return -1;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            return clipboard.getDimensions().getBlockY();
        } catch (IOException e) {
            return -1;
        }
    }

    public int getSize(String schematicFileName) {
        File schematicFile = new File(pl.getDataFolder()+"/schematics", schematicFileName);
        if (!schematicFile.exists()) {
            Logger.warn("Schematic file not found: " + schematicFileName);
            return -1;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            BlockVector3 dimensions = clipboard.getDimensions();
            return Math.max(Math.max(dimensions.getBlockX(), dimensions.getBlockY()), dimensions.getBlockZ());
        } catch (IOException e) {
            return -1;
        }
    }
}
