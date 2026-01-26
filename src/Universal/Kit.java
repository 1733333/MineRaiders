package Universal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public enum Kit {
    INSTANCE;
    public Block rayTraceBlock(Player p, int radius){
        Location eLoc = p.getEyeLocation();
        Vector eVec = eLoc.getDirection().normalize();
        Location bLoc = eLoc.clone();
        for(int i = 0;i <= radius;i ++){
            Block b = bLoc.getBlock();
            Material type = b.getType();
            if(type != Material.AIR && type != Material.LIGHT){
                return b;
            }
            bLoc.add(eVec.clone());
        }
        return null;
    }
    public String getLore(ItemStack item) {
        if(item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                String[] lore = meta.getLore().toArray(new String[0]);
                return lore[0];
            } else return "";
        }else return "";
    }


    public double distance(Location l1,Location l2){
        return (l1.subtract(l2)).length();
    }
}
