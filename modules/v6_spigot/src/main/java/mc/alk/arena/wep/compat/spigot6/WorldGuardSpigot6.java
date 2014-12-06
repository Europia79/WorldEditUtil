package mc.alk.arena.wep.compat.spigot6;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.alk.arena.wep.compat.WorldGuardInterface;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * 
 * @author Nikolai
 */
public class WorldGuardSpigot6 extends WorldGuardInterface {

    @Override
    public boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveSchematic(Player p, String schematicName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadAndPaste(CommandContext args, WorldEdit we, LocalSession session, LocalPlayer player, EditSession editSession, Vector pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
