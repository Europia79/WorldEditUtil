package mc.alk.arena.plugins.worldguard;

import mc.alk.arena.plugins.worldedit.WorldEditUtil;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.ArenaRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.euro.version.Version;
import mc.euro.version.VersionFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Stub class for future expansion
 *
 * @author alkarin
 *
 */
public abstract class WorldGuardInterface {

    WorldGuardPlugin wgp;
    boolean hasWorldGuard = false;

    Map<String, Set<String>> trackedRegions = new ConcurrentHashMap<String, Set<String>>();
    
    public static WorldGuardInterface newInstance() {
        WorldGuardInterface WGI = null;
        Version<Plugin> wg = VersionFactory.getPluginVersion("WorldGuard");
        if (wg.isCompatible("6") && wg.isSupported("6.99")) {
            // mc.alk.arena.plugins.worldguard.{version}.WG
            WGI = instantiate("v6");
        } else {
            WGI = instantiate("v5");
        }
        return WGI;
    }
    
    private static WorldGuardInterface instantiate(String version) {
        String classPackage = "mc.alk.arena.plugins.worldguard." + version + ".WG";
        WorldGuardInterface WGI = null;
        Class<?>[] args = {};
        Class clazz = null;
        Constructor con = null;
        try {
            clazz = Class.forName(classPackage);
            con = clazz.getConstructor(args);
            WGI = (WorldGuardInterface) con.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return WGI;
    }

    public boolean setWorldGuard(Plugin plugin) {
        wgp = (WorldGuardPlugin) plugin;
        hasWorldGuard = true;
        return hasWorldGuard();
    }

    public boolean hasWorldGuard() {
        return WorldEditUtil.hasWorldEdit() && hasWorldGuard;
    }

    public ProtectedRegion getRegion(String world, String id) {
        World w = Bukkit.getWorld(world);
        return getRegion(w, id);
    }

    public ProtectedRegion getRegion(World w, String id) {
        if (w == null) {
            return null;
        }
        return wgp.getRegionManager(w).getRegion(id);
    }

    public boolean hasRegion(ArenaRegion region) {
        return hasRegion(region.getWorldName(), region.getID());
    }

    public boolean hasRegion(World world, String id) {
        RegionManager mgr = wgp.getGlobalRegionManager().get(world);
        return mgr.hasRegion(id);
    }

    public boolean hasRegion(String world, String id) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return false;
        }
        RegionManager mgr = wgp.getGlobalRegionManager().get(w);
        return mgr.hasRegion(id);
    }

    public ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception {
        return createRegion(p, id);
    }

    public ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
        return createRegion(p, id);
    }

    private ProtectedRegion createRegion(Player p, String id) throws Exception {
        Selection sel = WorldEditUtil.getSelection(p);
        World w = sel.getWorld();
        RegionManager mgr = wgp.getGlobalRegionManager().get(w);
        mgr.removeRegion(id);
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
        region.setFlag(DefaultFlag.PVP, State.ALLOW);
        wgp.getRegionManager(w).addRegion(region);
        mgr.save();
        return region;
    }

    public void clearRegion(WorldGuardRegion region) {
        clearRegion(region.getRegionWorld(), region.getID());
    }

    public void clearRegion(String world, String id) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return;
        }
        ProtectedRegion region = getRegion(w, id);
        if (region == null) {
            return;
        }

        Location l;
        for (Entity entity : w.getEntitiesByClasses(Item.class, Creature.class)) {
            l = entity.getLocation();
            if (region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ())) {
                entity.remove();
            }
        }
    }

    public boolean isLeavingArea(final Location from, final Location to, final ArenaRegion region) {
        return isLeavingArea(from, to, Bukkit.getWorld(region.getWorldName()), region.getID());
    }

    public boolean isLeavingArea(final Location from, final Location to, final World w, String id) {
        ProtectedRegion pr = getRegion(w, id);
        return pr != null
                && (!pr.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())
                && pr.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
    }

    public boolean setFlag(WorldGuardRegion region, String flag, boolean enable) {
        return setFlag(region.getRegionWorld(), region.getID(), flag, enable);
    }

    public Flag<?> getWGFlag(String flagString) {
        for (Flag<?> f : DefaultFlag.getFlags()) {
            if (f.getName().equalsIgnoreCase(flagString)) {
                return f;
            }
        }
        throw new IllegalStateException("Worldguard flag " + flagString + " not found");
    }

    public StateFlag getStateFlag(String flagString) {
        for (Flag<?> f : DefaultFlag.getFlags()) {
            if (f.getName().equalsIgnoreCase(flagString) && f instanceof StateFlag) {
                return (StateFlag) f;
            }
        }
        throw new IllegalStateException("Worldguard flag " + flagString + " not found");
    }

    public boolean setFlag(String worldName, String id, String flag, boolean enable) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            return false;
        }
        ProtectedRegion pr = getRegion(w, id);
        if (pr == null) {
            return false;
        }
        StateFlag f = getStateFlag(flag);
        State newState = enable ? State.ALLOW : State.DENY;
        State state = pr.getFlag(f);

        if (state == null || state != newState) {
            pr.setFlag(f, newState);
        }
        return true;
    }

    public boolean allowEntry(Player player, String regionWorld, String id) {
        World w = Bukkit.getWorld(regionWorld);
        if (w == null) {
            return false;
        }
        ProtectedRegion pr = getRegion(w, id);
        if (pr == null) {
            return false;
        }
        DefaultDomain dd = pr.getMembers();
        dd.addPlayer(player.getName());
        pr.setMembers(dd);
        return true;
    }

    public boolean addMember(String playerName, WorldGuardRegion region) {
        return addMember(playerName, region.getRegionWorld(), region.getID());
    }

    public boolean addMember(String playerName, String regionWorld, String id) {
        return changeMember(playerName, regionWorld, id, true);
    }

    public boolean removeMember(String playerName, WorldGuardRegion region) {
        return removeMember(playerName, region.getRegionWorld(), region.getID());
    }

    public boolean removeMember(String playerName, String regionWorld, String id) {
        return changeMember(playerName, regionWorld, id, false);
    }

    private boolean changeMember(String name, String regionWorld, String id, boolean add) {
        World w = Bukkit.getWorld(regionWorld);
        if (w == null) {
            return false;
        }
        ProtectedRegion pr = getRegion(w, id);
        if (pr == null) {
            return false;
        }

        DefaultDomain dd = pr.getMembers();
        if (add) {
            dd.addPlayer(name);
        } else {
            dd.removePlayer(name);
        }
        pr.setMembers(dd);
        return true;
    }

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

    private void printError(LocalPlayer player, String msg) {
        if (player == null) {
            System.out.println(msg);
        } else {
            player.printError(msg);
        }
    }

    public boolean contains(Location location, WorldGuardRegion region) {
        ProtectedRegion pr = getRegion(region.getWorldName(), region.getID());
        return pr != null
                && pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean hasPlayer(String playerName, WorldGuardRegion region) {
        ProtectedRegion pr = getRegion(region.getWorldName(), region.getID());
        if (pr == null) {
            return true;
        }
        DefaultDomain dd = pr.getMembers();
        if (dd.contains(playerName)) {
            return true;
        }
        dd = pr.getOwners();
        return dd.contains(playerName);
    }

    public boolean trackRegion(ArenaRegion region) throws RegionNotFound {
        return trackRegion(region.getWorldName(), region.getID());
    }

    public boolean trackRegion(String world, String id) throws RegionNotFound {
        ProtectedRegion pr = getRegion(world, id);
        if (pr == null) {
            throw new RegionNotFound("The region " + id + " not found in world " + world);
        }
        Set<String> regions = trackedRegions.get(world);
        if (regions == null) {
            regions = new CopyOnWriteArraySet<String>();
            trackedRegions.put(world, regions);
        }
        return regions.add(id);
    }

    public int regionCount() {
        if (trackedRegions.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String world : trackedRegions.keySet()) {
            Set<String> sets = trackedRegions.get(world);
            if (sets != null) {
                count += sets.size();
            }
        }
        return count;
    }

    public WorldGuardRegion getContainingRegion(Location location) {
        for (String world : trackedRegions.keySet()) {
            World w = Bukkit.getWorld(world);
            if (w == null || location.getWorld().getUID() != w.getUID()) {
                continue;
            }
            for (String id : trackedRegions.get(world)) {
                ProtectedRegion pr = getRegion(w, id);
                if (pr == null) {
                    continue;
                }
                if (pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    return new WorldGuardRegion(world, id);
                }
            }
        }
        return null;
    }
    
    public boolean pasteSchematic(WorldGuardRegion region) {
        return pasteSchematic(region.getRegionWorld(), region.getID());
    }
    
    public boolean pasteSchematic(String worldName, String id) {
        return pasteSchematic(Bukkit.getConsoleSender(), worldName, id);
    }

    public boolean pasteSchematic(CommandSender consoleSender, String worldName, String id) {
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            return false;
        }
        ProtectedRegion pr = getRegion(w, id);
        return pr != null && pasteSchematic(consoleSender, pr, id, w);
    }
    
    public boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
        return pasteSchematic(sender, pr.getMinimumPoint(), schematic, world);
    }
    
    public abstract boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world);
    
    public abstract boolean saveSchematic(Player p, String schematicName);
    
}
