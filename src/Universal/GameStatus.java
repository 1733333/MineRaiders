package Universal;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public enum GameStatus {
    INSTANCE;
    HashMap<String, HashSet<Block>> worldContainers = new HashMap<>();
    HashMap<String, List<Entity>> worldExtractions = new HashMap<>();

    public boolean isEmpty(Block b) {
        String world = b.getWorld().getName();
        HashSet<Block> set = worldContainers.getOrDefault(world, new HashSet<>());
        return set.contains(b);
    }
    public void setEmpty(Block b){
        String world = b.getWorld().getName();
        HashSet<Block> set = worldContainers.getOrDefault(world, new HashSet<>());
        set.add(b);
        worldContainers.put(world,set);
    }
    public void refillContainers(World w){
        worldContainers.remove(w.getName());
    }

    public void addExtraction(Entity e){
        String world = e.getWorld().getName();
        List<Entity>list = worldExtractions.getOrDefault(world,new ArrayList<>());
        list.add(e);
        worldExtractions.put(world,list);
    }
    public void removeExtraction(Entity e){
        String world = e.getWorld().getName();
        List<Entity>list = worldExtractions.getOrDefault(world,new ArrayList<>());
        list.remove(e);
        worldExtractions.put(world,list);
    }
    public void clearExtractions(World w){
        worldExtractions.remove(w.getName());
    }
}
