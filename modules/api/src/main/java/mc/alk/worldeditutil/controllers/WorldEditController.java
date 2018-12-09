package mc.alk.worldeditutil.controllers;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Stub class for future expansion
 *
 * @author alkarin
 *
 */
public class WorldEditController {

    public static WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

    public static boolean hasWorldEdit() {
        return wep != null;
    }

    public static boolean setWorldEdit(Plugin plugin) {
        if (plugin == null) return false;
        wep = (WorldEditPlugin) plugin;
        return true;
    }

    public static Selection getSelection(Player player) {
        return wep.getSelection(player);
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        return wep;
    }
    
}
