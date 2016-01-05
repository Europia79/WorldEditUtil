package mc.alk.arena.plugins.worldguard;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.ArenaRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.euro.version.Tester;
import mc.euro.version.Version;
import mc.euro.version.VersionFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * We want to always reference an abstraction of WorldGuard and have the 
 * actual implementation decided at runtime: v0, v5, v6. <br/><br/>
 * 
 * v0 = WorldEdit not installed, not compatible, or not supported. <br/>
 * v0 = vZero, not vOH.
 * 
 * This class was a conversion from the static wrapper WorldGuardUtil.
 * Converted to an abstraction so that our implementation can vary at runtime.
 *
 * @author alkarin, Nikolai
 */
public abstract class WorldGuardInterface {
    
    /**
     * Instantiates: mc.alk.arena.plugins.worldguard.{version}.WG.
     * 
     * Based on the version of WorldEdit that the server has.
     */
    public static WorldGuardInterface newInstance() {
        WorldGuardInterface WGI = null;
        Version<Plugin> we = VersionFactory.getPluginVersion("WorldEdit");
        Version<Plugin> wg = VersionFactory.getPluginVersion("WorldGuard");
        WorldGuardPlugin wgp = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        Tester<WorldGuardPlugin> tester = new Tester<WorldGuardPlugin>() {

            @Override
            public boolean isEnabled(WorldGuardPlugin wgp) {
                if (wgp == null) return false;
                Field[] fields = wgp.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        if (field.get(wgp) == null) {
                            System.out.println("[BattleArena] WorldGuard is not fully initialized.");
                            System.out.println("[BattleArena] WorldGuard field " + field.getName() + " is null.");
                            return false;
                        }
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(WorldGuardInterface.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(WorldGuardInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return true;
            }
        };
        boolean wgIsInitialized = tester.isEnabled(wgp);
        if (we.isCompatible("6") && wg.isCompatible("6") && wgIsInitialized) {
            WGI = instantiate("v6");
        } else if (we.isCompatible("5") && we.isLessThan("6") && wg.isCompatible("5") && wg.isLessThan("6")&& wgIsInitialized) {
            WGI = instantiate("v5");
        } else {
            // Not present, not compatible, or not supported.
            System.out.println("[BattleArena] WG/WE not present, not compatible, or not supported.");
            WGI = instantiate("v0");
        }
        return WGI;
    }
    
    /**
     * Instantiates: mc.alk.arena.plugins.worldguard.{version}.WG.
     */
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
    
    public abstract boolean setWorldGuard(Plugin plugin);

    public abstract boolean hasWorldGuard();

    public abstract ProtectedRegion getRegion(String world, String id);

    public abstract ProtectedRegion getRegion(World w, String id);

    public abstract boolean hasRegion(ArenaRegion region);

    public abstract boolean hasRegion(World world, String id);

    public abstract boolean hasRegion(String world, String id);

    public abstract ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception;

    public abstract ProtectedRegion createProtectedRegion(Player p, String id) throws Exception;

    public abstract void clearRegion(WorldGuardRegion region);

    public abstract void clearRegion(String world, String id);

    public abstract boolean isLeavingArea(final Location from, final Location to, final ArenaRegion region);

    public abstract boolean isLeavingArea(final Location from, final Location to, final World w, String id);

    public abstract boolean setFlag(WorldGuardRegion region, String flag, boolean enable);

    public abstract Flag<?> getWGFlag(String flagString);

    public abstract StateFlag getStateFlag(String flagString);

    public abstract boolean setFlag(String worldName, String id, String flag, boolean enable);

    public abstract boolean allowEntry(Player player, String regionWorld, String id);

    public abstract boolean addMember(String playerName, WorldGuardRegion region);

    public abstract boolean addMember(String playerName, String regionWorld, String id);

    public abstract boolean removeMember(String playerName, WorldGuardRegion region);

    public abstract boolean removeMember(String playerName, String regionWorld, String id);

    public abstract void deleteRegion(String worldName, String id);

    public abstract boolean contains(Location location, WorldGuardRegion region);

    public abstract boolean hasPlayer(String playerName, WorldGuardRegion region);

    public abstract boolean trackRegion(ArenaRegion region) throws RegionNotFound;

    public abstract boolean trackRegion(String world, String id) throws RegionNotFound;

    public abstract int regionCount();

    public abstract WorldGuardRegion getContainingRegion(Location location);

    public abstract boolean pasteSchematic(WorldGuardRegion region);

    public abstract boolean pasteSchematic(String worldName, String id);

    public abstract boolean pasteSchematic(CommandSender consoleSender, String worldName, String id);

    public abstract boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world);

    public abstract boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world);

    public abstract boolean saveSchematic(Player p, String schematicName);

}
