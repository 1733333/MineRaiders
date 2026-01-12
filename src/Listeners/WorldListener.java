package Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldListener implements Listener {
    Material[] normalContainer = {
            Material.PALE_OAK_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.WARPED_DOOR,
            Material.CRIMSON_DOOR,
            Material.DAYLIGHT_DETECTOR,
            Material.FLOWER_POT,
            Material.ANVIL,
            Material.CRAFTING_TABLE,
            Material.COMPOSTER,
            Material.BOOKSHELF,
            Material.CAULDRON,
            Material.CARVED_PUMPKIN,
            Material.JUKEBOX,
            Material.MUDDY_MANGROVE_ROOTS
    };
    Material[] goodContainer = {
            Material.BEEHIVE,
            Material.WAXED_COPPER_GOLEM_STATUE,
            Material.WAXED_COPPER_CHEST,
            Material.CHEST,
            Material.BREWING_STAND,
            Material.FURNACE,
            Material.BARREL,
            Material.BEE_NEST,
            Material.LECTERN
    };
    Material[] bestContainer = {
            Material.CRAFTER,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.SMITHING_TABLE,
            Material.FLETCHING_TABLE,
            Material.LOOM
    };
    public float[] getContainerValue(Block b) {
        List<Material> normalContainerList = Arrays.stream(normalContainer).toList();
        List<Material> goodContainerList = Arrays.stream(goodContainer).toList();
        List<Material> bestContainerList = Arrays.stream(bestContainer).toList();
        Material m = b.getType();
        if (normalContainerList.contains(m)) return new float[]{4,2,2,1,0.75f,0.25f};
        if (goodContainerList.contains(m)) return new float[]{3,2.5f,2,1,1,0.5f};
        if (bestContainerList.contains(m)) return new float[]{2,2.5f,2,1.5f,1,1};
        return new float[0];
    }
}
