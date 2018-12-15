package mc.alk.arena.plugins.worldguard.v7;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.alk.worldeditutil.controllers.WorldEditController;
import mc.alk.worldeditutil.math.BlockSelection;
import mc.alk.worldeditutil.math.BlockVector;
import mc.alk.worldeditutil.WorldGuardAbstraction;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The WorldEdit v7.x implementation.
 *
 * Why does this exist under the WorldGuard Utilities ?
 * Because intention of saveSchematic() is really saveRegion().
 * And the intention of pasteSchematic() is really resetRegion().
 *
 * @author Alkarinv, Europia79, Paaattiii
 */
public class WG extends WorldGuardAbstraction {

    protected final WorldGuard wg = WorldGuard.getInstance();

    @Override
    public boolean saveSchematic(org.bukkit.entity.Player p, String schematicName) {
        WorldEditPlugin wep = WorldEditController.getWorldEditPlugin();
        LocalSession session = wep.getSession(p);
        com.sk89q.worldedit.entity.Player player = wep.wrapPlayer(p);
        EditSession editSession = session.createEditSession(player);
        Closer closer = Closer.create();

        try {
            Region region = session.getSelection(player.getWorld());
            Clipboard cb = new BlockArrayClipboard(region);
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
            Operations.complete(copy);
            LocalConfiguration config = wep.getWorldEdit().getConfiguration();
            File dir = wep.getWorldEdit().getWorkingDirectoryFile(config.saveDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Could not create directory " + config.saveDir);
                }
            }
            File schematicFile = new File(dir, schematicName + ".schematic");
            schematicFile.createNewFile();

            FileOutputStream fos = closer.register(new FileOutputStream(schematicFile));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(bos));
            writer.write(cb);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        } catch (WorldEditException e) {
            e.printStackTrace();
        } finally {
            try {
                closer.close();
                editSession.flushSession();
            } catch (IOException ignore) {
            }
        }
        return false;
    }

    @Override
    public Region getWorldEditRegion(Player p) {
        WorldEditPlugin wep = WorldEditController.getWorldEditPlugin();
        final LocalSession session = wep.getSession(p);
        final BukkitPlayer lPlayer = wep.wrapPlayer(p);

        try {
            return session.getSelection(lPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BlockSelection getBlockSelection(Region region) {
        World world = BukkitAdapter.adapt(region.getWorld());

        Location min = new Location(world, region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
        Location max = new Location(world, region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());

        return new BlockSelection(world, min, max);
    }

    @Override
    public BlockSelection getBlockSelection(World world, ProtectedRegion region) {
        return new BlockSelection(world, BukkitAdapter.adapt(world, region.getMinimumPoint()), BukkitAdapter.adapt(world, region.getMaximumPoint()));
    }

    /**
     * Error: LocalPlayer bsc = new ConsolePlayer();
     */
    @Override
    public boolean pasteSchematic(CommandSender sender, BlockVector position, String schematic, World world) {
        return pasteSchematic(sender, BlockVector3.at(position.x, position.y, position.z), schematic, world);
    }

    private boolean pasteSchematic(CommandSender sender, BlockVector3 position, String schematic, World world) {
        final WorldEditPlugin wep = WorldEditController.getWorldEditPlugin();
        final WorldEdit we = wep.getWorldEdit();
        // LocalPlayer bcs = new ConsolePlayer(wep, wep.getServerInterface(), sender, world);
        Actor actor = wep.wrapCommandSender(sender);
        com.sk89q.worldedit.world.World w = new BukkitWorld(world);
        final LocalSession session = wep.getWorldEdit().getSessionManager().get(actor);
        session.setUseInventory(false);
        // EditSession editSession = session.createEditSession(bcs);
        EditSession editSession = we.getEditSessionFactory().getEditSession(w, -1);
        try {
            return loadAndPaste(schematic, we, session, editSession, position);
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
     * @param editSession EditSession
     * @param location Vector
     * @return
     * @throws com.sk89q.worldedit.util.io.file.FilenameException
     */
    public boolean loadAndPaste(String schematic, WorldEdit we,
                                LocalSession session, EditSession editSession, BlockVector3 location) throws FilenameException {

        LocalConfiguration config = we.getConfiguration();

        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f = we.getSafeOpenFile(null, dir, schematic, BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension(), ClipboardFormats.getFileExtensionArray());

        if (!f.exists()) {
            System.out.println("Schematic " + schematic + " does not exist!");
            return false;
        }

        ClipboardFormat fileFormat = ClipboardFormats.findByFile(f);
        ClipboardFormat aliasFormat = ClipboardFormats.findByAlias("sponge");
        ClipboardFormat format = (fileFormat == null) ? aliasFormat : fileFormat;
        if (format == null) {
            System.out.println("Unknown schematic format for file " + f.getName());
            return false;
        }

        try (Closer closer = Closer.create()) {
            FileInputStream fis = closer.register(new FileInputStream(f));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = closer.register(format.getReader(bis));
            editSession.flushSession();
            Clipboard clipboard = reader.read();
            session.setClipboard(new ClipboardHolder(clipboard));

            ClipboardHolder holder = session.getClipboard();

            Operation operation = holder
                    .createPaste(editSession)
                    .to(location)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            editSession.flushSession();
        } catch (EmptyClipboardException e) {
            e.printStackTrace();
        } catch (WorldEditException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public ProtectedRegion getRegion(World w, String id) {
        if (w == null) {
            return null;
        }
        return wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w)).getRegion(id);
    }

    @Override
    public boolean hasRegion(World world, String id) {
        RegionManager mgr = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        return mgr.hasRegion(id);
    }

    @Override
    public boolean hasRegion(String world, String id) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return false;
        }
        RegionManager mgr = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        return mgr.hasRegion(id);
    }

    @Override
    public Flag<?> getWGFlag(String flagString) {
        Flag<?> f = Flags.get(flagString);
        if (f.getName().equalsIgnoreCase(flagString)) {
            return f;
        }

        throw new IllegalStateException("Worldguard flag " + flagString + " not found");
    }

    @Override
    public StateFlag getStateFlag(String flagString) {
        Flag<?> f = Flags.get(flagString);
        if (f instanceof StateFlag) {
            return (StateFlag) f;
        }

        throw new IllegalStateException("Worldguard flag " + flagString + " not found");
    }

    @Override
    public ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception {
        return createRegion(p, id);
    }

    @Override
    public ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
        return createRegion(p, id);
    }

    private ProtectedRegion createRegion(Player p, String id) throws Exception {
        Region sel = WorldEditController.getWorldEditPlugin().getSession(p).getSelection(BukkitAdapter.adapt(p.getWorld()));
        com.sk89q.worldedit.world.World w = sel.getWorld();
        RegionManager regionManager = wg.getPlatform().getRegionContainer().get(w);
        deleteRegion(w.getName(), id);
        ProtectedRegion region;
        // Detect the type of region from WorldEdit
        if (sel instanceof Polygonal2DRegion) {
            Polygonal2DRegion polySel = (Polygonal2DRegion) sel;
            int minY = polySel.getMinimumPoint().getBlockY();
            int maxY = polySel.getMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getPoints(), minY, maxY);
        } else { /// default everything to cuboid
            region = new ProtectedCuboidRegion(id,
                    sel.getMinimumPoint(),
                    sel.getMaximumPoint());
        }
        region.setPriority(11); /// some relatively high priority
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        regionManager.addRegion(region);
        regionManager.save();
        return region;
    }

    @Override
    public void deleteRegion(String worldName, String id) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            return;
        }
        RegionManager mgr = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        if (mgr == null) {
            return;
        }
        mgr.removeRegion(id);
    }

    @Override
    public boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
        return pasteSchematic(sender, pr.getMinimumPoint(), schematic, world);
    }
}
