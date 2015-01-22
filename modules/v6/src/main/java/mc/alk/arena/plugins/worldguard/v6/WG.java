package mc.alk.arena.plugins.worldguard.v6;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.alk.arena.plugins.worldedit.WorldEditUtil;
import mc.alk.arena.plugins.worldguard.WorldGuardAbstraction;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * The WorldEdit v6.x implementation.
 * 
 * Why does this exist under the WorldGuard Utilities ?
 * Because intention of saveSchematic() is really saveRegion().
 * And the intention of pasteSchematic() is really resetRegion().
 *
 * @author Nikolai
 */
public class WG extends WorldGuardAbstraction {

    @Override
    public boolean saveSchematic(org.bukkit.entity.Player p, String schematicName) {
        WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
        final LocalSession session = wep.getSession(p);
        final Player player = wep.wrapPlayer(p);
        EditSession editSession = session.createEditSession(player);
        try {
            Region region = session.getSelection(player.getWorld());
            Clipboard cb = new BlockArrayClipboard(region);
            WorldData worldData = editSession.getWorld().getWorldData();
            ClipboardHolder clipboardHolder = new ClipboardHolder(cb, worldData);
            session.setClipboard(clipboardHolder);
            SchematicCommands sc = new SchematicCommands(wep.getWorldEdit());
            // sc.save() is deprecated
            sc.save(player, session, "mcedit", schematicName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Error: LocalPlayer bsc = new ConsolePlayer(); 
     */
    @Override
    public boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        final WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
        final WorldEdit we = wep.getWorldEdit();
        // LocalPlayer bcs = new ConsolePlayer(wep, wep.getServerInterface(), sender, world);
        Actor actor = wep.wrapCommandSender(sender);
        com.sk89q.worldedit.world.World w = new BukkitWorld(world);
        WorldData wd = w.getWorldData();
        final LocalSession session = wep.getWorldEdit().getSessionManager().get(actor);
        session.setUseInventory(false);
        // EditSession editSession = session.createEditSession(bcs);
        EditSession editSession = new EditSession((LocalWorld) w, -1);
        try {
            return loadAndPaste(schematic, we, session, wd, editSession, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This is just copied and pasted from world edit source, with small changes
     * to also paste.
     *
     * @param schematic String filename
     * @param we WorldEdit
     * @param session LocalSession
     * @param worldData WorldData
     * @param editSession EditSession
     * @param location Vector
     * @return
     * @throws com.sk89q.worldedit.util.io.file.FilenameException
     */
    public boolean loadAndPaste(String schematic, WorldEdit we,
            LocalSession session, WorldData worldData, EditSession editSession, Vector location) throws FilenameException {

        String filename = schematic + ".schematic";
        LocalConfiguration config = we.getConfiguration();

        File dir = we.getWorkingDirectoryFile(config.saveDir);
        // File f = we.getSafeOpenFile(player, dir, filename, "schematic", "schematic");
        File f = new File(dir, filename);

        if (!f.exists()) {
            System.out.println("Schematic " + filename + " does not exist!");
            return false;
        }

        ClipboardFormat fileFormat = ClipboardFormat.findByFile(f);
        ClipboardFormat aliasFormat = ClipboardFormat.findByAlias("mcedit");
        ClipboardFormat format = (fileFormat == null) ? aliasFormat : fileFormat;
        if (format == null) {
            System.out.println("Unknown schematic format for file " + f.getName());
            return false;
        }

        Closer closer = Closer.create();
        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                System.out.println("Clipboard file could not read or it does not exist.");
            } else {
                FileInputStream fis = closer.register(new FileInputStream(f));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                ClipboardReader reader = format.getReader(bis);
                Clipboard clipboard = reader.read(worldData);
                session.setClipboard(new ClipboardHolder(clipboard, worldData));
            }

            // WE v5 to v6 conversion:
            // session.getClipboard().paste(editSession, location, false, true); // WE v6 ERROR ***
            ClipboardHolder holder = session.getClipboard();
            Operation operation = holder
                    .createPaste(editSession, editSession.getWorld().getWorldData())
                    .to(location)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.completeLegacy(operation);
            // WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
        } catch (IOException e) {
            System.out.println("Schematic could not be read or it does not exist:");
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            System.out.println("MaxChangedBlocksException");
            e.printStackTrace();
        } catch (EmptyClipboardException ex) {
            Logger.getLogger(WG.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    @Override
    public void deleteRegion(String worldName, String id) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            return;
        }
        RegionManager mgr = wgp.getRegionManager(w);
        if (mgr == null) {
            return;
        }
        mgr.removeRegion(id);
    }
}
