package mc.alk.arena.plugins.worldguard.v0;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.ArenaRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.worldeditutil.WorldGuardInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Fixes NoClassDefFound caused by ClassNotFoundException.
 * This is an implementation where Worldguard 
 * is not installed, not compatible, or not supported.
 * 
 * @author Nikolai
 */
public class WG extends WorldGuardInterface {

    @Override
    public boolean setWorldGuard(Plugin plugin) {
        return false;
    }

    /**
     * Even if getPlugin("WorldGuard"); does not equal null, the plugin 
     * may not be fully intialized.
     * @return Always returns false.
     */
    @Override
    public boolean hasWorldGuard() {
        return false;
    }

    @Override
    public ProtectedRegion getRegion(String world, String id) {
        return null;
    }

    @Override
    public ProtectedRegion getRegion(World w, String id) {
        return null;
    }

    @Override
    public boolean hasRegion(ArenaRegion region) {
        return false;
    }

    @Override
    public boolean hasRegion(World world, String id) {
        return false;
    }

    @Override
    public boolean hasRegion(String world, String id) {
        return false;
    }

    @Override
    public ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception {
        return null;
    }

    @Override
    public ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
        return null;
    }

    @Override
    public void clearRegion(WorldGuardRegion region) {
    }

    @Override
    public void clearRegion(String world, String id) {
    }

    @Override
    public boolean isLeavingArea(Location from, Location to, ArenaRegion region) {
        return false;
    }

    @Override
    public boolean isLeavingArea(Location from, Location to, World w, String id) {
        return false;
    }

    @Override
    public boolean setFlag(WorldGuardRegion region, String flag, boolean enable) {
        return false;
    }

    @Override
    public Flag<?> getWGFlag(String flagString) {
        return null;
    }

    @Override
    public StateFlag getStateFlag(String flagString) {
        return null;
    }

    @Override
    public boolean setFlag(String worldName, String id, String flag, boolean enable) {
        return false;
    }

    @Override
    public boolean allowEntry(Player player, String regionWorld, String id) {
        return true;
    }

    @Override
    public boolean addMember(String playerName, WorldGuardRegion region) {
        return false;
    }

    @Override
    public boolean addMember(String playerName, String regionWorld, String id) {
        return false;
    }

    @Override
    public boolean removeMember(String playerName, WorldGuardRegion region) {
        return false;
    }

    @Override
    public boolean removeMember(String playerName, String regionWorld, String id) {
        return false;
    }

    @Override
    public void deleteRegion(String worldName, String id) {
    }

    @Override
    public boolean contains(Location location, WorldGuardRegion region) {
        return false;
    }

    @Override
    public boolean hasPlayer(String playerName, WorldGuardRegion region) {
        return false;
    }

    @Override
    public boolean trackRegion(ArenaRegion region) throws RegionNotFound {
        return false;
    }

    @Override
    public boolean trackRegion(String world, String id) throws RegionNotFound {
        return false;
    }

    @Override
    public int regionCount() {
        return 0;
    }

    @Override
    public WorldGuardRegion getContainingRegion(Location location) {
        return new WorldGuardRegion();
    }

    @Override
    public boolean pasteSchematic(WorldGuardRegion region) {
        return false;
    }

    @Override
    public boolean pasteSchematic(String worldName, String id) {
        return false;
    }

    @Override
    public boolean pasteSchematic(CommandSender consoleSender, String worldName, String id) {
        return false;
    }

    @Override
    public boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
        return false;
    }
    
    @Override
    public boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        return false;
    }

    @Override
    public boolean saveSchematic(Player p, String schematicName) {
        return false;
    }
    
}
