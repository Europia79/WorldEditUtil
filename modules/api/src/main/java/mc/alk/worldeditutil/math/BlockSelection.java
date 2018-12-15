package mc.alk.worldeditutil.math;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.World;

// Due to the removal of Selection in WorldEdit, this class now exists :)
public class BlockSelection {

    private Location min;
    private Location max;

    private World world;

    public BlockSelection(World world, Location min, Location max) {
        this.world = world;
        this.min = min;
        this.max = max;
    }

    public World getWorld() {
        return world;
    }

    public Location getMinimumPoint() {
        return min;
    }

    public Location getMaximumPoint() {
        return max;
    }
}
