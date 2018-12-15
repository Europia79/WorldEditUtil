package mc.alk.arena.plugins.worldguard.v5;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.commands.SchematicCommands;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.File;
import java.io.IOException;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.alk.worldeditutil.controllers.WorldEditController;
import mc.alk.worldeditutil.math.BlockSelection;
import mc.alk.worldeditutil.math.BlockVector;
import mc.alk.worldeditutil.WorldGuardAbstraction;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The WorldEdit v5.x implementation.
 *
 * Why does this exist under the WorldGuard Utilities ?
 * Because intention of saveSchematic() is really saveRegion().
 * And the intention of pasteSchematic() is really resetRegion().
 *
 * @author Alkarin
 */
public class WG extends WorldGuardAbstraction {

    @Override
    public boolean saveSchematic(Player p, String schematicName) {
        CommandContext cc;
        WorldEditPlugin wep = WorldEditController.getWorldEditPlugin();
        final LocalSession session = wep.getSession(p);
        final BukkitPlayer lPlayer = wep.wrapPlayer(p);
        EditSession editSession = session.createEditSession(lPlayer);

        try {
            Region region = session.getSelection(lPlayer.getWorld());
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();
            CuboidClipboard clipboard = new CuboidClipboard(
                    max.subtract(min).add(new Vector(1, 1, 1)),
                    min, new Vector(0, 0, 0));
            clipboard.copy(editSession);
            session.setClipboard(clipboard);

            SchematicCommands sc = new SchematicCommands(wep.getWorldEdit());
            String args2[] = {"save", "mcedit", schematicName};
            cc = new CommandContext(args2);
            sc.save(cc, session, lPlayer, editSession);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        return new BlockSelection(BukkitUtil.toWorld(region.getWorld()), BukkitUtil.toLocation(BukkitUtil.toWorld(region.getWorld()), region.getMinimumPoint()), BukkitUtil.toLocation(BukkitUtil.toWorld(region.getWorld()), region.getMaximumPoint()));
    }

    @Override
    public BlockSelection getBlockSelection(World world, ProtectedRegion region) {
        return new BlockSelection(world, BukkitUtil.toLocation(world, region.getMinimumPoint()), BukkitUtil.toLocation(world, region.getMaximumPoint()));
    }

    @Override
    public boolean pasteSchematic(CommandSender sender, BlockVector position, String schematic, World world) {
        return pasteSchematic(sender, new Vector(position.x, position.y, position.z), schematic, world);
    }

    private boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        CommandContext cc;
        String args[] = {"load", schematic};
        final WorldEditPlugin wep = WorldEditController.getWorldEditPlugin();
        final WorldEdit we = wep.getWorldEdit();
        LocalPlayer bcs = new ConsolePlayer(wep, wep.getServerInterface(), sender, world);

        final LocalSession session = wep.getWorldEdit().getSession(bcs);
        session.setUseInventory(false);
        EditSession editSession = session.createEditSession(bcs);
        try {
            cc = new CommandContext(args);
            return loadAndPaste(cc, we, session, bcs, editSession, position);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This is just copied and pasted from world edit source, with small changes
     * to also paste
     *
     * @param args CommandContext
     * @param we WorldEdit
     * @param session LocalSession
     * @param player LocalPlayer
     * @param editSession EditSession
     * @return
     */
    public boolean loadAndPaste(CommandContext args, WorldEdit we,
                                LocalSession session, LocalPlayer player, EditSession editSession, Vector pos) {

        LocalConfiguration config = we.getConfiguration();

        String filename = args.getString(0);
        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f;
        try {
            f = we.getSafeOpenFile(player, dir, filename, "schematic", "schematic");
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Schematic could not read or it does not exist.");
                return false;
            }
            SchematicFormat format = SchematicFormat.getFormat(f);
            if (format == null) {
                player.printError("Unknown schematic format for file" + f);
                return false;
            }

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Schematic could not read or it does not exist.");
            } else {
                session.setClipboard(format.load(f));
                // WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                // print(player,filePath + " loaded");
            }
            session.getClipboard().paste(editSession, pos, false, true);
            // WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
        } catch (DataException e) {
            player.printError("Load error: " + e.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not read or it does not exist: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            player.printError("Error : " + e.getMessage());
        }
        return true;
    }

    public class ConsolePlayer extends BukkitCommandSender {

        LocalWorld world;

        public ConsolePlayer(WorldEditPlugin plugin, ServerInterface server, CommandSender sender, World w) {
            super(plugin, server, sender);
            world = BukkitUtil.getLocalWorld(w);
        }

        @Override
        public boolean isPlayer() {
            return true;
        }

        @Override
        public LocalWorld getWorld() {
            return world;
        }
    }

    @Override
    public ProtectedRegion getRegion(World w, String id) {
        if (w == null) {
            return null;
        }
        return wgp.getRegionManager(w).getRegion(id);
    }

    @Override
    public boolean hasRegion(World world, String id) {
        RegionManager mgr = wgp.getGlobalRegionManager().get(world);
        return mgr.hasRegion(id);
    }

    @Override
    public boolean hasRegion(String world, String id) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return false;
        }
        RegionManager mgr = wgp.getGlobalRegionManager().get(w);
        return mgr.hasRegion(id);
    }

    @Override
    public Flag<?> getWGFlag(String flagString) {
        for (Flag<?> f : DefaultFlag.getFlags()) {
            if (f.getName().equalsIgnoreCase(flagString)) {
                return f;
            }
        }
        throw new IllegalStateException("Worldguard flag " + flagString + " not found");
    }

    @Override
    public StateFlag getStateFlag(String flagString) {
        for (Flag<?> f : DefaultFlag.getFlags()) {
            if (f.getName().equalsIgnoreCase(flagString) && f instanceof StateFlag) {
                return (StateFlag) f;
            }
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
        Selection sel = WorldEditController.getSelection(p);
        World w = sel.getWorld();
        GlobalRegionManager gmanager = wgp.getGlobalRegionManager();
        RegionManager regionManager = gmanager.get(w);
        deleteRegion(w.getName(), id);
        ProtectedRegion region;
        // Detect the type of region from WorldEdit
        if (sel instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else { /// default everything to cuboid
            region = new ProtectedCuboidRegion(id,
                    sel.getNativeMinimumPoint().toBlockVector(),
                    sel.getNativeMaximumPoint().toBlockVector());
        }
        region.setPriority(11); /// some relatively high priority
        region.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
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
        RegionManager mgr = wgp.getRegionManager(w);
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
