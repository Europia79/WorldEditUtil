package mc.alk.arena.plugins.worldguard.v6;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import mc.alk.arena.plugins.worldedit.WorldEditUtil;
import mc.alk.arena.plugins.worldguard.WorldGuardInterface;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * Not finished.
 *
 * Self reminder: In order to fix the compiler errors and incompatibilities with
 * WorldEdit v6.0.0, I recommend working backwards from loadAndPaste() first,
 * then moving to the calling code, pasteSchematic().
 *
 * The reason for working backwards is because the meat of what has to be done
 * is clipboard.paste(). So we first need to find the WE v6 equivalent in the
 * API, then constructing the necessary arguments to satisfy a paste() call.
 *
 * @author Nikolai
 */
public class WG extends WorldGuardInterface {

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
     * Error: LocalPlayer bsc = new ConsolePlayer(); line 80
     */
    @Override
    public boolean pasteSchematic(CommandSender sender, Vector position, String schematic, World world) {
        /* CommandContext cc;
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
         } */
        return false;
    }

    /**
     * This is just copied and pasted from world edit source, with small changes
     * to also paste.
     * @param args CommandContext
     * @param we WorldEdit
     * @param session LocalSession
     * @param player LocalPlayer
     * @param editSession EditSession
     * @return
     */
    public boolean loadAndPaste(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession, Vector pos) {
        /*
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
                session.setClipboard(format.load(f)); // WE v6 ERROR *************
                // WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                // print(player,filePath + " loaded");
            }
            session.getClipboard().paste(editSession, pos, false, true); // WE v6 ERROR ***
            // WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
        } catch (DataException e) {
            printError(player, "Load error: " + e.getMessage());
        } catch (IOException e) {
            printError(player, "Schematic could not read or it does not exist: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            printError(player, "Error : " + e.getMessage());
        } */
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
            super(plugin, sender);
            world = BukkitUtil.getLocalWorld(w);
        }

        @Override
        public boolean isPlayer() {
            return true;
        }

        public LocalWorld getWorld() {
            return world;
        }
    }

}
