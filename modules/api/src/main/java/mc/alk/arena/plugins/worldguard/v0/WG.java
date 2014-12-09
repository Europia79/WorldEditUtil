package mc.alk.arena.plugins.worldguard.v0;

import com.sk89q.worldedit.Vector;
import mc.alk.arena.plugins.worldguard.WorldGuardInterface;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * 
 * @author Nikolai
 */
public class WG extends WorldGuardInterface {
    

    @Override
    public boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        return false;
    }

    @Override
    public boolean saveSchematic(Player p, String schematicName) {
        return false;
    }
    
}
