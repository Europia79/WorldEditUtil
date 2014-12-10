package mc.alk.arena.plugins.worldguard.v5;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
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
import com.sk89q.worldedit.commands.SchematicCommands;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.IOException;
import mc.alk.arena.plugins.worldedit.WorldEditUtil;
import mc.alk.arena.plugins.worldguard.WorldGuardAbstraction;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 *
 * @author Alkarin
 */
public class WG extends WorldGuardAbstraction {

    @Override
    public boolean saveSchematic(Player p, String schematicName) {
        CommandContext cc;
        WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
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
    public boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        CommandContext cc;
        String args[] = {"load", schematic};
        final WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
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
                printError(player, "Schematic could not read or it does not exist.");
                return false;
            }
            SchematicFormat format = SchematicFormat.getFormat(f);
            if (format == null) {
                printError(player, "Unknown schematic format for file" + f);
                return false;
            }

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                printError(player, "Schematic could not read or it does not exist.");
            } else {
                session.setClipboard(format.load(f));
                // WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                // print(player,filePath + " loaded");
            }
            session.getClipboard().paste(editSession, pos, false, true);
            // WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
        } catch (DataException e) {
            printError(player, "Load error: " + e.getMessage());
        } catch (IOException e) {
            printError(player, "Schematic could not read or it does not exist: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            printError(player, "Error : " + e.getMessage());
        }
        return true;
    }

    private void printError(LocalPlayer player, String msg) {
        if (player == null) {
            System.out.println(msg);
        } else {
            player.printError(msg);
        }
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

}
